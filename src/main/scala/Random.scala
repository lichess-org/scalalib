package ornicar.scalalib

import scala.math.round
import scala.util.{ Random => ScalaRandom }

import java.security.SecureRandom

object Random {

  private val chars: IndexedSeq[Char] =
    "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz".toIndexedSeq
  private val nbChars = chars.size

  def nextChar = ScalaRandom.alphanumeric.head
  def nextString(len: Int) = ScalaRandom.alphanumeric.take(len).mkString

  private val secureRandom = new SecureRandom()

  def secureChar: Char = chars(secureRandom nextInt nbChars)
  def secureString(len: Int): String = List.fill(len)(secureChar) mkString

  def approximatly(ratio: Float = 0.1f) = new {

    def apply(number: Double): Double =
      number + (ratio * number * 2 * ScalaRandom.nextDouble) - (ratio * number)

    def apply(number: Float): Float =
      apply(number.toDouble).toFloat

    def apply(number: Int): Int =
      round(apply(number.toFloat))
  }
}
