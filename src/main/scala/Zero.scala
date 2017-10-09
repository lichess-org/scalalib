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
    import Zero.{instance => z}

    implicit val stringZero: Zero[String] = z("")
    implicit val booleanZero: Zero[Boolean] = z(false)
    implicit val intZero: Zero[Int] = z(0)
    implicit val longZero: Zero[Long] = z(0l)
    implicit val doubleZero: Zero[Double] = z(0d)
    implicit val floatZero: Zero[Float] = z(0f)
    implicit val unitZero: Zero[Unit] = z(())

    private val zList = z(List.empty)
    private val zMap = z(Map.empty)
    private val zOption = z(Option.empty)
    private val zSet = z(Set.empty)
    private val zSeq = z(Seq.empty)
    private val zVector = z(Vector.empty)

    @inline implicit def listZero[A] = zList.asInstanceOf[Zero[List[A]]]
    @inline implicit def mapZero[A, B] = zMap.asInstanceOf[Zero[Map[A, B]]]
    @inline implicit def optionZero[A] = zOption.asInstanceOf[Zero[Option[A]]]
    @inline implicit def setZero[A] = zSet.asInstanceOf[Zero[Set[A]]]
    @inline implicit def seqZero[A] = zSeq.asInstanceOf[Zero[Seq[A]]]
    @inline implicit def vectorZero[A] = zVector.asInstanceOf[Zero[Vector[A]]]
}
