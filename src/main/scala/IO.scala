package ornicar.scalalib

import scalaz.effects.{ IO ⇒ SIO }
import scalaz.Zero

trait IO extends scalaz.Zeros {

  implicit def ornicarRichIOUnit(iou: SIO[Unit]) = new {

    def doIf(cond: Boolean): SIO[Unit] = if (cond) iou else SIO.ioPure pure Unit

    def doUnless(cond: Boolean): SIO[Unit] = if (cond) SIO.ioPure pure Unit else iou

    def inject[A](a: A): SIO[A] = iou map (_ ⇒ a)
  }

  implicit def ornicarRichIOA[A](ioa: SIO[A]) = new {

    def void: SIO[Unit] = ioa map (_ ⇒ Unit)
  }

  val void: SIO[Unit] = SIO.ioPure pure Unit

  implicit def IOZero[A: Zero]: Zero[SIO[A]] = new Zero[SIO[A]] {
    val zero = SIO.ioPure pure ∅[A]
  }
}
