package ornicar.scalalib

import scalaz.Monoid

trait OrnicarMonoids {

  implicit val BooleanMonoid: Monoid[Boolean] = new Monoid[Boolean] {
    def append(f1: Boolean, f2: ⇒ Boolean) = f1 || f2
    def zero = false
  }

  implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
    def append(f1: Int, f2: ⇒ Int) = f1 + f2
    def zero = 0
  }
  implicit val LongMonoid: Monoid[Long] = new Monoid[Long] {
    def append(f1: Long, f2: ⇒ Long) = f1 + f2
    def zero = 0
  }
  implicit val DoubleMonoid: Monoid[Double] = new Monoid[Double] {
    def append(f1: Double, f2: ⇒ Double) = f1 + f2
    def zero = 0
  }
  implicit val FloatMonoid: Monoid[Float] = new Monoid[Float] {
    def append(f1: Float, f2: ⇒ Float) = f1 + f2
    def zero = 0
  }

  implicit val UnitMonoid: Monoid[Unit] = new Monoid[Unit] {
    def append(f1: Unit, f2: ⇒ Unit) = ()
    def zero = ()
  }

  implicit def SeqMonoid[A]: Monoid[Seq[A]] = new Monoid[Seq[A]] {
    def append(f1: Seq[A], f2: ⇒ Seq[A]) = f1 ++ f2
    def zero: Seq[A] = Seq.empty
  }

  implicit def MapMonoid[A, B]: Monoid[Map[A, B]] = new Monoid[Map[A, B]] {
    def append(f1: Map[A, B], f2: ⇒ Map[A, B]) = f1 ++ f2
    def zero: Map[A, B] = Map.empty
  }

  implicit def SetMonoid[A]: Monoid[Set[A]] = new Monoid[Set[A]] {
    def append(f1: Set[A], f2: ⇒ Set[A]) = f1 ++ f2
    def zero: Set[A] = Set.empty
  }
}

object OrnicarMonoids extends OrnicarMonoids