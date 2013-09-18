package ornicar.scalalib

import util.control.Exception.allCatch
import scalaz._
import Scalaz._

trait Validation extends ValidationFunctions {

  type Failures = NonEmptyList[String]

  type Valid[A] = scalaz.Validation[Failures, A]

  implicit def ornicarEitherToValidation[E, B](either: Either[E, B]): Valid[B] =
    fromEither(either.left map makeFailures)

  implicit final class ornicarRichValidation[E, A](validation: scalaz.Validation[E, A]) {

    def mapFail[F](f: E ⇒ F): scalaz.Validation[F, A] = validation match {
      case Success(s) ⇒ Success(s)
      case Failure(s) ⇒ Failure(f(s))
    }

    def flatOption[B](f: A ⇒ Option[B]): Option[B] = validation.toOption flatMap f

    def toValid(implicit f: E ⇒ Any = identity _): Valid[A] = mapFail(makeFailures _ compose f)

    def toValid(v: ⇒ Any): Valid[A] = mapFail(_ ⇒ makeFailures(v))
  }

  implicit final class ornicarRichValid[A](valid: Valid[A]) {

    def and[B](f: Valid[A ⇒ B])(implicit a: Apply[Valid]): Valid[B] = valid <*> f

    def err: A = valid match {
      case Success(a) ⇒ a
      case Failure(e) ⇒ throw new RuntimeException(e.shows)
    }

    def mapFailures[F](f: String ⇒ F) = valid mapFail (_ map f)

    def prefixFailuresWith(prefix: String): Valid[A] = mapFailures(prefix ++ _)
  }

  implicit final class ornicarRichOption[A](option: Option[A]) {

    def toValid(v: ⇒ Any): Valid[A] = ornicarEitherToValidation(option toRight v)
  }

  implicit final class ornicarMkValid[A](a: ⇒ A) {

    def validIf(cond: Boolean, failure: String): Valid[A] =
      if (cond) Success(a) else Failure(failure wrapNel)

    def validIf(cond: A ⇒ Boolean, failure: String): Valid[A] =
      if (cond(a)) Success(a) else Failure(failure wrapNel)
  }

  implicit def ornicarFailuresShow: Show[Failures] = Show.show {
    (fs: Failures) ⇒ fs.list mkString "\n"
  }

  def validateOption[A, B](ao: Option[A])(op: A ⇒ Valid[B]): Valid[Option[B]] =
    ao.fold(success(none[B]): Valid[Option[B]])(a ⇒ op(a) map some)

  def sequenceValid[A](as: List[Valid[A]]): Valid[List[A]] =
    as.sequence[({ type λ[α] = Valid[α] })#λ, A]

  def printLnFailures(failures: Failures) {
    println(failures.shows)
  }

  // courtesy of https://github.com/jlcanela
  implicit def ValidSemigroup[A: Semigroup]: Semigroup[Valid[A]] =
    scalaz.Semigroup.instance { (x, y) ⇒ (x |@| y)(_ |+| _) }

  private def makeFailures(e: Any): Failures = e match {
    case e: Throwable       ⇒ e.getMessage wrapNel
    case m: NonEmptyList[_] ⇒ m map (_.toString)
    case s                  ⇒ s.toString wrapNel
  }

  def unsafe[A](op: ⇒ A)(implicit handler: Throwable ⇒ Failures = exceptionToFailures.message): Valid[A] =
    fromEither((allCatch either op).left map handler)

  object exceptionToFailures {

    def prefixedMessage(msg: String, t: Throwable): Failures = msg <:: message(t)

    def message(t: Throwable): Failures = t.getMessage.wrapNel

    def stackTrace(t: Throwable): Failures = {

      val buff = new java.io.StringWriter()
      val w = new java.io.PrintWriter(buff)

      try {
        t.printStackTrace(w)
        w.flush()

        buff.toString wrapNel
      }
      catch {
        case _: Exception ⇒ t.getMessage wrapNel
      }
      finally {
        try {
          w.close()
        }
      }
    }

    def messageAndStacktrace(t: Throwable): Failures =
      t.getMessage <:: stackTrace(t)
  }
}
