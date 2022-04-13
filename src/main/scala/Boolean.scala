package ornicar.scalalib

import alleycats.Zero

extension (self: Boolean)
  def option[A](a: => A): Option[A]             = if (self) Some(a) else None
  def ??[A](a: => A)(implicit zero: Zero[A]): A = if (self) a else zero.zero
