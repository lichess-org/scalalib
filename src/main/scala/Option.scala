package ornicar.scalalib

object OrnicarOption {

  implicit final class ornicarOption[A](self: Option[A]) {

    def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero

    def ??[B: Zero](f: A ⇒ B): B = self.fold(Zero[B].zero)(f)

    def ifTrue(b: Boolean): Option[A] = self filter (_ ⇒ b)
    def ifFalse(b: Boolean): Option[A] = self filter (_ ⇒ !b)
  }
}
