package ornicar.scalalib

import scala.util.{ Random => ScalaRandom }
import scala.math.round

object Random {

  private val chars: IndexedSeq[Char] = (('0' to '9') ++ ('a' to 'z'))
  private val nbChars = chars.size

  def nextString(len: Int) = List.fill(len)(nextChar) mkString

  def nextChar = chars(ScalaRandom nextInt nbChars)

  def approximatly(ratio: Float = 0.1f) = new {

    def apply(number: Double): Double =
      number + (ratio * number * 2 * ScalaRandom.nextDouble) - (ratio * number)

    def apply(number: Float): Float =
      apply(number.toDouble).toFloat

    def apply(number: Int): Int =
      round(apply(number.toFloat))
  }
}
