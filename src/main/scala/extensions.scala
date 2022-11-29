package ornicar.scalalib

import alleycats.Zero
import cats.data.Validated
import scala.util.matching.Regex

trait ScalalibExtensions:

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
    def ~(sideEffect: A => Unit): A = kCombinator(sideEffect)
    def pp: A                       = kCombinator(println)
    def pp(msg: String): A          = kCombinator(a => println(s"[$msg] $a"))

  extension [A, B](m: Map[A, B])
    // Add Map.mapKeys, similar to Map.mapValues
    def mapKeys[C](f: A => C): Map[C, B] =
      m map { case (a, b) =>
        (f(a), b)
      } toMap
    // Add Map.filterValues, similar to Map.filterKeys
    def filterValues(p: B => Boolean): Map[A, B] = m filter { x =>
      p(x._2)
    }

  extension [E, A](validated: Validated[E, A])
    def flatMap[EE >: E, B](f: A => Validated[EE, B]): Validated[EE, B] = validated.andThen(f)

  extension [A](self: Option[A])

    def ??[B: Zero](f: A => B): B = self.fold(Zero[B].zero)(f)

    def ifTrue(b: Boolean): Option[A]  = self.filter(_ => b)
    def ifFalse(b: Boolean): Option[A] = self.filter(_ => !b)

    // typesafe getOrElse
    def |(default: => A): A = self getOrElse default

    def unary_~(using z: Zero[A]): A = self getOrElse z.zero
    def orZero(using z: Zero[A]): A  = self getOrElse z.zero

implicit final class OrnicarBooleanWrapper(private val self: Boolean) extends AnyVal:
  def option[A](a: => A): Option[A]             = if (self) Some(a) else None
  def ??[A](a: => A)(implicit zero: Zero[A]): A = if (self) a else zero.zero

object ScalalibExtensions extends ScalalibExtensions
