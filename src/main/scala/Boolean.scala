package ornicar.scalalib

trait OrnicarBoolean {
  @inline implicit def toOrnicarBoolean[A](b: Boolean) = new OrnicarBooleanWrapper(b)
}

final class OrnicarBooleanWrapper(private val self: Boolean) extends AnyVal {

  def option[A](a: => A): Option[A] = if (self) Some(a) else None

  def ??[A](a: => A)(implicit zero: Zero[A]): A = if (self) a else zero.zero
}
