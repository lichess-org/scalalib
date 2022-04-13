package ornicar.scalalib

import cats.data.Validated

/** K combinator implementation Provides oneliner side effects See
  * https://web.archive.org/web/20111209063845/hacking-scala.posterous.com/side-effecting-without-braces
  */
extension [A](a: A)
  def kCombinator(sideEffect: A => Unit): A = {
    sideEffect(a)
    a
  }
  def ~(sideEffect: A => Unit): A = kCombinator(sideEffect)
  def pp: A                       = kCombinator(println)
  def pp(msg: String): A          = kCombinator(a => println(s"[$msg] $a"))
  def combine[B](o: Option[B])(f: (A, B) => A): A = o match {
    case None    => a
    case Some(b) => f(a, b)
  }

extension [A, B](m: Map[A, B])
  // Add Map.mapKeys, similar to Map.mapValues
  def mapKeys[C](f: A => C): Map[C, B] =
    m map { case (a, b) =>
      (f(a), b)
    } toMap
  // Add Map.filterValues, similar to Map.filterKeys
  def filterValues(p: B => Boolean): Map[A, B] = m filter { x =>
    p(x._2)
  }

extension [E, A](validated: Validated[E, A])
  def flatMap[EE >: E, B](f: A => Validated[EE, B]): Validated[EE, B] = validated.andThen(f)
