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
}

trait Zeros {
  import Zero.{ instance => z }

  implicit final val stringZero: Zero[String]   = z("")
  implicit final val booleanZero: Zero[Boolean] = z(false)
  implicit final val intZero: Zero[Int]         = z(0)
  implicit final val longZero: Zero[Long]       = z(0L)
  implicit final val doubleZero: Zero[Double]   = z(0d)
  implicit final val floatZero: Zero[Float]     = z(0f)
  implicit final val unitZero: Zero[Unit]       = z(())

  private[this] val zList   = z(List.empty)
  private[this] val zMap    = z(Map.empty)
  private[this] val zOption = z(Option.empty)
  private[this] val zSet    = z(Set.empty)
  private[this] val zSeq    = z(Seq.empty)
  private[this] val zVector = z(Vector.empty)

  @inline implicit def listZero[A]   = zList.asInstanceOf[Zero[List[A]]]
  @inline implicit def mapZero[A, B] = zMap.asInstanceOf[Zero[Map[A, B]]]
  @inline implicit def optionZero[A] = zOption.asInstanceOf[Zero[Option[A]]]
  @inline implicit def setZero[A]    = zSet.asInstanceOf[Zero[Set[A]]]
  @inline implicit def seqZero[A]    = zSeq.asInstanceOf[Zero[Seq[A]]]
  @inline implicit def vectorZero[A] = zVector.asInstanceOf[Zero[Vector[A]]]
}
