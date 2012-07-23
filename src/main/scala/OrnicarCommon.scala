package ornicar.scalalib

trait OrnicarCommon {
  /**
   * K combinator implementation
   * Provides oneliner side effects
   * See http://hacking-scala.posterous.com/side-effecting-without-braces
   */
  implicit def ornicarAddKcombinator[A](any: A) = new {
    def kCombinator(sideEffect: A ⇒ Unit): A = {
      sideEffect(any)
      any
    }
    def ~(sideEffect: A ⇒ Unit): A = kCombinator(sideEffect)
    def pp: A = kCombinator(println)
  }

  implicit def ornicarRichMap[A, B](m: Map[A, B]) = new {

    // Add Map.mapKeys, similar to Map.mapValues
    def mapKeys[C](f: A ⇒ C): Map[C, B] = m map {
      case (a, b) ⇒ (f(a), b)
    } toMap

    // Add Map.filterValues, similar to Map.filterKeys
    def filterValues(p: B ⇒ Boolean): Map[A, B] = m filter { x ⇒ p(x._2) }
  }

  implicit def ornicarRichIdentity[A](a: A) = new {

    def combine[B](o: Option[B], f: (A, B) ⇒ A): A = o match {
      case None    ⇒ a
      case Some(b) ⇒ f(a, b)
    }
  }

  def exit(msg: Any): Nothing = {
    println(msg)
    sys.exit()
  }
}
