package ornicar.scalalib.test

import org.specs2.matcher.{ Matcher, MustMatchers }
import ornicar.scalalib.Validation

trait ValidationMatchers
    extends ScalazValidationMatchers
    with Validation
    with MustMatchers {

  def haveFailures(nb: Int): Matcher[Valid[_]] =
    beFailure.like {
      case e ⇒ e.list.size mustEqual nb
    }

  def haveFailureMatching(m: String): Matcher[Valid[_]] =
    beFailure.like {
      case e ⇒ e.list must containMatch(m)
    }
}
