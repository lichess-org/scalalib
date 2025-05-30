package scalalib
package data

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.Future
import java.time.Instant

import scalalib.time.*

/* Like a lazy val that would recompute its value every `ttl`.
 * Only works for synchronous computations. */
final class SimpleMemo[A](ttl: Option[FiniteDuration])(compute: () => A):
  private var value: A                     = compute()
  private var recomputeAt: Option[Instant] = ttl.map(nowInstant.plus(_))
  def get(): A                             =
    if recomputeAt.exists(_.isBeforeNow) then
      recomputeAt = ttl.map(nowInstant.plus(_))
      value = compute()
    value

/* Optional value that can be otherwise be loaded asynchronously later on */
case class Preload[A](value: Option[A]) extends AnyVal:
  def orLoad(f: => Future[A]): Future[A] = value.fold(f)(Future.successful)
object Preload:
  def apply[A](value: A): Preload[A] = Preload(Some(value))
  def none[A]                        = Preload[A](None)

/* A lazily evaluated future */
final class LazyFu[A](run: () => Future[A]):
  lazy val value: Future[A]         = run()
  def dmap[B](f: A => B): LazyFu[B] =
    LazyFu(() => value.map(f)(using scala.concurrent.ExecutionContext.parasitic))
object LazyFu:
  def sync[A](v: => A): LazyFu[A] = LazyFu(() => Future.successful(v))
