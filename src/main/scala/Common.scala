package ornicar.scalalib

import scalaz.{ Functor, Monoid }

trait Common {
  @inline implicit def toOrnicarFunctor[M[_]: Functor, A](fa: M[A]) = new ornicarFunctor(fa)
  @inline implicit def toOrnicarAddKcombinator[A](any: A) = new ornicarAddKcombinator(any)
  @inline implicit def toOrnicarRichMap[A, B](m: Map[A, B]) = new ornicarRichMap(m)
  @inline implicit def toOrnicarRichIdentity[A](a: A) = new ornicarRichIdentity(a)
}

final class ornicarFunctor[M[_]: Functor, A](fa: M[A]) {
  def map2[N[_], B, C](f: B ⇒ C)(implicit m: A <:< N[B], f1: Functor[M], f2: Functor[N]): M[N[C]] =
    f1.map(fa) { k ⇒ f2.map(k: N[B])(f) }
}

/**
 * K combinator implementation
 * Provides oneliner side effects
 * See http://hacking-scala.posterous.com/side-effecting-without-braces
 */
final class ornicarAddKcombinator[A](private val any: A) extends AnyVal {
  def kCombinator(sideEffect: A ⇒ Unit): A = {
    sideEffect(any)
    any
  }
  def ~(sideEffect: A ⇒ Unit): A = kCombinator(sideEffect)
  def pp: A = kCombinator(println)
  def pp(msg: String): A = kCombinator(a ⇒ println(s"[$msg] $a"))
}

final class ornicarRichMap[A, B](private val m: Map[A, B]) extends AnyVal {

  // Add Map.mapKeys, similar to Map.mapValues
  def mapKeys[C](f: A ⇒ C): Map[C, B] = m map {
    case (a, b) ⇒ (f(a), b)
  } toMap

  // Add Map.filterValues, similar to Map.filterKeys
  def filterValues(p: B ⇒ Boolean): Map[A, B] = m filter { x ⇒ p(x._2) }
}

final class ornicarRichIdentity[A](private val a: A) extends AnyVal {

  def combine[B](o: Option[B])(f: (A, B) ⇒ A): A = o match {
    case None    ⇒ a
    case Some(b) ⇒ f(a, b)
  }
}