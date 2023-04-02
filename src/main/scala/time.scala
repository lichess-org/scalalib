package ornicar.scalalib

import java.time.{ Duration, Instant, LocalDateTime, ZoneOffset }
import java.time.temporal.{ ChronoUnit, TemporalAdjuster, TemporalAdjusters }
import scala.concurrent.duration as concDur

object time:

  val utcZone = ZoneOffset.UTC

  extension (d: LocalDateTime)
    def toMillis: Long                               = d.toInstant(utcZone).toEpochMilli
    def toSeconds: Long                              = toMillis / 1000
    def toCentis: Long                               = toMillis / 10
    def toNow: Duration                              = Duration.between(d, LocalDateTime.now)
    def isBeforeNow: Boolean                         = d.isBefore(LocalDateTime.now)
    def isAfterNow: Boolean                          = d.isAfter(LocalDateTime.now)
    def atMost(other: LocalDateTime): LocalDateTime  = if other.isBefore(d) then other else d
    def atLeast(other: LocalDateTime): LocalDateTime = if other.isAfter(d) then other else d
    def withTimeAtStartOfDay: LocalDateTime          = d.toLocalDate.atStartOfDay
    def plus(dur: concDur.Duration): LocalDateTime   = d.plus(dur.toMillis, ChronoUnit.MILLIS)
    def minus(dur: concDur.Duration): LocalDateTime  = d.minus(dur.toMillis, ChronoUnit.MILLIS)

  case class TimeInterval(start: LocalDateTime, end: LocalDateTime):
    def overlaps(other: TimeInterval): Boolean = start.isBefore(other.end) && other.start.isBefore(end)
    def contains(date: LocalDateTime): Boolean = (start == date || start.isBefore(date)) && end.isAfter(date)

  object TimeInterval:
    def apply(start: LocalDateTime, duration: Duration): TimeInterval =
      TimeInterval(start, start.plus(duration))

  def millisToDate(millis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), utcZone)

  def daysBetween(from: LocalDateTime, to: LocalDateTime): Int =
    ChronoUnit.DAYS.between(from, to).toInt

  val isoDateFormatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME
