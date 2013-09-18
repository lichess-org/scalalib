package ornicar.scalalib

trait OrnicarOption[A] {

  def self: Option[A]

  final def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero

  def ??[B: Zero](f: A ⇒ B): B = self.fold(Zero[B].zero)(f)

  def ifTrue(b: Boolean): Option[A] = self filter (_ ⇒ b)
  def ifFalse(b: Boolean): Option[A] = self filter (_ ⇒ !b)
}

object OrnicarOption {

  implicit def ornicarOption[A](o: Option[A]) = new OrnicarOption[A] {
    def self = o
  }
}
