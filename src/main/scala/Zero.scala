package ornicar.scalalib
import alleycats.Zero

trait Zeros:

  given Zero[String] with
    def zero = ""

  given Zero[Boolean] with
    def zero = false

  given Zero[Int] with
    def zero = 0

  given Zero[Long] with
    def zero = 0L

  given Zero[Double] with
    def zero = 0d

  given Zero[Float] with
    def zero = 0f

  given Zero[Unit] with
    def zero = ()

  given Zero[List[?]] with
    def zero = List.empty

  given Zero[Map[?, ?]] with
    def zero = Map.empty

  given Zero[Option[?]] with
    def zero = Option.empty

  given Zero[Set[?]] with
    def zero = Set.empty

  given Zero[Seq[?]] with
    def zero = Seq.empty

  given Zero[Vector[?]] with
    def zero = Vector.empty
