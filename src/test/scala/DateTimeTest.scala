package ornicar.scalalib

import org.joda.time.{ DateTime ⇒ JodaDateTime }

import org.specs2.mutable.Specification

class DateTimeTest
    extends Specification
    with test.ValidationMatchers
    with DateTime {

  "datetime" should {
    val ts = 1330444640000l
    val date = new JodaDateTime(ts)
    "convert to seconds" in {
      date.getSeconds must_== 1330444640l
    }
  }
}
