package ornicar.scalalib

import scala.util.matching.Regex

trait OrnicarCommon {
  /**
   * K combinator implementation
   * Provides oneliner side effects
   * See http://hacking-scala.posterous.com/side-effecting-without-braces
   */
  implicit def addKcombinator[A](any: A) = new {
    def kCombinator(sideEffect: A => Unit): A = {
      sideEffect(any)
      any
    }
    def ~(sideEffect: A => Unit): A = kCombinator(sideEffect)
  }

  // Add Map.mapKeys, similar to Map.mapValues
  implicit def addMapKeys[A, B](m: Map[A, B]) = new {
    def mapKeys[C](f: A => C): Map[C, B] = m map {
      case (a, b) => (f(a), b)
    } toMap
  }

  // Pimp regex library
  implicit def regexToRichRegex(r: Regex) = new {
    def matches(s: String) = r.pattern.matcher(s).matches
  }

  def exit(msg: Any): Nothing = {
    println(msg)
    sys.exit()
  }
}
