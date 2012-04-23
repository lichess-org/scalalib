package ornicar.scalalib

import scala.util.Random

object OrnicarRandom {

  def nextAsciiString(len: Int) = List.fill(len)(nextAsciiChar) mkString

  private def nextAsciiChar = (Random.nextInt(25) + 97).toChar
}
