package scalalib

import cats.kernel.Eq
import cats.Show
import alleycats.Zero

// thanks Anton!
// https://github.com/indoorvivants/opaque-newtypes/blob/main/modules/core/src/main/scala/OpaqueNewtypes.scala

// WARNING: This implementation below is fragile and seemingly small changes can significantly
// degrade performance. In particular, do not add any methods which implicitly use the witness to perform
// type conversions.  Only use `raw` and `apply` (or the extension methods) to convert types.

// Details: The classes use witnesses to ensure type compatibility.  However, in order to completely inline
// simple methods like `<` (OpaqueInt), we cannot use the witness, and instead "cast" using asInstanceOf.
// scala will remove these redudant casts at compile time, leaving the types completely elided.
// The issue for new code is that scala will happily use a witness implicitly to convert types, so if you're
// changing this file, it's up to you to ensure any new code does not use the witness (by using `raw`, `apply`,
// or the extension methods).

// Why it's not regression tested:
// - The scala class `=:=` is sealed and impossible to subclass, so we cannot create a mock which raises
//   exceptions on use.
// - It's possible to compile code, decompile it, and inspect bytecode but this requires a hell of a lot of
//   machinery.  See github.com/scala/scala3/blob/main/compiler/test/dotty/tools/backend/jvm/ArrayApplyOptTest.scala
//   as an example of what would be required.  (it's a lot -- compiling to disk, finding the right class files,
//   interpreting bytecode, ignoring irrelevant differences, etc.)
object newtypes:

  @FunctionalInterface
  trait SameRuntime[A, T]:
    def apply(a: A): T

    extension (a: A) def transform: T = apply(a)

  object SameRuntime:
    def apply[A, T](f: A => T): SameRuntime[A, T] = new:
      def apply(a: A): T = f(a)

  type StringRuntime[A] = SameRuntime[A, String]
  type IntRuntime[A]    = SameRuntime[A, Int]
  type DoubleRuntime[A] = SameRuntime[A, Double]

  abstract class TotalWrapper[Newtype, Impl](using Newtype =:= Impl):
    inline final def raw(inline a: Newtype): Impl              = a.asInstanceOf[Impl]
    inline final def apply(inline s: Impl): Newtype            = s.asInstanceOf[Newtype]
    inline final def from[M[_]](inline f: M[Impl]): M[Newtype] = f.asInstanceOf[M[Newtype]]
    inline final def from[M[_], B](using sr: SameRuntime[B, Impl])(inline f: M[B]): M[Newtype] =
      f.asInstanceOf[M[Newtype]]
    inline final def from[M[_], B](inline other: TotalWrapper[B, Impl])(inline f: M[B]): M[Newtype] =
      f.asInstanceOf[M[Newtype]]
    inline final def raw[M[_]](inline f: M[Newtype]): M[Impl] = f.asInstanceOf[M[Impl]]

    given SameRuntime[Newtype, Impl] = raw(_)
    given SameRuntime[Impl, Newtype] = apply(_)
    given (using e: Eq[Impl]): Eq[Newtype] = new Eq[Newtype]:
      override def eqv(x: Newtype, y: Newtype) = e.eqv(raw(x), raw(y))

    extension (inline a: Newtype)
      inline def value: Impl                                     = raw(a)
      inline def into[X](inline other: TotalWrapper[X, Impl]): X = other.apply(raw(a))
      inline def map(inline f: Impl => Impl): Newtype            = apply(f(raw(a)))
  end TotalWrapper

  abstract class FunctionWrapper[Newtype, Impl](using Newtype =:= Impl) extends TotalWrapper[Newtype, Impl]:
    extension (inline a: Newtype) inline def apply: Impl = a.asInstanceOf[Impl]

  abstract class OpaqueString[A](using A =:= String) extends TotalWrapper[A, String]:
    given Show[A]   = _.value
    given Render[A] = _.value

  abstract class OpaqueInt[A](using A =:= Int) extends TotalWrapper[A, Int]:
    extension (inline a: A)
      inline infix def >(inline o: Int): Boolean  = raw(a) > o
      inline infix def <(inline o: Int): Boolean  = raw(a) < o
      inline infix def >=(inline o: Int): Boolean = raw(a) >= o
      inline infix def <=(inline o: Int): Boolean = raw(a) <= o
      inline infix def +(inline o: Int): A        = apply(raw(a) + o)
      inline infix def -(inline o: Int): A        = apply(raw(a) - o)
      inline def atLeast(inline bot: Int): A      = apply(Math.max(raw(a), bot))
      inline def atMost(inline top: Int): A       = apply(Math.min(raw(a), top))
      inline infix def >(inline o: A): Boolean    = >(raw(o))
      inline infix def <(inline o: A): Boolean    = <(raw(o))
      inline infix def >=(inline o: A): Boolean   = >=(raw(o))
      inline infix def <=(inline o: A): Boolean   = <=(raw(o))
      inline infix def +(inline o: A): A          = a + raw(o)
      inline infix def -(inline o: A): A          = a - raw(o)
      inline def atLeast(inline bot: A): A        = atLeast(raw(bot))
      inline def atMost(inline top: A): A         = atMost(raw(top))

  abstract class OpaqueIntSafer[A](using A =:= Int) extends TotalWrapper[A, Int]:
    extension (inline a: A)
      inline def unary_- : A                    = apply(-raw(a))
      inline infix def >(inline o: A): Boolean  = raw(a) > raw(o)
      inline infix def <(inline o: A): Boolean  = raw(a) < raw(o)
      inline infix def >=(inline o: A): Boolean = raw(a) >= raw(o)
      inline infix def <=(inline o: A): Boolean = raw(a) <= raw(o)
      inline infix def +(inline o: A): A        = apply(raw(a) + raw(o))
      inline infix def -(inline o: A): A        = apply(raw(a) - raw(o))
      inline def atLeast(inline bot: A): A      = apply(Math.max(raw(a), raw(bot)))
      inline def atMost(inline top: A): A       = apply(Math.min(raw(a), raw(top)))

  abstract class OpaqueLong[A](using A =:= Long) extends TotalWrapper[A, Long]
  abstract class OpaqueDouble[A](using A =:= Double) extends TotalWrapper[A, Double]:
    extension (inline a: A) inline def +(inline o: Int): A = apply(raw(a) + o)
  abstract class OpaqueFloat[A](using A =:= Float) extends TotalWrapper[A, Float]

  import scala.concurrent.duration.FiniteDuration
  abstract class OpaqueDuration[A](using A =:= FiniteDuration) extends TotalWrapper[A, FiniteDuration]

  abstract class YesNo[A](using A =:= Boolean) extends TotalWrapper[A, Boolean]:
    extension (inline a: A)
      inline def flip: A                  = apply(!raw(a))
      inline def yes: Boolean             = raw(a)
      inline def no: Boolean              = !raw(a)
      inline def &&(inline other: A): A   = apply(raw(a) && raw(other))
      inline def `||`(inline other: A): A = apply(raw(a) || raw(other))
  end YesNo

  inline def sameOrdering[A, T](using bts: SameRuntime[T, A], ord: Ordering[A]): Ordering[T] =
    Ordering.by(bts.apply(_))

  inline def stringOrdering[T: StringRuntime](using Ordering[String]): Ordering[T] = sameOrdering[String, T]
  inline def intOrdering[T: IntRuntime](using Ordering[Int]): Ordering[T]          = sameOrdering[Int, T]
  inline def doubleOrdering[T: DoubleRuntime](using Ordering[Double]): Ordering[T] = sameOrdering[Double, T]

  given [A](using sr: SameRuntime[Boolean, A]): Zero[A] = Zero(sr(false))
