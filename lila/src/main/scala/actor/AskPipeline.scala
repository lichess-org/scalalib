package scalalib
package actor

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ Future, ExecutionContext, Promise }
import com.github.blemale.scaffeine.LoadingCache
import java.util.concurrent.Executor

import scalalib.future.FutureAfter
import scalalib.future.extensions.*
import scalalib.cache.scaffeine

/*
 * Only processes one computation at a time
 * and only enqueues one.
 */
final class AskPipeline[A](
    compute: () => Future[A],
    timeout: FiniteDuration,
    name: String
)(using FutureAfter, ExecutionContext)
    extends SyncActor:

  private var state: State = State.Idle

  protected val process: SyncActor.Receive =

    case Get(promise) =>
      state match
        case State.Idle =>
          startComputing()
          state = State.Processing(List(promise), Nil)
        case p @ State.Processing(_, next) =>
          state = p.copy(next = promise :: next)

    case Done(res) => complete(Right(res))

    case Fail(err) => complete(Left(err))

  def get: Future[A] = ask[A](Get.apply)

  private def startComputing() =
    compute()
      .withTimeout(timeout, s"AskPipeline $name")
      .addEffects(
        err => this ! Fail(err),
        res => this ! Done(res)
      )

  private def complete(res: Either[Exception, A]) =
    state match
      case State.Idle => // shouldn't happen
      case State.Processing(current, next) =>
        res.fold(
          err => current.foreach(_.failure(err)),
          res => current.foreach(_.success(res))
        )
        if next.isEmpty
        then state = State.Idle
        else
          startComputing()
          state = State.Processing(next, Nil)

  private case class Get(promise: Promise[A])
  private case class Done(result: A)
  private case class Fail(err: Exception)

  private enum State:
    case Idle extends State
    case Processing(current: List[Promise[A]], next: List[Promise[A]]) extends State

// Distributes tasks to many pipelines
final class AskPipelines[K, R](
    compute: K => Future[R],
    expiration: FiniteDuration,
    timeout: FiniteDuration,
    name: String
)(using FutureAfter, ExecutionContext, Executor):

  def apply(key: K): Future[R] = pipelines.get(key).get

  private val pipelines: LoadingCache[K, AskPipeline[R]] =
    scaffeine
      .expireAfterAccess(expiration)
      .build(key => AskPipeline[R](() => compute(key), timeout, name = s"$name:$key"))
