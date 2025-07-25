package scalalib

import java.time.{ Duration, Instant, LocalDate, LocalDateTime, ZoneOffset }
import java.time.temporal.{ ChronoUnit, TemporalAdjuster }
import scala.concurrent.duration as concDur
import java.time.format.DateTimeFormatter

// about java.time https://stackoverflow.com/a/32443004
object time:
  // private vals are faster (accessed as a static member).
  private val utc_ = ZoneOffset.UTC

  val utcZone = utc_

  extension (d: LocalDate) def adjust(a: TemporalAdjuster): LocalDate = d.`with`(a)

  extension (d: LocalDateTime)
    /** Seconds from epoch.
      *
      * This uses a checked conversion, it will overflow and raise if the year is after 2038.
      */
    def toSeconds: Int = Math.toIntExact(d.toEpochSecond(utc_))

    def toMillis: Long = d.toInstant(utc_).toEpochMilli
    def toCentis: Long = toMillis / 10L
    def instant: Instant = d.toInstant(utc_)
    def date: LocalDate = d.toLocalDate
    def toNow: Duration = instant.toNow
    def isBeforeNow: Boolean = instant.isBeforeNow
    def isAfterNow: Boolean = instant.isAfterNow
    def atMost(other: LocalDateTime): LocalDateTime = if other.isBefore(d) then other else d
    def atLeast(other: LocalDateTime): LocalDateTime = if other.isAfter(d) then other else d
    def withTimeAtStartOfDay: LocalDateTime = d.toLocalDate.atStartOfDay
    def plus(dur: concDur.Duration): LocalDateTime = instant.plusMillis(dur.toMillis).dateTime
    def minus(dur: concDur.Duration): LocalDateTime = instant.minusMillis(dur.toMillis).dateTime
    def adjust(a: TemporalAdjuster): LocalDateTime = d.`with`(a)

  extension (i: Instant)
    /** Seconds from epoch.
      *
      * This uses a checked conversion, it will overflow and raise if the year is after 2038.
      */
    def toSeconds: Int = Math.toIntExact(toMillis / 1000L)

    def toMillis: Long = i.toEpochMilli
    def toCentis: Long = toMillis / 10L
    def date: LocalDate = LocalDate.ofInstant(i, utc_)
    def dateTime: LocalDateTime = LocalDateTime.ofInstant(i, utc_)
    def toNow: Duration = Duration.between(i, Instant.now)
    def isBeforeNow: Boolean = i.isBefore(Instant.now)
    def isAfterNow: Boolean = i.isAfter(Instant.now)
    def atMost(other: Instant): Instant = if other.isBefore(i) then other else i
    def atLeast(other: Instant): Instant = if other.isAfter(i) then other else i
    def withTimeAtStartOfDay: Instant = date.atStartOfDay(utc_).toInstant
    def plus(dur: concDur.Duration): Instant = i.plusMillis(dur.toMillis)
    def minus(dur: concDur.Duration): Instant = i.minusMillis(dur.toMillis)
    def adjust(a: TemporalAdjuster): Instant = i.`with`(a)

    // These methods add time in LocalDateTime space and then convert back to
    // an Instant using the UTC timezone. These conversions can lead to unexpected
    // results. For example, adding a minute to an Instant that's close to a leap
    // second will result in an Instant that's 61 seconds later than the original.
    // And adding a month or year is not well defined for Instants, but ambiguity
    // is resolved through the UTC trampoline.
    // Regardless of whether these methods do too much implicitly, they are what
    // one normally wants for adjusting Instants in a service that uses UTC.
    def plusMinutes(m: Int): Instant = dateTime.plusMinutes(m).instant
    def minusMinutes(m: Int): Instant = dateTime.minusMinutes(m).instant
    def plusHours(h: Int): Instant = dateTime.plusHours(h).instant
    def minusHours(h: Int): Instant = dateTime.minusHours(h).instant
    def plusDays(d: Int): Instant = dateTime.plusDays(d).instant
    def minusDays(d: Int): Instant = dateTime.minusDays(d).instant
    def plusWeeks(w: Int): Instant = dateTime.plusWeeks(w).instant
    def minusWeeks(w: Int): Instant = dateTime.minusWeeks(w).instant
    def plusMonths(m: Int): Instant = dateTime.plusMonths(m).instant
    def minusMonths(m: Int): Instant = dateTime.minusMonths(m).instant
    def plusYears(y: Int): Instant = dateTime.plusYears(y).instant
    def minusYears(y: Int): Instant = dateTime.minusYears(y).instant

    // These methods guarantee that the resulting instant is exactly the
    // specified amount away from the original instant.
    def plusStdMinutes(m: Int): Instant = i.plus(Duration.ofMinutes(m))
    def minusStdMinutes(m: Int): Instant = i.minus(Duration.ofMinutes(m))
    def plusStdHours(h: Int): Instant = i.plus(Duration.ofHours(h))
    def minusStdHours(h: Int): Instant = i.minus(Duration.ofHours(h))
    def plusStdDays(d: Int): Instant = i.plus(Duration.ofDays(d))
    def minusStdDays(d: Int): Instant = i.minus(Duration.ofDays(d))

  // DateTimeFormatter is very dangerous as it throws exceptions where it could instead fail at compile time.
  // format(instant) for instance can fail with `exception[[UnsupportedTemporalTypeException: Unsupported field: YearOfEra`
  // or `java.time.temporal.UnsupportedTemporalTypeException: Unsupported field: OffsetSeconds`
  // use `print` instead to ensure all fields are provided
  extension (d: DateTimeFormatter)
    def print(date: LocalDate): String = d.format(date)
    def print(dateTime: LocalDateTime): String = d.format(dateTime.atOffset(utc_))
    def print(instant: Instant): String = print(instant.dateTime)

  case class TimeInterval(start: Instant, end: Instant):
    def overlaps(other: TimeInterval): Boolean = start.isBefore(other.end) && other.start.isBefore(end)
    def contains(date: Instant): Boolean = !start.isAfter(date) && end.isAfter(date)

  object TimeInterval:
    def apply(start: Instant, duration: Duration): TimeInterval =
      TimeInterval(start, start.plus(duration))

  def millisToInstant(millis: Long): Instant = Instant.ofEpochMilli(millis)
  def millisToDateTime(millis: Long): LocalDateTime = millisToInstant(millis).dateTime

  inline def nowDateTime: LocalDateTime = LocalDateTime.now(utc_)
  inline def nowInstant: Instant = Instant.now()
  inline def nowMillis: Long = System.currentTimeMillis()
  inline def nowCentis: Long = nowMillis / 10L
  inline def nowTenths: Long = nowMillis / 100L
  // Unchecked conversion, but this won't overflow until 2038
  inline def nowSeconds: Int = (nowMillis / 1000L).toInt

  /** Relative to some arbitrary point in time.
    *
    * Useful only in comparisons to self, such as measuring time intervals, though even this can be
    * problematic as the clock can pause if the process sleeps or system hibernates.
    */
  inline def nowNanosRel: Long = System.nanoTime()

  def instantOf(year: Int, month: Int, dayOfMonth: Int, hour: Int, minute: Int) =
    java.time.LocalDateTime.of(year, month, dayOfMonth, hour, minute).instant

  def daysBetween(from: LocalDateTime, to: LocalDateTime): Int =
    ChronoUnit.DAYS.between(from, to).toInt

  def daysBetween(from: Instant, to: Instant): Int =
    ChronoUnit.DAYS.between(from, to).toInt

  val isoDateTimeFormatter = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(utc_)
  val isoInstantFormatter = DateTimeFormatter.ISO_INSTANT.withZone(utc_)
