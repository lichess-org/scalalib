package ornicar.scalalib

import scala.util.matching.Regex

extension (r: Regex)
  def find(s: String): Boolean =
    r.pattern.matcher(s).find
  def matches(s: String): Boolean =
    r.pattern.matcher(s).matches
