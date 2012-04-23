package ornicar.scalalib

import scala.util.Random
import scala.math.round

object OrnicarRandom {

  def nextAsciiString(len: Int) = List.fill(len)(nextAsciiChar) mkString

  def nextAsciiChar = (Random.nextInt(25) + 97).toChar

  def approximatly(ratio: Float = 0.1f) = new {

    def apply(number: Double): Double =
      number + (ratio * number * 2 * Random.nextDouble) - (ratio * number)

    def apply(number: Float): Float =
      apply(number.toDouble).toFloat

    def apply(number: Int): Int =
      round(apply(number.toFloat))
  }
}
