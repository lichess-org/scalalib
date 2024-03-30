package scalalib
package actor

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator
import scala.collection.immutable.Queue
import scala.concurrent.{ ExecutionContext, Future, Promise }

import scalalib.model.Max

/*
 * Sequential like an actor, but for async functions,
 * and using an atomic backend instead of akka actor.
 */
final class AsyncActorBounded(
    maxSize: Max,
    name: String,
    monitor: AsyncActorBounded.Monitor
)(
    process: AsyncActor.ReceiveAsync
)(using ExecutionContext):

  import AsyncActorBounded.*

  def !(msg: Matchable): Boolean =
    stateRef
      .getAndUpdate: state =>
        Some:
          state.fold(emptyQueue): q =>
            if q.size >= maxSize.value then q
            else q.enqueue(msg)
      .match
        case None => // previous state was idle, we can run immediately
          run(msg)
          true
        case Some(q) =>
          val success = q.size < maxSize.value
          if !success then monitor.overflow(name)
          else if q.size >= monitorQueueSize then monitor.queueSize(name, q.size)
          success

  def ask[A](makeMsg: Promise[A] => Matchable): Future[A] =
    val promise = Promise[A]()
    val success = this ! makeMsg(promise)
    if !success then promise.failure(new EnqueueException(s"The $name asyncActor queue is full ($maxSize)"))
    promise.future

  def queueSize = stateRef.get().fold(0)(_.size + 1)

  private val monitorQueueSize = maxSize.value / 4

  /*
   * Idle: None
   * Busy: Some(Queue.empty)
   * Busy with backlog: Some(Queue.nonEmpty)
   */
  private val stateRef: AtomicReference[State] = new AtomicReference(None)

  private def run(msg: Matchable): Unit =
    process.applyOrElse(msg, fallback).onComplete(postRun)

  private val postRun = (_: Matchable) =>
    stateRef.getAndUpdate(postRunUpdate).flatMap(_.headOption).foreach(run)

  private lazy val fallback = (msg: Any) =>
    monitor.unhandled(name, msg)
    Future.unit

object AsyncActorBounded:

  case class Monitor(
      overflow: String => Unit,
      queueSize: (String, Int) => Unit,
      unhandled: (String, Any) => Unit
  )

  final class EnqueueException(msg: String) extends Exception(msg)

  private case class SizedQueue(queue: Queue[Matchable], size: Int):
    def enqueue(a: Matchable) = SizedQueue(queue.enqueue(a), size + 1)
    def isEmpty               = size == 0
    def nonEmpty              = !isEmpty
    def tailOption            = Option.when(nonEmpty)(SizedQueue(queue.tail, size - 1))
    def headOption            = queue.headOption
  private val emptyQueue = SizedQueue(Queue.empty, 0)

  private type State = Option[SizedQueue]

  private val postRunUpdate = new UnaryOperator[State]:
    override def apply(state: State): State = state.flatMap(_.tailOption)
