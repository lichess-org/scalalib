package scalalib

import alleycats.Zero
import cats.Eq
import cats.syntax.all.*
import scala.util.matching.Regex
import scala.concurrent.{ ExecutionContext, Future }
import java.lang.Math.{ max, min }
import pprint.pprintln

object extensions:

  extension (r: Regex)
    def find(s: String): Boolean =
      r.pattern.matcher(s).find
    def matches(s: String): Boolean =
      r.pattern.matcher(s).matches

  private def pprintlnBoth(x: Any, y: Any): Unit =
    (pprint.tokenize(x) ++ Seq(fansi.Str(" ")) ++ pprint.tokenize(y)).foreach(print)
    println()

  /** K combinator implementation Provides oneliner side effects See
    * https://web.archive.org/web/20111209063845/hacking-scala.posterous.com/side-effecting-without-braces
    */
  extension [A](a: A)
    def kCombinator(sideEffect: A => Unit): A =
      sideEffect(a)
      a
    def pp: A                              = kCombinator(pprintln(_))
    infix def pp(msg: Any): A              = kCombinator(pprintlnBoth(msg, _))
    def ppAs(as: A => Any): A              = kCombinator(x => pprintln(as(x)))
    def ppAs(as: A => Any, msg: String): A = kCombinator(x => pprintlnBoth(msg, as(x)))
    def pps: A                             = kCombinator(x => pprintln(x.toString))
    infix def pp(msg: String): A           = kCombinator(x => pprintlnBoth(msg, x))

  extension [A, B](m: Map[A, B])
    // Add Map.mapKeys, similar to Map.mapValues
    def mapKeys[C](f: A => C): Map[C, B] = m.map { (a, b) => (f(a), b) }
    // Add Map.filterValues, similar to Map.filterKeys
    def filterValues(p: B => Boolean): Map[A, B] = m.filter { x => p(x._2) }

  extension [A](as: Iterable[A]) def mapBy[B](f: A => B): Map[B, A] = as.view.map { a => f(a) -> a }.toMap

  extension [A](seq: Seq[A])
    def has(b: A)(using Eq[A]): Boolean = seq.contains(b)

    def indexOption(a: A) = Option(seq.indexOf(a)).filter(0 <= _)

  extension [A](self: Option[A])

    infix def so[B: Zero](f: A => B): B = self.fold(Zero[B].zero)(f)

    def soFu[B](f: A => Future[B]): Future[Option[B]] = self match
      case Some(x) => f(x).map(Some(_))(ExecutionContext.parasitic)
      case None    => Future.successful(None)

    inline def ifTrue(b: Boolean): Option[A]  = self.filter(_ => b)
    inline def ifFalse(b: Boolean): Option[A] = self.filter(_ => !b)

    // typesafe getOrElse
    inline infix def |(default: => A): A = self.getOrElse(default)

    inline def unary_~(using z: Zero[A]): A = self.getOrElse(z.zero)
    inline def orZero(using z: Zero[A]): A  = self.getOrElse(z.zero)

    def has(b: A)(using Eq[A]): Boolean = self.exists(_ === b)

    def soUse[B: Zero](f: A ?=> B): B      = self.fold(Zero[B].zero)(f(using _))
    def foldUse[B](zero: B)(f: A ?=> B): B = self.fold(zero)(f(using _))

  implicit final class ScalalibBooleanWrapper(private val self: Boolean) extends AnyVal:
    inline def option[A](a: => A): Option[A]                = if self then Some(a) else None
    inline infix def so[A](a: => A)(using zero: Zero[A]): A = if self then a else zero.zero

  extension (self: Long)
    infix def atLeast(bottomValue: Long): Long = max(self, bottomValue)
    infix def atMost(topValue: Long): Long     = min(self, topValue)
    def squeeze(bottom: Long, top: Long): Long = max(min(self, top), bottom)
    def toSaturatedInt: Int =
      if self.toInt == self then self.toInt
      else if self > 0 then Integer.MAX_VALUE
      else Integer.MIN_VALUE

  extension (self: Int)
    def atLeast(bottomValue: Int): Int      = max(self, bottomValue)
    def atMost(topValue: Int): Int          = min(self, topValue)
    def squeeze(bottom: Int, top: Int): Int = max(min(self, top), bottom)

  extension (self: Float)
    def atLeast(bottomValue: Float): Float        = max(self, bottomValue)
    def atMost(topValue: Float): Float            = min(self, topValue)
    def squeeze(bottom: Float, top: Float): Float = max(min(self, top), bottom)

  extension (self: Double)
    def atLeast(bottomValue: Double): Double         = max(self, bottomValue)
    def atMost(topValue: Double): Double             = min(self, topValue)
    def squeeze(bottom: Double, top: Double): Double = max(min(self, top), bottom)
