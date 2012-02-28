package ornicar.scalalib

import org.joda.time.DateTime

class OrnicarDateTimeTest extends OrnicarTest with OrnicarDateTime {

  "datetime" should {
    val ts = 1330444640000l
    val date = new DateTime(ts)
    "convert to seconds" in {
      date.getSeconds must_== 1330444640l
    }
  }
}
