package scalalib

import java.util.concurrent.ConcurrentHashMap

/* Like Debouncer, but a function is passed to `push(id)(f)`.
 * Runs f()` immediately the first time `push(id)` is called for a given id.
 * Then if further calls to `push(id)` are made, they're discarded
 * and `f()` is called at most once every `duration`.
 * It is guaranteed that `f()` will run after the last `push(id)`. */
final class DebouncerFunction[Id](
    scheduleOnce: Runnable => Unit, // scheduler.scheduleOnce(duration, _)
    initialCapacity: Int = 64
):

  private enum Queued:
    case Another(f: () => Unit)
    case Empty

  private val debounces = ConcurrentHashMap[Id, Queued](initialCapacity)

  def push(id: Id)(f: () => Unit): Unit = debounces
    .compute(
      id,
      (_, prev) =>
        Option(prev) match
          case None =>
            f()
            scheduleOnce { () => runScheduled(id) }
            Queued.Empty
          case _ => Queued.Another(f)
    )

  private def runScheduled(id: Id): Unit = debounces
    .computeIfPresent(
      id,
      (_, queued) =>
        queued match
          case Queued.Another(f) =>
            f()
            scheduleOnce { () => runScheduled(id) }
            Queued.Empty
          case Queued.Empty => nullToRemove
    )

  @scala.annotation.nowarn
  private var nullToRemove: Queued = scala.compiletime.uninitialized
