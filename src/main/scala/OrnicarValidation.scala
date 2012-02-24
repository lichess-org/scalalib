package ornicar.scalalib

import util.control.Exception.allCatch
import scalaz.{ Validation ⇒ ScalazValidation, Success, Failure, Semigroup, Apply, NonEmptyList }

trait OrnicarValidation
    extends scalaz.Validations
    with scalaz.Semigroups
    with scalaz.Options
    with scalaz.MABs
    with scalaz.Identitys {

  type Failures = NonEmptyList[String]

  type Valid[A] = ScalazValidation[Failures, A]

  implicit def eitherToValidation[E, B](either: Either[E, B]): Valid[B] =
    validation(either.left map {
      case e: Throwable       ⇒ throwableToFailures(e)
      case s: String          ⇒ stringToFailures(s)
      case m: NonEmptyList[_] ⇒ m map (_.toString)
    })

  implicit def stringToFailures(s: String): Failures = s wrapNel

  implicit def throwableToFailures(e: Throwable): Failures = e.getMessage wrapNel

  implicit def richStringToFailures(str: String) = new {
    def toFailures: Failures = stringToFailures(str)
  }

  implicit def richScalazValidation[E, A](validation: ScalazValidation[E, A]) = new {

    def and[B](f: ScalazValidation[E, A ⇒ B])
    (implicit a: Apply[({ type λ[α] = ScalazValidation[E, α] })#λ])
    : ScalazValidation[E, B] = validation <*> f

    def mapFail[F](f: E => F): ScalazValidation[F, A] = validation match {
      case Success(s) => Success(s)
      case Failure(s) => Failure(f(s))
    }
  }

  def unsafe[A](op: ⇒ A)(implicit handle: Throwable ⇒ Failures = throwableToFailures): Valid[A] =
    eitherToValidation((allCatch either op).left map handle)

  def validateOption[A, B](ao: Option[A])(op: A ⇒ Valid[B]): Valid[Option[B]] =
    ao some { a ⇒ op(a) map (_.some) } none success(none)

  def sequenceValid[A](as: List[Valid[A]]): Valid[List[A]] =
    as.sequence[({ type λ[α] = Valid[α] })#λ, A]
}
