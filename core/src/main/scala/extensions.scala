package scalalib

import alleycats.Zero
import cats.Eq
import cats.syntax.all.*
import scala.util.matching.Regex
import scala.concurrent.{ ExecutionContext, Future }
import java.lang.Math.{ max, min }
import pprint.pprintln

object extensions:

  extension (r: Regex) def find(s: String): Boolean = r.pattern.matcher(s).find

  extension (s: String)
    def replaceIf(t: Char, r: Char): String =
      if s.indexOf(t.toInt) >= 0 then s.replace(t, r) else s
    def replaceIf(t: Char, r: CharSequence): String =
      if s.indexOf(t.toInt) >= 0 then s.replace(String.valueOf(t), r) else s
    def replaceIf(t: CharSequence, r: CharSequence): String =
      if s.contains(t) then s.replace(t, r) else s
    def replaceAllIn(regex: Regex, replacement: String) = regex.replaceAllIn(s, replacement)

  private def pprintlnBoth(x: Any, y: Any): Unit =
    (pprint.tokenize(x) ++ Seq(fansi.Str(" ")) ++ pprint.tokenize(y)).foreach(print)
    println()

  extension [A](a: A)
    def pp: A                              = tap(pprintln(_))
    infix def pp(msg: Any): A              = tap(pprintlnBoth(msg, _))
    def ppAs(as: A => Any): A              = tap(x => pprintln(as(x)))
    def ppAs(as: A => Any, msg: String): A = tap(x => pprintlnBoth(msg, as(x)))
    def pps: A                             = tap(x => pprintln(x.toString))
    infix def pp(msg: String): A           = tap(x => pprintlnBoth(msg, x))

    // https://github.com/Ichoran/kse3/blob/d3e3ec4771599204a22091ece804dd3491b31ea3/basics/src/Data.scala#L5506-L5510
    /** replacement for standard lib pipe use inline to avoid boxing */
    inline def pipe[B](inline f: A => B): B = f(a)

    /** Apply a side-effecting function to this value; return the original value */
    inline def tap(inline f: A => Unit): A =
      f(a)
      a

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
      case Some(x) => f(x).map(Some(_))(using ExecutionContext.parasitic)
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

  extension (self: Boolean)
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
