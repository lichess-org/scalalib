package ornicar.scalalib

import scala.util.matching.{ Regex => ScalaRegex }

trait Regex {

  implicit final class oanicarRegex(r: ScalaRegex) {

    def matches(s: String): Boolean = 
      r.pattern.matcher(s).matches
  }
}
