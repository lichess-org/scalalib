package scalalib

import java.util.concurrent.ConcurrentHashMap
import scala.concurrent.ExecutionContext

/* Runs f(id)` immediately the first time `push(id)` is called for a given id.
 * Then if further calls to `push(id)` are made, they're discarded
 * and `f(id)` is called at most once every `duration`.
 * It is guaranteed that `f(id)` will run after the last `push(id)`. */
final class Debouncer[Id](
    scheduleOnce: Runnable => Unit, // scheduler.scheduleOnce(duration, _)
    initialCapacity: Int = 64
)(
    f: Id => Unit
)(using ExecutionContext):

  // can't use a boolean or int,
  // the ConcurrentHashMap uses weird defaults instead of null for missing values
  private enum Queued:
    case Another, Empty

  private val debounces = ConcurrentHashMap[Id, Queued](initialCapacity)

  def push(id: Id): Unit = debounces
    .compute(
      id,
      (_, prev) =>
        Option(prev) match
          case None =>
            f(id)
            scheduleOnce { () => runScheduled(id) }
            Queued.Empty
          case _ => Queued.Another
    )

  private def runScheduled(id: Id): Unit = debounces
    .computeIfPresent(
      id,
      (_, queued) =>
        if queued == Queued.Another then
          f(id)
          scheduleOnce { () => runScheduled(id) }
          Queued.Empty
        else nullToRemove
    )

  @scala.annotation.nowarn
  private var nullToRemove: Queued = scala.compiletime.uninitialized
