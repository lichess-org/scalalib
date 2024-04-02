package scalalib
package cache

import com.github.blemale.scaffeine.Scaffeine
import com.github.benmanes.caffeine.cache.Scheduler
import java.util.concurrent.Executor
import scala.concurrent.duration.FiniteDuration
import com.github.blemale.scaffeine.Cache
import java.util.concurrent.ConcurrentMap

def scaffeine(using Executor): Scaffeine[Any, Any] =
  scaffeineNoScheduler.scheduler(Scheduler.systemScheduler)

def scaffeineNoScheduler(using ex: Executor): Scaffeine[Any, Any] =
  Scaffeine().executor(ex)

final class ExpireSetMemo[K](ttl: FiniteDuration)(using Executor):

  private val cache: Cache[K, Boolean] = scaffeineNoScheduler
    .expireAfterWrite(ttl)
    .build[K, Boolean]()

  // we compare to `true` so that `null` is turned to `false`
  def get(key: K): Boolean = cache.underlying.getIfPresent(key) == true

  def intersect(keys: Iterable[K]): Set[K] =
    if keys.nonEmpty then
      val res = cache.getAllPresent(keys)
      keys.filter(res.contains).toSet
    else Set.empty

  def put(key: K) = cache.put(key, true)

  def putAll(keys: Iterable[K]) = cache.putAll(keys.view.map(k => k -> true).toMap)

  def remove(key: K) = cache.invalidate(key)

  def removeAll(keys: Iterable[K]) = cache.invalidateAll(keys)

  def keys: Iterable[K] = cache.asMap().keys

  def keySet: Set[K] = keys.toSet

  def count: Int = cache.estimatedSize().toInt

final class HashCodeExpireSetMemo[A](ttl: FiniteDuration)(using Executor):

  private val cache: Cache[Int, Boolean] = scaffeineNoScheduler
    .expireAfterWrite(ttl)
    .build[Int, Boolean]()

  def get(key: A): Boolean = cache.underlying.getIfPresent(key.hashCode) == true

  def put(key: A): Unit = cache.put(key.hashCode, true)

  def remove(key: A): Unit = cache.invalidate(key.hashCode)

object OnceEvery:

  def apply[A](ttl: FiniteDuration)(using Executor): A => Boolean =

    val cache = ExpireSetMemo[A](ttl)

    key =>
      val isNew = !cache.get(key)
      if isNew then cache.put(key)
      isNew

  def hashCode[A](ttl: FiniteDuration)(using Executor): A => Boolean =

    val cache = HashCodeExpireSetMemo[A](ttl)

    key =>
      val isNew = !cache.get(key)
      if isNew then cache.put(key)
      isNew

final class FrequencyThreshold[K](count: Int, duration: FiniteDuration)(using Executor):

  private val cache = Scaffeine()
    .expireAfter[K, Int](
      create = (_, _) => duration,
      update = (_, _, current) => current,
      read = (_, _, current) => current
    )
    .build[K, Int]()

  private val concMap: ConcurrentMap[K, Int] = cache.underlying.asMap()

  /* Returns true when called more than `count` times in `duration` window. */
  def apply(key: K): Boolean = concMap.compute(
    key,
    (_, prev) => Option(prev).fold(1)(_ + 1)
  ) >= count
