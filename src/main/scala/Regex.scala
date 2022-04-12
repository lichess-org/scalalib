package ornicar.scalalib

import scala.util.matching.{ Regex => ScalaRegex }

trait Regex {
  @inline implicit def toOrnicarRegex(r: ScalaRegex): OrnicarRegexWrapper = new OrnicarRegexWrapper(r)
}

object Regex extends Regex

final class OrnicarRegexWrapper(private val r: ScalaRegex) extends AnyVal {

  def find(s: String): Boolean =
    r.pattern.matcher(s).find

  def matches(s: String): Boolean =
    r.pattern.matcher(s).matches
}
