package scalalib

import java.time.{ Duration, Instant, LocalDate, LocalDateTime, ZoneOffset }
import java.time.temporal.{ ChronoUnit, TemporalAdjuster }
import scala.concurrent.duration as concDur
import java.time.format.DateTimeFormatter

// about java.time https://stackoverflow.com/a/32443004
object time:

  val utcZone = ZoneOffset.UTC

  extension (d: LocalDate) def adjust(a: TemporalAdjuster): LocalDate = d.`with`(a)

  extension (d: LocalDateTime)
    def toMillis: Long                               = d.toInstant(utcZone).toEpochMilli
    def toSeconds: Long                              = toMillis / 1000
    def toCentis: Long                               = toMillis / 10
    def instant: Instant                             = d.toInstant(utcZone)
    def date: LocalDate                              = d.toLocalDate
    def toNow: Duration                              = instant.toNow
    def isBeforeNow: Boolean                         = d.isBefore(LocalDateTime.now)
    def isAfterNow: Boolean                          = d.isAfter(LocalDateTime.now)
    def atMost(other: LocalDateTime): LocalDateTime  = if other.isBefore(d) then other else d
    def atLeast(other: LocalDateTime): LocalDateTime = if other.isAfter(d) then other else d
    def withTimeAtStartOfDay: LocalDateTime          = d.toLocalDate.atStartOfDay
    def plus(dur: concDur.Duration): LocalDateTime   = d.plus(dur.toMillis, ChronoUnit.MILLIS)
    def minus(dur: concDur.Duration): LocalDateTime  = d.minus(dur.toMillis, ChronoUnit.MILLIS)
    def adjust(a: TemporalAdjuster): LocalDateTime   = d.`with`(a)

  extension (d: Instant)
    def toMillis: Long                        = d.toEpochMilli
    def toSeconds: Long                       = toMillis / 1000
    def toCentis: Long                        = toMillis / 10
    def date: LocalDate                       = LocalDate.ofInstant(d, utcZone)
    def dateTime: LocalDateTime               = LocalDateTime.ofInstant(d, utcZone)
    def toNow: Duration                       = Duration.between(d, Instant.now)
    def isBeforeNow: Boolean                  = d.isBefore(Instant.now)
    def isAfterNow: Boolean                   = d.isAfter(Instant.now)
    def atMost(other: Instant): Instant       = if other.isBefore(d) then other else d
    def atLeast(other: Instant): Instant      = if other.isAfter(d) then other else d
    def withTimeAtStartOfDay: Instant         = date.atStartOfDay(utcZone).toInstant
    def plus(dur: concDur.Duration): Instant  = d.plus(dur.toMillis, ChronoUnit.MILLIS)
    def minus(dur: concDur.Duration): Instant = d.minus(dur.toMillis, ChronoUnit.MILLIS)
    def plusMinutes(v: Int): Instant          = dateTime.plusMinutes(v).instant
    def minusMinutes(v: Int): Instant         = dateTime.minusMinutes(v).instant
    def plusHours(v: Int): Instant            = dateTime.plusHours(v).instant
    def minusHours(v: Int): Instant           = dateTime.minusHours(v).instant
    def plusDays(v: Int): Instant             = dateTime.plusDays(v).instant
    def minusDays(v: Int): Instant            = dateTime.minusDays(v).instant
    def plusWeeks(v: Int): Instant            = dateTime.plusWeeks(v).instant
    def minusWeeks(v: Int): Instant           = dateTime.minusWeeks(v).instant
    def plusMonths(v: Int): Instant           = dateTime.plusMonths(v).instant
    def minusMonths(v: Int): Instant          = dateTime.minusMonths(v).instant
    def plusYears(v: Int): Instant            = dateTime.plusYears(v).instant
    def minusYears(v: Int): Instant           = dateTime.minusYears(v).instant
    def adjust(a: TemporalAdjuster): Instant  = d.`with`(a)

  // DateTimeFormatter is very dangerous as it throws exceptions where it could instead fail at compile time.
  // format(instant) for instance can fail with `exception[[UnsupportedTemporalTypeException: Unsupported field: YearOfEra`
  // or `java.time.temporal.UnsupportedTemporalTypeException: Unsupported field: OffsetSeconds`
  // use `print` instead to ensure all fields are provided
  extension (d: DateTimeFormatter)
    def print(date: LocalDate): String         = d.format(date)
    def print(dateTime: LocalDateTime): String = d.format(dateTime.atOffset(utcZone))
    def print(instant: Instant): String        = print(instant.dateTime)

  case class TimeInterval(start: Instant, end: Instant):
    def overlaps(other: TimeInterval): Boolean = start.isBefore(other.end) && other.start.isBefore(end)
    def contains(date: Instant): Boolean       = (start == date || start.isBefore(date)) && end.isAfter(date)

  object TimeInterval:
    def apply(start: Instant, duration: Duration): TimeInterval =
      TimeInterval(start, start.plus(duration))

  def millisToInstant(millis: Long): Instant        = Instant.ofEpochMilli(millis)
  def millisToDateTime(millis: Long): LocalDateTime = millisToInstant(millis).dateTime

  inline def nowDateTime: LocalDateTime = LocalDateTime.now()
  inline def nowInstant: Instant        = Instant.now()
  inline def nowNanos: Long             = System.nanoTime()
  inline def nowMillis: Long            = System.currentTimeMillis()
  inline def nowCentis: Long            = nowMillis / 10
  inline def nowTenths: Long            = nowMillis / 100
  inline def nowSeconds: Int            = (nowMillis / 1000).toInt

  def instantOf(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int) =
    java.time.LocalDateTime.of(year, month, dayOfMonth, hour, minute).instant

  def daysBetween(from: LocalDateTime, to: LocalDateTime): Int =
    ChronoUnit.DAYS.between(from, to).toInt

  def daysBetween(from: Instant, to: Instant): Int =
    ChronoUnit.DAYS.between(from, to).toInt

  val isoDateTimeFormatter = java.time.format.DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(utcZone)
  val isoInstantFormatter  = java.time.format.DateTimeFormatter.ISO_INSTANT.withZone(utcZone)
