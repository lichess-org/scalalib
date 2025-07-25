package scalalib

import scala.annotation.nowarn
import cats.kernel.Eq
import cats.Show
import alleycats.Zero

// thanks Anton!
// https://github.com/indoorvivants/opaque-newtypes/blob/main/modules/core/src/main/scala/OpaqueNewtypes.scala

// WARNING: This implementation below is fragile and seemingly small changes can degrade performance.
// In particular, do not add any methods which implicitly use the witness to perform type conversions.
// Only use `raw` and `apply` (or the extension methods) to convert types.
//
// Details: The classes use witnesses to ensure type compatibility.  However, in order to completely inline
// simple methods like `<` (OpaqueInt), we cannot use the witness, and instead "cast" using asInstanceOf.
// During compilation, scala3 detects and removes redundant casts when using asInstanceOf, but it does *not*
// detect or remove redudant witness casts. So, when using asInstanceOf, types are completely elided, but not
// so with witnesses. This is especially problematic for methods marked `inline` which should be small.
//
// The challenge for you, dear coder, when writing new code, either in this file or in a subclass, is that
// you can accidentally rely on a witness cast, because scala will happily use a witness implicitly for
// conversions. It's up to you to ensure any new code does not use a witness conversion (use `raw`, `apply`,
// or extension methods instead).
//
// === Why this issue is not regression tested ===
// - The scala class `=:=` is sealed and difficult/impossible to subclass, so we cannot create a mock which raises
//   exceptions on use and test each method.
// - It's possible to compile code, decompile it, and inspect bytecode but this requires a hell of a lot of
//   machinery. See github.com/scala/scala3/blob/main/compiler/test/dotty/tools/backend/jvm/ArrayApplyOptTest.scala
//   as an example of what would be required.  (it's a lot -- compiling to disk, finding the right class files,
//   interpreting bytecode, ignoring irrelevant differences, etc.)
object newtypes:

  @FunctionalInterface
  trait SameRuntime[A, T]:
    // TODO: Convert in both directions...
    def apply(a: A): T

    extension (a: A) def transform: T = apply(a)

  object SameRuntime:
    def apply[A, T](f: A => T): SameRuntime[A, T] = new:
      override def apply(a: A): T = f(a)

  type StringRuntime[A] = SameRuntime[A, String]
  type IntRuntime[A] = SameRuntime[A, Int]
  type LongRuntime[A] = SameRuntime[A, Long]
  type FloatRuntime[A] = SameRuntime[A, Float]
  type DoubleRuntime[A] = SameRuntime[A, Double]

  abstract class TotalWrapper[Newtype, Impl](using Newtype =:= Impl):
    inline final def raw(inline a: Newtype): Impl = a.asInstanceOf[Impl]
    inline final def apply(inline s: Impl): Newtype = s.asInstanceOf[Newtype]
    inline final def from[M[_]](inline f: M[Impl]): M[Newtype] = f.asInstanceOf[M[Newtype]]
    @nowarn("msg=unused implicit parameter")
    def from[M[_], B](using SameRuntime[B, Impl])(f: M[B]): M[Newtype] = f.asInstanceOf[M[Newtype]]
    @nowarn("msg=unused explicit parameter")
    final def from[M[_], B](other: TotalWrapper[B, Impl])(f: M[B]): M[Newtype] = f.asInstanceOf[M[Newtype]]
    inline final def raw[M[_]](inline f: M[Newtype]): M[Impl] = f.asInstanceOf[M[Impl]]

    given SameRuntime[Newtype, Impl] = raw(_)
    given SameRuntime[Impl, Newtype] = apply(_)
    // Avoiding a simple cast because Eq is @specialized, so there might be edge cases.
    given (eqi: Eq[Impl]) => Eq[Newtype] = new:
      override def eqv(x: Newtype, y: Newtype) = eqi.eqv(raw(x), raw(y))

    extension (inline a: Newtype)
      inline def value: Impl = raw(a)
      inline def into[X](inline other: TotalWrapper[X, Impl]): X = other.apply(raw(a))
      inline def map(inline f: Impl => Impl): Newtype = apply(f(raw(a)))
  end TotalWrapper

  abstract class FunctionWrapper[Newtype, Impl](using Newtype =:= Impl) extends TotalWrapper[Newtype, Impl]:
    extension (inline a: Newtype) inline def exec: Impl = a.asInstanceOf[Impl]

  abstract class OpaqueString[A](using A =:= String) extends TotalWrapper[A, String]:
    given Show[A] = _.value
    given Render[A] = _.value

  /** [[RichOpaqueInt]] is preferred when possible as it offers more type safety.
    */
  abstract class RelaxedOpaqueInt[A](using A =:= Int) extends TotalWrapper[A, Int]:
    extension (inline a: A)
      inline def unary_- : A = apply(-raw(a))
      inline infix def >(inline o: Int): Boolean = raw(a) > o
      inline infix def <(inline o: Int): Boolean = raw(a) < o
      inline infix def >=(inline o: Int): Boolean = raw(a) >= o
      inline infix def <=(inline o: Int): Boolean = raw(a) <= o
      inline infix def +(inline o: Int): A = apply(raw(a) + o)
      inline infix def -(inline o: Int): A = apply(raw(a) - o)
      inline def atLeast(inline bot: Int): A = apply(Math.max(raw(a), bot))
      inline def atMost(inline top: Int): A = apply(Math.min(raw(a), top))
      inline infix def >(inline o: A): Boolean = >(raw(o))
      inline infix def <(inline o: A): Boolean = <(raw(o))
      inline infix def >=(inline o: A): Boolean = >=(raw(o))
      inline infix def <=(inline o: A): Boolean = <=(raw(o))
      inline infix def +(inline o: A): A = a + raw(o)
      inline infix def -(inline o: A): A = a - raw(o)
      inline def atLeast(inline bot: A): A = atLeast(raw(bot))
      inline def atMost(inline top: A): A = atMost(raw(top))
  end RelaxedOpaqueInt

  abstract class RichOpaqueInt[A](using A =:= Int) extends TotalWrapper[A, Int]:
    extension (inline a: A)
      inline def unary_- : A = apply(-raw(a))
      inline infix def >(inline o: A): Boolean = raw(a) > raw(o)
      inline infix def <(inline o: A): Boolean = raw(a) < raw(o)
      inline infix def >=(inline o: A): Boolean = raw(a) >= raw(o)
      inline infix def <=(inline o: A): Boolean = raw(a) <= raw(o)
      inline infix def +(inline o: A): A = apply(raw(a) + raw(o))
      inline infix def -(inline o: A): A = apply(raw(a) - raw(o))
      inline def atLeast(inline bot: A): A = apply(Math.max(raw(a), raw(bot)))
      inline def atMost(inline top: A): A = apply(Math.min(raw(a), raw(top)))
  end RichOpaqueInt

  abstract class OpaqueInt[A](using A =:= Int) extends TotalWrapper[A, Int]
  abstract class OpaqueLong[A](using A =:= Long) extends TotalWrapper[A, Long]
  abstract class OpaqueDouble[A](using A =:= Double) extends TotalWrapper[A, Double]
  abstract class OpaqueFloat[A](using A =:= Float) extends TotalWrapper[A, Float]

  import scala.concurrent.duration.FiniteDuration
  abstract class OpaqueDuration[A](using A =:= FiniteDuration) extends TotalWrapper[A, FiniteDuration]

  abstract class YesNo[A](using A =:= Boolean) extends TotalWrapper[A, Boolean]:
    final def Yes: A = apply(true)
    final def No: A = apply(false)

    extension (inline a: A)
      inline def flip: A = apply(!raw(a))
      inline def unary_! : A = a.flip
      inline def yes: Boolean = raw(a)
      inline def no: Boolean = !raw(a)
      inline def &&(inline other: A): A = apply(raw(a) && raw(other))
      inline def `||`(inline other: A): A = apply(raw(a) || raw(other))
  end YesNo

  inline def sameOrdering[A, T](using bts: SameRuntime[T, A], ord: Ordering[A]): Ordering[T] =
    Ordering.by(bts.apply(_))

  inline def stringOrdering[T: StringRuntime](using Ordering[String]): Ordering[T] = sameOrdering[String, T]
  inline def intOrdering[T: IntRuntime](using Ordering[Int]): Ordering[T] = sameOrdering[Int, T]
  inline def longOrdering[T: LongRuntime](using Ordering[Long]): Ordering[T] = sameOrdering[Long, T]
  inline def floatOrdering[T: FloatRuntime](using Ordering[Float]): Ordering[T] = sameOrdering[Float, T]
  inline def doubleOrdering[T: DoubleRuntime](using Ordering[Double]): Ordering[T] = sameOrdering[Double, T]

  given [A] => (sr: SameRuntime[Boolean, A]) => Zero[A] = Zero(sr(false))
