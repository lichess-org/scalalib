package ornicar.scalalib

import alleycats.Zero

extension [A](self: Option[A])

  def ??[B: Zero](f: A => B): B = self.fold(Zero[B].zero)(f)

  def ifTrue(b: Boolean): Option[A]  = self filter (_ => b)
  def ifFalse(b: Boolean): Option[A] = self filter (_ => !b)

  // typesafe getOrElse
  def |(default: => A): A = self getOrElse default

  def unary_~(implicit z: Zero[A]): A = self getOrElse z.zero
