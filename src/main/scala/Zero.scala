package ornicar.scalalib

trait Zero[F] {
  def zero: F
}

object Zero {

  @inline def apply[F](implicit F: Zero[F]): Zero[F] = F

  def instance[A](z: A): Zero[A] = new Zero[A] {
    def zero = z
  }

  trait Syntax {

    def zero[F](implicit F: Zero[F]): F = F.zero
  }

  trait Instances {

    import scalaz.Monoid
    import OrnicarMonoid.Instances._

    implicit def MonoidZero[A](implicit m: scalaz.Monoid[A]): Zero[A] = instance(m.zero)
    implicit def OptionZero[A]: Zero[Option[A]] = instance(None)
  }
}
