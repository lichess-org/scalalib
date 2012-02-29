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
    validation(either.left map makeFailures)

  implicit def richValid[A](valid: Valid[A]) = new {

    def and[B](f: Valid[A ⇒ B])(implicit a: Apply[Valid]): Valid[B] = valid <*> f
  }

  implicit def richValidation[E, A](validation: Validation[E, A]) = new {

    def mapFail[F](f: E ⇒ F): Validation[F, A] = validation match {
      case Success(s) ⇒ Success(s)
      case Failure(s) ⇒ Failure(f(s))
    }

    def flatOption[B](f: A ⇒ Option[B]): Option[B] = validation.toOption flatMap f

    def toValid(implicit f: E ⇒ Any = identity _): Valid[A] = mapFail(makeFailures _ compose f)

    def toValid(v: ⇒ Any): Valid[A] = mapFail(_ ⇒ makeFailures(v))
  }

  implicit def richOption[A](option: Option[A]) = new {

    def toValid(v: => Any): Valid[A] = eitherToValidation(option toRight v)
  }

  def unsafe[A](op: ⇒ A)(implicit handle: Throwable ⇒ String = _.getMessage): Valid[A] =
    eitherToValidation((allCatch either op).left map handle)

  def validateOption[A, B](ao: Option[A])(op: A ⇒ Valid[B]): Valid[Option[B]] =
    ao.fold(a ⇒ op(a) map some, success(none))

  def sequenceValid[A](as: List[Valid[A]]): Valid[List[A]] =
    as.sequence[({ type λ[α] = Valid[α] })#λ, A]

  private def makeFailures(e: Any): Failures = e match {
    case e: Throwable       ⇒ e.getMessage wrapNel
    case m: NonEmptyList[_] ⇒ m map (_.toString)
    case s                  ⇒ s.toString wrapNel
  }
}
