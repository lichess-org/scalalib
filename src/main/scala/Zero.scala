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
  object Syntax extends Syntax

  trait Instances {

    implicit def SeqZero[A]: Zero[Seq[A]] = instance(Seq.empty[A])
    implicit def ListZero[A]: Zero[List[A]] = instance(List.empty[A])
    implicit def UnitZero: Zero[Unit] = instance(())
    implicit def StringZero: Zero[String] = instance("")
    implicit def IntZero: Zero[Int] = instance(0)
    implicit def BooleanZero: Zero[Boolean] = instance(false)
    implicit def CharZero: Zero[Char] = instance(0.toChar)
    implicit def ByteZero: Zero[Byte] = instance(0.toByte)
    implicit def LongZero: Zero[Long] = instance(0L)
    implicit def ShortZero: Zero[Short] = instance(0.toShort)
    implicit def FloatZero: Zero[Float] = instance(0F)
    implicit def DoubleZero: Zero[Double] = instance(0D)
    implicit def BigIntegerZero = instance(java.math.BigInteger.valueOf(0))
    implicit def BigIntZero: Zero[BigInt] = instance(BigInt(0))
  }
  object Instances extends Instances
}
