package ornicar.scalalib

import org.joda.time.DateTime

trait OrnicarDateTime {

  implicit def richDateTime(date: DateTime) = new {
    def getSeconds: Int = math.round(date.getMillis / 1000)
  }
}
