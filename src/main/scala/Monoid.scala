package ornicar.scalalib

import scalaz.Monoid

object OrnicarMonoid {

  trait Instances {

    import Zero.Instances._
    import Zero.Syntax._

    implicit val BooleanMonoid: Monoid[Boolean] = new Monoid[Boolean] {
      def append(f1: Boolean, f2: ⇒ Boolean) = f1 || f2
      def zero = ∅[Boolean]
    }

    implicit val IntMonoid: Monoid[Int] = new Monoid[Int] {
      def append(f1: Int, f2: ⇒ Int) = f1 + f2
      def zero = ∅[Int]
    }

    implicit val UnitMonoid: Monoid[Unit] = new Monoid[Unit] {
      def append(f1: Unit, f2: ⇒ Unit) = ()
      def zero = ∅[Unit]
    }

    implicit def SeqMonoid[A]: Monoid[Seq[A]] = new Monoid[Seq[A]] {
      def append(f1: Seq[A], f2: ⇒ Seq[A]) = f1 ++ f2
      def zero: Seq[A] = ∅[Seq[A]]
    }
  }
}
