package scalalib

/* Exactly like ConcurrentHashMap but using Option instead of null.
 * I didn't like the scala.collection.Concurrent.Map overrides in
 * https://github.com/scala/scala/blob/2.13.x/src/library/scala/collection/convert/JavaCollectionWrappers.scala
 * e.g. the extra `underlying.containsKey(k)` in `get(k)`
 * and the absence of `computeIfPresent`. */
final class ConcurrentMap[K, V](initialCapacity: Int):

  private val underlying = new java.util.concurrent.ConcurrentHashMap[K, V](initialCapacity)

  export underlying.{ put as put, remove as remove }

  def get(key: K): Option[V] = Option(underlying.get(key))

  /* Runs f exactly once and returns the new value */
  def compute(key: K)(f: Option[V] => Option[V]): Option[V] =
    Option:
      underlying.compute(key, (k, v) => f(Option(v)).getOrElse(null.asInstanceOf[V]))

  /* Runs f at most once and returns the new value */
  def computeIfPresent(key: K)(f: V => Option[V]): Option[V] =
    Option:
      underlying.computeIfPresent(key, (k, v) => f(v).getOrElse(null.asInstanceOf[V]))

  /* Runs f at most once and returns the new value */
  def computeIfAbsent(key: K)(f: => Option[V]): Option[V] =
    Option:
      underlying.computeIfAbsent(key, _ => f.getOrElse(null.asInstanceOf[V]))

  def remove(key: K): Option[V] = Option(underlying.remove(key))
