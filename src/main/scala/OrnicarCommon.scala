package ornicar.scalalib

import scala.util.matching.Regex

trait OrnicarCommon {
  /**
   * K combinator implementation
   * Provides oneliner side effects
   * See http://hacking-scala.posterous.com/side-effecting-without-braces
   */
  implicit def addKcombinator[A](any: A) = new {
    def kCombinator(sideEffect: A ⇒ Unit): A = {
      sideEffect(any)
      any
    }
    def ~(sideEffect: A ⇒ Unit): A = kCombinator(sideEffect)
    def pp: A = kCombinator(println)
  }

  implicit def richMap[A, B](m: Map[A, B]) = new {

    // Add Map.mapKeys, similar to Map.mapValues
    def mapKeys[C](f: A ⇒ C): Map[C, B] = m map {
      case (a, b) ⇒ (f(a), b)
    } toMap

    // Add Map.filterValues, similar to Map.filterKeys
    def filterValues(p: B ⇒ Boolean): Map[A, B] = m filter { x ⇒ p(x._2) }
  }

  def exit(msg: Any): Nothing = {
    println(msg)
    sys.exit()
  }
}
