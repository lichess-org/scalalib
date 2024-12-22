package scalalib

import scalalib.newtypes.*

object model:

  opaque type Max = Int
  object Max extends RelaxedOpaqueInt[Max]

  opaque type MaxPerPage = Int
  object MaxPerPage extends RelaxedOpaqueInt[MaxPerPage]

  opaque type MaxPerSecond = Int
  object MaxPerSecond extends RelaxedOpaqueInt[MaxPerSecond]

  opaque type Days = Int
  object Days extends RelaxedOpaqueInt[Days]

  opaque type Seconds = Int
  object Seconds extends RelaxedOpaqueInt[Seconds]

  trait Percent[A]:
    def value(a: A): Double
    def apply(a: Double): A
  object Percent:
    def of[A](w: TotalWrapper[A, Double]): Percent[A] = new:
      def apply(a: Double): A = w(a)
      def value(a: A): Double = w.value(a)
    def toInt[A](a: A)(using p: Percent[A]): Int = Math.round(p.value(a)).toInt // round to closest
