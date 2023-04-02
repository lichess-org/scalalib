package ornicar.scalalib

import java.time.{ Duration, Instant, LocalDateTime, ZoneOffset }
import java.time.temporal.ChronoUnit

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
    def plus(duration: scala.concurrent.duration.Duration): LocalDateTime =
      d.plus(duration.toMillis, ChronoUnit.MILLIS)
    def minus(duration: scala.concurrent.duration.Duration): LocalDateTime =
      d.minus(duration.toMillis, ChronoUnit.MILLIS)

  def millisToDate(millis: Long): LocalDateTime =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), ZoneOffset.UTC)

  def daysBetween(from: LocalDateTime, to: LocalDateTime): Int =
    ChronoUnit.DAYS.between(from, to).toInt
