package scalalib

import alleycats.Zero
import scala.collection.immutable.SeqMap

object zeros:

  given Zero[String]:
    def zero = ""

  given Zero[Boolean]:
    def zero = false

  given Zero[Int]:
    def zero = 0

  given Zero[Long]:
    def zero = 0L

  given Zero[Double]:
    def zero = 0d

  given Zero[Float]:
    def zero = 0f

  given Zero[Unit]:
    def zero = ()

  given [A] => Zero[List[A]]:
    def zero = List.empty

  given [A, B] => Zero[Map[A, B]]:
    def zero = Map.empty

  given [A] => Zero[Option[A]]:
    def zero = Option.empty

  given [A] => Zero[Set[A]]:
    def zero = Set.empty

  given [A] => Zero[Seq[A]]:
    def zero = Seq.empty

  given [A] => Zero[Vector[A]]:
    def zero = Vector.empty

  given [A, B] => Zero[SeqMap[A, B]]:
    def zero = SeqMap.empty
