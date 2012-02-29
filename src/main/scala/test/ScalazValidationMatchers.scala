package ornicar.scalalib.test

import org.specs2.matcher._
import org.specs2.execute.{ Failure ⇒ SpecFailure, Success ⇒ SpecSuccess, Result ⇒ SpecResult }
import scalaz.{ Validation, Success, Failure }

trait ScalazValidationMatchers extends MatchersImplicits {

  /** success matcher for a Validation with a specific value */
  def succeedWith[E, A](a: ⇒ A) = validationWith[E, A](Success(a))

  /** success matcher for a Validation */
  def beSuccess[A](t: ⇒ A) = new Matcher[Validation[_, A]] {
    def apply[S <: Validation[_, A]](value: Expectable[S]) = {
      val expected = t
      result(value.value == Success(t),
        value.description + " is Success with value " + expected,
        value.description + " is not Success with value " + expected,
        value)
    }
  }
  def beSuccess[A] = new Matcher[Validation[_, A]] {
    def apply[S <: Validation[_, A]](value: Expectable[S]) = {
      result(value.value.isSuccess,
        value.description + " is Success",
        value.description + " is not Success",
        value)
    }

    def like(f: PartialFunction[A, MatchResult[_]]) = this and partialMatcher(f)

    private def partialMatcher(f: PartialFunction[A, MatchResult[_]]) = new Matcher[Validation[_, A]] {
      def apply[S <: Validation[_, A]](value: Expectable[S]) = {
        val res: SpecResult = value.value match {
          case Success(t) if f.isDefinedAt(t)  ⇒ f(t).toResult
          case Success(t) if !f.isDefinedAt(t) ⇒ SpecFailure("function undefined")
          case other                           ⇒ SpecFailure("no match")
        }
        result(res.isSuccess,
          value.description + " is Success[A] and " + res.message,
          value.description + " is Success[A] but " + res.message,
          value)
      }
    }
  }

  /** failure matcher for a Validation with a specific value */
  def failWith[E, A](e: ⇒ E) = validationWith[E, A](Failure(e))

  /** failure matcher for a Validation */
  def beFailure[E](t: ⇒ E) = new Matcher[Validation[E, _]] {
    def apply[S <: Validation[E, _]](value: Expectable[S]) = {
      val expected = t
      result(value.value == Failure(t),
        value.description + " is Failure with value " + expected,
        value.description + " is not Failure with value " + expected,
        value)
    }
  }
  def beFailure[E] = new Matcher[Validation[E, _]] {
    def apply[S <: Validation[E, _]](value: Expectable[S]) = {
      result(value.value.isFailure,
        value.description + " is Failure",
        value.description + " is not Failure",
        value)
    }

    def like(f: PartialFunction[E, MatchResult[_]]) = this and partialMatcher(f)

    private def partialMatcher(f: PartialFunction[E, MatchResult[_]]) = new Matcher[Validation[E, _]] {
      def apply[S <: Validation[E, _]](value: Expectable[S]) = {
        val res: SpecResult = value.value match {
          case Failure(t) if f.isDefinedAt(t)  ⇒ f(t).toResult
          case Failure(t) if !f.isDefinedAt(t) ⇒ SpecFailure("function undefined")
          case other                           ⇒ SpecFailure("no match")
        }
        result(res.isSuccess,
          value.description + " is Failure[A] and " + res.message,
          value.description + " is Failure[A] but " + res.message,
          value)
      }
    }
  }

  private def validationWith[E, A](f: ⇒ Validation[E, A]): Matcher[Validation[E, A]] = (v: Validation[E, A]) ⇒ {
    val expected = f
    (expected == v, v + " is a " + expected, v + " is not a " + expected)
  }
}
