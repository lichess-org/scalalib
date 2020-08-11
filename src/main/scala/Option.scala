package ornicar.scalalib

trait OrnicarOption {
  implicit final def toOrnicarOption[A](o: Option[A]) = new OrnicarOptionWrapper(o)
  implicit final def toOrnicarZeroOption[A: Zero](o: Option[A]) = new OrnicarZeroOptionWrapper(o)
}

final class OrnicarOptionWrapper[A](private val self: Option[A]) extends AnyVal {

  def ??[B: Zero](f: A => B): B = self.fold(Zero[B].zero)(f)

  def ifTrue(b: Boolean): Option[A]  = self filter (_ => b)
  def ifFalse(b: Boolean): Option[A] = self filter (_ => !b)

  // typesafe getOrElse
  def |(default: A): A = self getOrElse default
}

final class OrnicarZeroOptionWrapper[A](private val self: Option[A])(implicit zero: Zero[A]) {

  def unary_~ = self getOrElse zero.zero
}
