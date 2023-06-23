package ornicar.scalalib

import alleycats.Zero
import cats.Eq
import cats.data.Validated
import scala.util.matching.Regex
import pprint.pprintln

object extensions:

  extension (r: Regex)
    def find(s: String): Boolean =
      r.pattern.matcher(s).find
    def matches(s: String): Boolean =
      r.pattern.matcher(s).matches

  /** K combinator implementation Provides oneliner side effects See
    * https://web.archive.org/web/20111209063845/hacking-scala.posterous.com/side-effecting-without-braces
    */
  extension [A](a: A)
    def kCombinator(sideEffect: A => Unit): A =
      sideEffect(a)
      a
    infix def ~(sideEffect: A => Unit): A  = kCombinator(sideEffect)
    def pp: A                              = kCombinator(pprintln(_))
    infix def pp(msg: String): A           = kCombinator(x => pprintln(s"[$msg] $x"))
    def ppAs(as: A => Any): A              = kCombinator(x => pprintln(as(x)))
    def ppAs(as: A => Any, msg: String): A = kCombinator(x => pprintln(s"[$msg] ${as(x)}"))

  extension [A, B](m: Map[A, B])
    // Add Map.mapKeys, similar to Map.mapValues
    def mapKeys[C](f: A => C): Map[C, B] = m.map { (a, b) => (f(a), b) }
    // Add Map.filterValues, similar to Map.filterKeys
    def filterValues(p: B => Boolean): Map[A, B] = m.filter { x => p(x._2) }

  extension [A](as: Iterable[A]) def mapBy[B](f: A => B): Map[B, A] = as.view.map { a => f(a) -> a }.toMap

  extension [E, A](validated: Validated[E, A])
    def flatMap[EE >: E, B](f: A => Validated[EE, B]): Validated[EE, B] = validated.andThen(f)

  extension [A](seq: Seq[A])
    def has[B](b: B)(using A =:= B): Boolean = seq.contains(b)
    def indexOption(a: A)                    = Option(seq indexOf a).filter(0 <= _)

  extension [A](self: Option[A])

    infix def so[B: Zero](f: A => B): B = self.fold(Zero[B].zero)(f)

    inline def ifTrue(b: Boolean): Option[A]  = self.filter(_ => b)
    inline def ifFalse(b: Boolean): Option[A] = self.filter(_ => !b)

    // typesafe getOrElse
    inline infix def |(default: => A): A = self getOrElse default

    inline def unary_~(using z: Zero[A]): A = self getOrElse z.zero
    inline def orZero(using z: Zero[A]): A  = self getOrElse z.zero

    def has(b: A)(using Eq[A]): Boolean = self.contains_(b)

    def soUse[B: Zero](f: A ?=> B): B      = self.fold(Zero[B].zero)(f(using _))
    def foldUse[B](zero: B)(f: A ?=> B): B = self.fold(zero)(f(using _))

  implicit final class OrnicarBooleanWrapper(private val self: Boolean) extends AnyVal:
    inline def option[A](a: => A): Option[A]                = if self then Some(a) else None
    inline infix def so[A](a: => A)(using zero: Zero[A]): A = if self then a else zero.zero
