package ornicar.scalalib

import org.joda.time.{ DateTime => JDateTime }

trait DateTime {

  implicit def richDateTime(date: JDateTime) = new {
    def getSeconds: Int = math.round(date.getMillis / 1000)
  }
}
