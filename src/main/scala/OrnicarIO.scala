package ornicar.scalalib

import scalaz.effects._

trait OrnicarIO {

  implicit def ornicarRichIOUnit(iou: IO[Unit]) = new {

    def doIf(cond: Boolean) = if (cond) iou else io()

    def doUnless(cond: Boolean) = if (cond) io() else iou
  }
}
