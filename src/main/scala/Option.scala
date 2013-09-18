package ornicar.scalalib

trait OrnicarOption[A] {

  def self: Option[A]

  final def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero
}

object OrnicarOption {

  implicit def ornicarOption[A](o: Option[A]) = new OrnicarOption[A] {
    def self = o
  }
}
