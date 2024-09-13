package scalalib

import scala.collection.mutable.StringBuilder

object ThreadLocalRandom extends RandomApi:
  protected def impl = java.util.concurrent.ThreadLocalRandom.current()

object SecureRandom extends RandomApi:
  protected val impl = java.security.SecureRandom.getInstanceStrong()

private abstract class RandomApi:
  protected def impl: java.util.Random

  def nextBoolean()     = impl.nextBoolean
  def nextDouble()      = impl.nextDouble
  def nextFloat()       = impl.nextFloat
  def nextGaussian()    = impl.nextGaussian
  def nextInt()         = impl.nextInt
  def nextInt(n: Int)   = impl.nextInt(n)
  def nextLong()        = impl.nextLong
  def nextLong(l: Long) = impl.nextLong(l)

  def nextBytes(len: Int): Array[Byte] =
    val bytes = new Array[Byte](len)
    impl.nextBytes(bytes)
    bytes

  private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  private inline def nextAlphanumeric(): Char =
    chars.charAt(impl.nextInt(chars.length)) // Constant time

  def nextString(len: Int): String =
    val sb = StringBuilder(len)
    for _ <- 0 until len do sb += nextAlphanumeric()
    sb.result()

  def shuffle[T, C](xs: IterableOnce[T])(using scala.collection.BuildFrom[xs.type, T, C]): C =
    scala.util.Random(impl).shuffle(xs)

  def oneOf[A](vec: Vector[A]): Option[A] =
    if vec.nonEmpty then vec.lift(impl.nextInt(vec.size)) else None

  // odds(1) = 100% true
  // odds(2) = 50% true
  // odds(3) = 33% true
  def odds(n: Int): Boolean = impl.nextInt(n) == 0
