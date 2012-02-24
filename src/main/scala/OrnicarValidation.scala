package ornicar.scalalib

import util.control.Exception.allCatch
import scalaz.{ Validation, Success, Failure, Semigroup, Apply, NonEmptyList }

trait OrnicarValidation
    extends scalaz.Validations
    with scalaz.Options
    with scalaz.MABs
    with scalaz.Identitys {

  type Failures = NonEmptyList[String]

  type Valid[A] = Validation[Failures, A]

  implicit def eitherToValidation[E, B](either: Either[E, B]): Valid[B] =
    validation(either.left map {
      case e: Throwable       ⇒ e.getMessage wrapNel
      case m: NonEmptyList[_] ⇒ m map (_.toString)
      case s                  ⇒ s.toString wrapNel
    })

  implicit def richString(str: String) = new {
    def toFailures: Failures = str wrapNel
  }

  implicit def richValidation[E, A](validation: Validation[E, A]) = new {

    def and[B](f: Validation[E, A ⇒ B])(implicit a: Apply[({ type λ[α] = Validation[E, α] })#λ]): Validation[E, B] = validation <*> f

    def mapFail[F](f: E ⇒ F): Validation[F, A] = validation match {
      case Success(s) ⇒ Success(s)
      case Failure(s) ⇒ Failure(f(s))
    }
  }

  def unsafe[A](op: ⇒ A)(implicit handle: Throwable ⇒ String = _.getMessage): Valid[A] =
    eitherToValidation((allCatch either op).left map handle)

  def validateOption[A, B](ao: Option[A])(op: A ⇒ Valid[B]): Valid[Option[B]] =
    ao some { a ⇒ op(a) map (_.some) } none success(none)

  def sequenceValid[A](as: List[Valid[A]]): Valid[List[A]] =
    as.sequence[({ type λ[α] = Valid[α] })#λ, A]
}
