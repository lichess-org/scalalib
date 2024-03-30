package ornicar.scalalib
package actor

import java.util.concurrent.atomic.AtomicReference
import java.util.function.UnaryOperator
import scala.collection.immutable.Queue
import scala.concurrent.{ ExecutionContext, Future, Promise }

/*
 * Like an actor, but not an actor.
 * Uses an Atomic Reference backend for sequentiality.
 * Has an unbounded (!) Queue of messages.
 */
abstract class SyncActor(using ExecutionContext):

  import SyncActor.*

  // implement async behaviour here
  protected val process: Receive

  protected var isAlive = true

  def getIsAlive = isAlive

  def stop(): Unit =
    isAlive = false

  def !(msg: Matchable): Unit =
    if isAlive && stateRef
        .getAndUpdate(state => Some(state.fold(Queue.empty[Matchable])(_.enqueue(msg))))
        .isEmpty
    then run(msg)

  def ask[A](makeMsg: Promise[A] => Matchable): Future[A] =
    val promise = Promise[A]()
    this ! makeMsg(promise)
    promise.future

  def queueSize = stateRef.get().fold(0)(_.size + 1)

  /*
   * Idle: None
   * Busy: Some(Queue.empty)
   * Busy with backlog: Some(Queue.nonEmpty)
   */
  private val stateRef: AtomicReference[State] = new AtomicReference(None)

  private def run(msg: Matchable): Unit =
    Future {
      process(msg)
    }.onComplete(postRun)

  private val postRun = (_: Matchable) =>
    stateRef.getAndUpdate(postRunUpdate).flatMap(_.headOption).foreach(run)

object SyncActor:

  type Receive = Matchable => Unit

  private type State = Option[Queue[Matchable]]

  private val postRunUpdate = new UnaryOperator[State]:
    override def apply(state: State): State =
      state.flatMap { q =>
        if q.isEmpty then None else Some(q.tail)
      }
