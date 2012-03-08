package ornicar.scalalib.test

import org.specs2.matcher._
import org.specs2.execute.{ Failure ⇒ SpecFailure, Success ⇒ SpecSuccess, Result ⇒ SpecResult }
import util.control.Exception.allCatch
import scalaz.effects._

trait ScalazIOMatchers extends MatchersImplicits {

  /** matcher for an IO */
  def beIO[A](t: ⇒ A) = new Matcher[IO[A]] {

    def apply[S <: IO[A]](value: Expectable[S]) = {
      val expected = t
      (allCatch either { value.value.unsafePerformIO }).fold(
        e ⇒ result(false,
          "IO fails with " + e,
          "IO fails with " + e,
          value),
        a ⇒ result(a == expected,
          a + " is IO with value " + expected,
          a + " is not IO with value " + expected,
          value)
      )
    }
  }

  def beIO[A] = new Matcher[IO[A]] {

    def apply[S <: IO[A]](value: Expectable[S]) = {
      val performed = allCatch either { value.value.unsafePerformIO }
      result(performed.isRight,
        "IO perfoms successfully",
        "IO fails",
        value)
    }

    def like(f: PartialFunction[A, MatchResult[_]]) = this and partialMatcher(f)

    private def partialMatcher(
      f: PartialFunction[A, MatchResult[_]]) = new Matcher[IO[A]] {

      def apply[S <: IO[A]](value: Expectable[S]) = {
        (allCatch either { value.value.unsafePerformIO }).fold(
          e ⇒ result(false,
            "IO fails with " + e,
            "IO fails with " + e,
            value),
          a ⇒ {
            val res: SpecResult = a match {
              case t if f.isDefinedAt(t) ⇒ f(t).toResult
              case _                     ⇒ SpecFailure("function undefined")
            }
            result(res.isSuccess,
              a + " is IO[A] and " + res.message,
              a + " is not IO[A] with value " + res.message,
              value)
          }
        )
      }
    }
  }
}
