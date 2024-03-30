package ornicar.scalalib
package future

import scala.concurrent.Future
import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext as EC
import scala.util.control.NoStackTrace

/** Returns a [[scala.concurrent.Future]] that will be completed with the success or failure of the provided
  * value after the specified duration. Implementation can be done with or without akka/pekko
  */
type FutureAfter = [T] => (FiniteDuration) => (() => Future[T]) => Future[T]

final class TimeoutException(msg: String) extends Exception(msg) with NoStackTrace

object FutureExtension:
  extension [A](fua: Future[A])

    inline def dmap[B](f: A => B): Future[B]   = fua.map(f)(EC.parasitic)
    inline def dforeach[B](f: A => Unit): Unit = fua.foreach(f)(EC.parasitic)

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
