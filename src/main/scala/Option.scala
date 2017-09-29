package ornicar.scalalib

trait OrnicarOption {
  @inline implicit def toOrnicarOption[A](o: Option[A]) = new ornicarOptionWrapper(o)
}

final class ornicarOptionWrapper[A](private val self: Option[A]) extends AnyVal {

  def ??[B: Zero](f: A ⇒ B): B = self.fold(Zero[B].zero)(f)

  def ifTrue(b: Boolean): Option[A] = self filter (_ ⇒ b)
  def ifFalse(b: Boolean): Option[A] = self filter (_ ⇒ !b)
}

object OrnicarOption extends OrnicarOption
