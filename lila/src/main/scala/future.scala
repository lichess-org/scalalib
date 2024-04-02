package scalalib
package future

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext as EC
import scala.util.control.NoStackTrace
import scala.util.Try
import alleycats.Zero

/** Returns a [[scala.concurrent.Future]] that will be completed with the success or failure of the provided
  * value after the specified duration. Implementation can be done with or without akka/pekko
  */
type FutureAfter = [T] => (FiniteDuration) => (() => Future[T]) => Future[T]

final class TimeoutException(msg: String) extends Exception(msg) with NoStackTrace

object FutureExtension:
  extension [A](fua: Future[A])

    inline def dmap[B](f: A => B): Future[B]   = fua.map(f)(EC.parasitic)
    inline def dforeach[B](f: A => Unit): Unit = fua.foreach(f)(EC.parasitic)

    def andDo(sideEffect: => Unit)(using EC): Future[A] =
      fua.andThen:
        case _ => sideEffect

    inline def void: Future[Unit] =
      fua.dmap(_ => ())

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

    def recoverDefault(using EC)(using z: Zero[A]): Future[A] = recoverDefault(z.zero)

    def recoverDefault(using EC)(default: => A): Future[A] =
      fua.recover:
        case _: java.util.concurrent.TimeoutException => default
        case e: Exception =>
          println(s"Future.recoverDefault $e")
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
