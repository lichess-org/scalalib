package scalalib

import cats.kernel.Eq
import cats.Show

// thanks Anton!
// https://github.com/indoorvivants/opaque-newtypes/blob/main/modules/core/src/main/scala/OpaqueNewtypes.scala

object newtypes:

  @FunctionalInterface
  trait SameRuntime[A, T]:
    def apply(a: A): T

  object SameRuntime:
    def apply[A, T](f: A => T): SameRuntime[A, T] = new:
      def apply(a: A): T = f(a)

  type StringRuntime[A] = SameRuntime[A, String]
  type IntRuntime[A]    = SameRuntime[A, Int]
  type DoubleRuntime[A] = SameRuntime[A, Double]

  trait TotalWrapper[Newtype, Impl](using ev: Newtype =:= Impl):
    inline def raw(inline a: Newtype): Impl              = a
    inline def apply(inline s: Impl): Newtype            = s.asInstanceOf[Newtype]
    inline def from[M[_]](inline f: M[Impl]): M[Newtype] = f.asInstanceOf[M[Newtype]]
    inline def from[M[_], B](using sr: SameRuntime[B, Impl])(inline f: M[B]): M[Newtype] =
      f.asInstanceOf[M[Newtype]]
    inline def from[M[_], B](inline other: TotalWrapper[B, Impl])(inline f: M[B]): M[Newtype] =
      f.asInstanceOf[M[Newtype]]
    inline def raw[M[_]](inline f: M[Newtype]): M[Impl] = f.asInstanceOf[M[Impl]]

    given SameRuntime[Newtype, Impl]    = identity
    given SameRuntime[Impl, Newtype]    = _.asInstanceOf[Newtype]
    given (using Eq[Impl]): Eq[Newtype] = Eq.by(_.value)

    extension (a: Newtype)
      inline def value: Impl                                     = a
      inline def into[X](inline other: TotalWrapper[X, Impl]): X = other.apply(a)
      inline def map(inline f: Impl => Impl): Newtype            = apply(f(a))
  end TotalWrapper

  trait FunctionWrapper[Newtype, Impl](using ev: Newtype =:= Impl) extends TotalWrapper[Newtype, Impl]:
    extension (a: Newtype) inline def apply: Impl = a

  trait OpaqueString[A](using A =:= String) extends TotalWrapper[A, String]:
    given Show[A]   = _.value
    given Render[A] = _.value

  trait OpaqueInt[A](using A =:= Int) extends TotalWrapper[A, Int]:
    extension (inline a: A)
      inline def unary_-                          = apply(-raw(a))
      inline infix def >(inline o: Int): Boolean  = raw(a) > o
      inline infix def <(inline o: Int): Boolean  = raw(a) < o
      inline infix def >=(inline o: Int): Boolean = raw(a) >= o
      inline infix def <=(inline o: Int): Boolean = raw(a) <= o
      inline infix def +(inline o: Int): A        = apply(raw(a) + o)
      inline infix def -(inline o: Int): A        = apply(raw(a) - o)
      inline def atLeast(inline bot: Int): A      = apply(math.max(raw(a), bot))
      inline def atMost(inline top: Int): A       = apply(math.min(raw(a), top))
      inline infix def >(inline o: A): Boolean    = >(raw(o))
      inline infix def <(inline o: A): Boolean    = <(raw(o))
      inline infix def >=(inline o: A): Boolean   = >=(raw(o))
      inline infix def <=(inline o: A): Boolean   = <=(raw(o))
      inline infix def +(inline o: A): A          = a + raw(o)
      inline infix def -(inline o: A): A          = a - raw(o)
      inline def atLeast(inline bot: A): A        = atLeast(raw(bot))
      inline def atMost(inline top: A): A         = atMost(raw(top))

  trait OpaqueLong[A](using A =:= Long) extends TotalWrapper[A, Long]
  trait OpaqueDouble[A](using A =:= Double) extends TotalWrapper[A, Double]:
    extension (inline a: A) inline def +(inline o: Int): A = apply(raw(a) + o)
  trait OpaqueFloat[A](using A =:= Float) extends TotalWrapper[A, Float]

  import scala.concurrent.duration.FiniteDuration
  trait OpaqueDuration[A](using A =:= FiniteDuration) extends TotalWrapper[A, FiniteDuration]

  abstract class YesNo[A](using ev: Boolean =:= A):
    val Yes: A = true
    val No: A  = false

    inline def from[M[_]](inline a: M[Boolean]): M[A] = a.asInstanceOf[M[A]]

    given SameRuntime[A, Boolean] = _ == Yes
    given SameRuntime[Boolean, A] = if _ then Yes else No
    given Eq[A]                   = Eq.by(_.value)

    inline def apply(inline b: Boolean): A = b

    extension (inline a: A)
      inline def value: Boolean        = a == Yes
      inline def flip: A               = if value then No else Yes
      inline def yes: Boolean          = value
      inline def no: Boolean           = !value
      inline def &&(inline other: A)   = a.value && other.value
      inline def `||`(inline other: A) = a.value || other.value
  end YesNo

  inline def sameOrdering[A, T](using bts: SameRuntime[T, A], ord: Ordering[A]): Ordering[T] =
    Ordering.by(bts.apply(_))

  inline def stringOrdering[T: StringRuntime](using Ordering[String]): Ordering[T] = sameOrdering[String, T]
  inline def intOrdering[T: IntRuntime](using Ordering[Int]): Ordering[T]          = sameOrdering[Int, T]
  inline def doubleOrdering[T: DoubleRuntime](using Ordering[Double]): Ordering[T] = sameOrdering[Double, T]
