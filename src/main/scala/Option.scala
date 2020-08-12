package ornicar.scalalib

trait OrnicarOption {
  implicit final def toOrnicarOption[A](o: Option[A]) = new OrnicarOptionWrapper(o)
}

final class OrnicarOptionWrapper[A](private val self: Option[A]) extends AnyVal {

  def ??[B: Zero](f: A => B): B = self.fold(Zero[B].zero)(f)

  def ifTrue(b: Boolean): Option[A]  = self filter (_ => b)
  def ifFalse(b: Boolean): Option[A] = self filter (_ => !b)

  // typesafe getOrElse
  def |(default: => A): A = self getOrElse default

  def unary_~(implicit z: Zero[A]): A   = self getOrElse z.zero
}
