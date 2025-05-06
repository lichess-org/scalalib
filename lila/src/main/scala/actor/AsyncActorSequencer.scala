package scalalib
package actor

import com.github.blemale.scaffeine.{ LoadingCache, Scaffeine }
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.{ ExecutionContext, Future, Promise }

import scalalib.model.Max
import scalalib.future.FutureAfter
import scalalib.future.extensions.*
import java.util.concurrent.Executor

final class AsyncActorSequencer(
    maxSize: Max,
    timeout: FiniteDuration,
    name: String,
    monitor: AsyncActorBounded.Monitor
)(using ExecutionContext, FutureAfter):

  import AsyncActorSequencer.*

  def apply[A <: Matchable](fu: => Future[A]): Future[A] = run(() => fu)

  def run[A <: Matchable](task: Task[A]): Future[A] = asyncActor.ask[A](TaskWithPromise(task, _))

  private val asyncActor = AsyncActorBounded(maxSize, name, monitor):
    case TaskWithPromise(task, promise) =>
      promise.completeWith {
        task().withTimeout(timeout, s"AsyncActorSequencer $name")
      }.future

// Distributes tasks to many sequencers
final class AsyncActorSequencers[K](
    maxSize: Max,
    expiration: FiniteDuration,
    timeout: FiniteDuration,
    name: String,
    monitor: AsyncActorBounded.Monitor
)(using Executor, ExecutionContext, FutureAfter):

  def apply[A <: Matchable](key: K)(task: => Future[A]): Future[A] =
    sequencers.get(key).run(() => task)

  private val sequencers: LoadingCache[K, AsyncActorSequencer] =
    cache.scaffeine
      .expireAfterAccess(expiration)
      .build(key => AsyncActorSequencer(maxSize, timeout, s"$name:$key", monitor))

object AsyncActorSequencer:

  private type Task[A <: Matchable] = () => Future[A]
  private case class TaskWithPromise[A <: Matchable](task: Task[A], promise: Promise[A])
