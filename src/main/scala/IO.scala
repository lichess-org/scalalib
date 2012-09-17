package ornicar.scalalib

import scalaz.effects.{ IO ⇒ SIO }

trait IO {

  implicit def ornicarRichIOUnit(iou: SIO[Unit]) = new {

    def doIf(cond: Boolean): SIO[Unit] = if (cond) iou else SIO.ioPure pure Unit

    def doUnless(cond: Boolean): SIO[Unit] = if (cond) SIO.ioPure pure Unit else iou

    def inject[A](a: A): SIO[A] = iou map (_ => a)
  }
}
