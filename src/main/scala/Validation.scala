package ornicar.scalalib

import util.control.Exception.allCatch
import scalaz.{ Success, Failure, Semigroup, Apply, NonEmptyList, effects, Show, Validation ⇒ ScalazValidation }

trait Validation
    extends scalaz.Validations
    with scalaz.Options
    with scalaz.MABs
    with scalaz.Identitys
    with scalaz.Semigroups
    with scalaz.NonEmptyLists {

  type Failures = NonEmptyList[String]

  type Valid[A] = ScalazValidation[Failures, A]

  implicit def ornicarEitherToValidation[E, B](either: Either[E, B]): Valid[B] =
    validation(either.left map makeFailures)

  implicit def ornicarRichValidation[E, A](validation: ScalazValidation[E, A]) = new {

    def mapFail[F](f: E ⇒ F): ScalazValidation[F, A] = validation match {
      case Success(s) ⇒ Success(s)
      case Failure(s) ⇒ Failure(f(s))
    }

    def flatOption[B](f: A ⇒ Option[B]): Option[B] = validation.toOption flatMap f

    def toValid(implicit f: E ⇒ Any = identity _): Valid[A] = mapFail(makeFailures _ compose f)

    def toValid(v: ⇒ Any): Valid[A] = mapFail(_ ⇒ makeFailures(v))
  }

  implicit def ornicarRichValid[A](valid: Valid[A]) = new {

    def and[B](f: Valid[A ⇒ B])(implicit a: Apply[Valid]): Valid[B] = valid <*> f

    def err: A = valid match {
      case Success(a) ⇒ a
      case Failure(e) ⇒ throw new RuntimeException(e.shows)
    }

    def mapFailures[F](f: String ⇒ F) = valid mapFail (_ map f)

    def prefixFailuresWith(prefix: String): Valid[A] = mapFailures(prefix ++ _)
  }

  implicit def ornicarRichOption[A](option: Option[A]) = new {

    def toValid(v: ⇒ Any): Valid[A] = ornicarEitherToValidation(option toRight v)
  }

  implicit def ornicarMkValid[A](a: ⇒ A) = new {

    def validIf(cond: Boolean, failure: String): Valid[A] =
      if (cond) Success(a) else Failure(failure wrapNel)

    def validIf(cond: A ⇒ Boolean, failure: String): Valid[A] =
      if (cond(a)) Success(a) else Failure(failure wrapNel)
  }

  implicit def ornicarFailuresShow: Show[Failures] = new Show[Failures] {
    def show(fs: Failures) = (fs.list mkString "\n").toList
  }

  def validateOption[A, B](ao: Option[A])(op: A ⇒ Valid[B]): Valid[Option[B]] =
    ao.fold(a ⇒ op(a) map some, success(none))

  def sequenceValid[A](as: List[Valid[A]]): Valid[List[A]] =
    as.sequence[({ type λ[α] = Valid[α] })#λ, A]

  def printLnFailures(failures: Failures) {
    println(failures.shows)
  }

  def putFailures(failures: Failures): effects.IO[Unit] =
    effects.putStrLn(failures.shows)

  // courtesy of https://github.com/jlcanela
  implicit def ValidSemigroup[A: Semigroup]: Semigroup[Valid[A]] =
    semigroup { (x, y) ⇒ (x |@| y)(_ |+| _) }

  private def makeFailures(e: Any): Failures = e match {
    case e: Throwable       ⇒ e.getMessage wrapNel
    case m: NonEmptyList[_] ⇒ m map (_.toString)
    case s                  ⇒ s.toString wrapNel
  }

  def unsafe[A](op: ⇒ A)(implicit handler: Throwable ⇒ Failures = exceptionToFailures.message): Valid[A] =
    validation((allCatch either op).left map handler)

  object exceptionToFailures {

    implicit def message(t: Throwable): Failures = t.getMessage.wrapNel

    implicit def stackTrace(t: Throwable): Failures = {

      val buff = new java.io.StringWriter()
      val w = new java.io.PrintWriter(buff)

      try {
        t.printStackTrace(w)
        w.flush()

        buff.toString wrapNel
      }
      catch {
        case _ ⇒ t.getMessage wrapNel
      }
      finally {
        try {
          w.close()
        }
      }
    }

    implicit def messageAndStacktrace(t: Throwable): Failures =
      t.getMessage <:: stackTrace(t)
  }
}
