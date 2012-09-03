package ornicar.scalalib

import scalaz.effects.{ IO ⇒ SIO }

trait IO {

  implicit def ornicarRichIOUnit(iou: SIO[Unit]) = new {

    def doIf(cond: Boolean) = if (cond) iou else SIO.ioPure

    def doUnless(cond: Boolean) = if (cond) SIO.ioPure else iou
  }
}
