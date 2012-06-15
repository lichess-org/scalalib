package ornicar.scalalib

import scala.util.matching.Regex

trait OrnicarRegex {

  implicit def richRegex(r: Regex) = new {

    def matches(s: String): Boolean = 
      r.pattern.matcher(s).matches
  }
}
