package ornicar.scalalib

import org.joda.time.DateTime
import java.util.Date

trait OrnicarDateTime {

  implicit def richDateTime(date: DateTime) = new {
    def getSeconds: Long = date.getMillis / 1000
    def getDate: Date = date.toDate
  }
}
