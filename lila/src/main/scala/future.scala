package scalalib
package future

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext as EC
import scala.util.control.NoStackTrace
import scala.util.Try
import alleycats.Zero
import scalalib.extensions.so

/** Returns a [[scala.concurrent.Future]] that will be completed with the success or failure of the provided
  * value after the specified duration. Implementation can be done with or without akka/pekko
  */
type FutureAfter = [T] => (FiniteDuration) => (() => Future[T]) => Future[T]

final class TimeoutException(msg: String) extends Exception(msg) with NoStackTrace

given [A: Zero] => Zero[Future[A]]:
  def zero = Future.successful(Zero[A].zero)

object extensions:

  extension [A](fua: Future[A])

    inline def dmap[B](f: A => B): Future[B]   = fua.map(f)(using EC.parasitic)
    inline def dforeach[B](f: A => Unit): Unit = fua.foreach(f)(using EC.parasitic)

    def andDo(sideEffect: => Unit)(using EC): Future[A] =
      fua.andThen:
        case _ => sideEffect

    inline def void: Future[Unit] =
      fua.dmap(_ => ())

    inline def discard: Unit = ()

    inline infix def inject[B](b: => B): Future[B] =
      fua.dmap(_ => b)

    def addFailureEffect(effect: Throwable => Unit)(using EC) =
      fua.failed.foreach: (e: Throwable) =>
        effect(e)
      fua

    def addEffect(effect: A => Unit)(using EC): Future[A] =
      fua.foreach(effect)
      fua

    def addEffects(fail: Exception => Unit, succ: A => Unit)(using EC): Future[A] =
      fua.onComplete:
        case scala.util.Failure(e: Exception) => fail(e)
        case scala.util.Failure(e)            => throw e // Throwables
        case scala.util.Success(e)            => succ(e)
      fua

    def addEffects(f: Try[A] => Unit)(using EC): Future[A] =
      fua.onComplete(f)
      fua

    def addEffectAnyway(inAnyCase: => Unit)(using EC): Future[A] =
      fua.onComplete: _ =>
        inAnyCase
      fua

    private def recoverDefaultMonitor: Exception => Unit      = e => println(s"Future.recoverDefault $e")
    def recoverDefault(using EC)(using z: Zero[A]): Future[A] = recoverDefault(z.zero)
    def recoverDefault(using EC)(default: => A): Future[A]    = recoverDefault(default)(recoverDefaultMonitor)
    def recoverDefault(monitor: Exception => Unit)(using EC)(using z: Zero[A]): Future[A] =
      recoverDefault(z.zero)(monitor)
    def recoverDefault(default: => A)(monitor: Exception => Unit)(using EC): Future[A] =
      fua.recover:
        case e: Exception =>
          monitor(e)
          default

    def withTimeout(
        duration: FiniteDuration,
        error: => String
    )(using EC, FutureAfter): Future[A] =
      withTimeoutError(duration, new TimeoutException(s"$error timeout after $duration"))

    def withTimeoutError(
        duration: FiniteDuration,
        error: => Exception & util.control.NoStackTrace
    )(using EC)(using after: FutureAfter): Future[A] =
      Future.firstCompletedOf(
        Seq(
          fua,
          after(duration)(() => Future.failed(error))
        )
      )

    def withTimeoutDefault(
        duration: FiniteDuration,
        default: => A
    )(using EC)(using after: FutureAfter): Future[A] =
      Future.firstCompletedOf(
        Seq(
          fua,
          after(duration)(() => Future(default))
        )
      )

  extension [A](fua: Future[Option[A]])

    def orFail(msg: => String)(using EC): Future[A] =
      fua.flatMap:
        _.fold[Future[A]](Future.failed(new Exception(msg)))(Future.successful)

    def orFailWith(err: => Exception)(using EC): Future[A] =
      fua.flatMap:
        _.fold[Future[A]](Future.failed(err))(Future.successful)

    def orElse(other: => Future[Option[A]])(using EC): Future[Option[A]] =
      fua.flatMap:
        _.fold(other): x =>
          Future.successful(Some(x))

    def getOrElse(other: => Future[A])(using EC): Future[A] = fua.flatMap { _.fold(other)(Future.successful) }
    def orZeroFu(using z: Zero[A]): Future[A]               = fua.map(_.getOrElse(z.zero))(using EC.parasitic)

    def map2[B](f: A => B)(using EC): Future[Option[B]] = fua.map(_.map(f))
    def dmap2[B](f: A => B): Future[Option[B]]          = fua.map(_.map(f))(using EC.parasitic)

    def getIfPresent: Option[A] =
      fua.value match
        case Some(scala.util.Success(v)) => v
        case _                           => None

    def mapz[B: Zero](fb: A => B)(using EC): Future[B]                    = fua.map { _.so(fb) }
    infix def flatMapz[B: Zero](fub: A => Future[B])(using EC): Future[B] = fua.flatMap { _.so(fub) }
