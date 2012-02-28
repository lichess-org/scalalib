package ornicar.scalalib

import org.joda.time.DateTime

trait OrnicarDateTime {

  implicit def richDateTime(date: DateTime) = new {
    def getSeconds: Long = date.getMillis / 1000
  }
}
