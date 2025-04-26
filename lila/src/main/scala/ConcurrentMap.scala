package scalalib

import scala.jdk.CollectionConverters.*
import typemap.{ MutableMapOps, ThreadSafeMutableMapOps }

/* Exactly like ConcurrentHashMap but using Option instead of null.
 * I didn't like the scala.collection.Concurrent.Map overrides in
 * https://github.com/scala/scala/blob/2.13.x/src/library/scala/collection/convert/JavaCollectionWrappers.scala
 * e.g. the extra `underlying.containsKey(k)` in `get(k)`
 * and the absence of `computeIfPresent`.
 **/
final class ConcurrentMap[K, V](initialCapacity: Int):

  private val underlying = new java.util.concurrent.ConcurrentHashMap[K, V](initialCapacity)

  export underlying.{ put, size, contains, remove, containsKey, forEach as foreach }

  def get(key: K): Option[V] = Option(underlying.get(key))

  /* Runs f exactly once and returns the new value */
  def compute(key: K)(f: Option[V] => Option[V]): Option[V] =
    Option:
      underlying.compute(key, (_, v) => f(Option(v)).getOrElse(null.asInstanceOf[V]))

  /* Runs f at most once and returns the new value */
  def computeIfPresent(key: K)(f: V => Option[V]): Option[V] =
    Option:
      underlying.computeIfPresent(key, (_, v) => f(v).getOrElse(null.asInstanceOf[V]))

  /* Runs f at most once and returns the new value */
  def computeIfAbsent(key: K)(f: => Option[V]): Option[V] =
    Option:
      underlying.computeIfAbsent(key, _ => f.getOrElse(null.asInstanceOf[V]))

  /* Runs f at most once and returns the new value */
  def computeIfAbsentAlways(key: K)(f: => V): V =
    underlying.computeIfAbsent(key, _ => f)

  def remove(key: K): Option[V] = Option(underlying.remove(key))

  /* better than ConcurrentHashMap.getOrDefault because our default is lazy */
  def getOrDefault(key: K, default: => V): V = get(key).getOrElse(default)

  def keySet: Set[K] = underlying.keySet.asScala.toSet

object ConcurrentMap:
  type Backend = [X] =>> ConcurrentMap[String, X]
  given [V]: MutableMapOps[Backend, V] with
    private type DS = Backend[V]
    def make(length: Int): DS                    = new DS(length)
    def get(ds: DS, key: String): Option[V]      = ds.get(key)
    def put(ds: DS, key: String, value: V): Unit = ds.put(key, value)

  given [V]: ThreadSafeMutableMapOps[Backend, V] with
    private type DS = Backend[V]
    def computeIfAbsent(ds: DS, key: String, f: => V): V =
      ds.computeIfAbsent(key)(Option(f)).get
    def computeIfPresent(ds: DS, key: String, f: V => V): Option[V] =
      // we just wrapped it in Some
      ds.computeIfPresent(key)((v) => Some(f(v)))
    def compute(ds: DS, key: String, f: Option[V] => V): V =
      // we just wrapped it in Some
      ds.compute(key)((vOpt) => Some(f(vOpt))).get
