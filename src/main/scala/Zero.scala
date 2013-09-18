package ornicar.scalalib

trait Zero[F] {
  def zero: F
}

object Zero {

  @inline def apply[F](implicit F: Zero[F]): Zero[F] = F

  /** Make a zero into an instance. */
  def instance[A](z: A): Zero[A] = new Zero[A] {
    def zero = z
  }

  def ∅[Z](implicit z: Zero[Z]): Z = z.zero

  def mzero[Z](implicit z: Zero[Z]): Z = z.zero

  trait Syntax {

    def ∅[F](implicit F: Zero[F]): F = F.zero
  }

  trait Instances {

    import scalaz.Monoid
    import OrnicarMonoid.Instances._

    implicit def MonoidZero[A](implicit m: scalaz.Monoid[A]): Zero[A] = instance(m.zero)
  }
}
