package ornicar.scalalib

import scala.util.matching.{ Regex => ScalaRegex }

trait Regex {
  @inline implicit def toOrnicarRegex(r: ScalaRegex) = new ornicarRegexWrapper(r)
}

final class ornicarRegexWrapper(private val r: ScalaRegex) extends AnyVal {

  def matches(s: String): Boolean =
    r.pattern.matcher(s).matches
}
