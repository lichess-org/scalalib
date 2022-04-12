package ornicar.scalalib

import cats.data.Validated

trait Common {
  implicit final def toOrnicarAddKcombinator[A](any: A): OrnicarAddKcombinator[A] =
    new OrnicarAddKcombinator(any)
  implicit final def toOrnicarMap[A, B](m: Map[A, B]): OrnicarMap[A, B] =
    new OrnicarMap(m)
  implicit final def toOrnicarIdentity[A](a: A): OrnicarIdentity[A] =
    new OrnicarIdentity(a)
  implicit final def toOrnicarValidated[E, A](a: Validated[E, A]): OrnicarValidated[E, A] =
    new OrnicarValidated(a)
}

/** K combinator implementation Provides oneliner side effects See
  * https://web.archive.org/web/20111209063845/hacking-scala.posterous.com/side-effecting-without-braces
  */
final class OrnicarAddKcombinator[A](private val any: A) extends AnyVal {
  def kCombinator(sideEffect: A => Unit): A = {
    sideEffect(any)
    any
  }
  def ~(sideEffect: A => Unit): A = kCombinator(sideEffect)
  def pp: A                       = kCombinator(println)
  def pp(msg: String): A          = kCombinator(a => println(s"[$msg] $a"))
}

final class OrnicarMap[A, B](private val m: Map[A, B]) extends AnyVal {

  // Add Map.mapKeys, similar to Map.mapValues
  def mapKeys[C](f: A => C): Map[C, B] =
    m map { case (a, b) =>
      (f(a), b)
    } toMap

  // Add Map.filterValues, similar to Map.filterKeys
  def filterValues(p: B => Boolean): Map[A, B] = m filter { x =>
    p(x._2)
  }
}

final class OrnicarIdentity[A](private val a: A) extends AnyVal {

  def combine[B](o: Option[B])(f: (A, B) => A): A = o match {
    case None    => a
    case Some(b) => f(a, b)
  }
}

final class OrnicarValidated[E, A](validated: Validated[E, A]) {

  def flatMap[EE >: E, B](f: A => Validated[EE, B]): Validated[EE, B] = validated.andThen(f)
}
