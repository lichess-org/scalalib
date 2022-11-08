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

  given [A]: Zero[List[A]] with
    def zero = List.empty

  given [A, B]: Zero[Map[A, B]] with
    def zero = Map.empty

  given [A]: Zero[Option[A]] with
    def zero = Option.empty

  given [A]: Zero[Set[A]] with
    def zero = Set.empty

  given [A]: Zero[Seq[A]] with
    def zero = Seq.empty

  given [A]: Zero[Vector[A]] with
    def zero = Vector.empty
