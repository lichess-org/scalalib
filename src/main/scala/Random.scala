package ornicar.scalalib

import scala.collection.mutable.StringBuilder

val ThreadLocalRandom = new RandomApi(java.util.concurrent.ThreadLocalRandom.current)

val SecureRandom = new RandomApi(new java.security.SecureRandom())

final class RandomApi(impl: java.util.Random):

  inline def nextBoolean(): Boolean  = impl.nextBoolean()
  inline def nextDouble(): Double    = impl.nextDouble()
  inline def nextFloat(): Float      = impl.nextFloat()
  inline def nextInt(): Int          = impl.nextInt()
  inline def nextInt(n: Int): Int    = impl.nextInt(n)
  inline def nextLong(): Long        = impl.nextLong()
  inline def nextGaussian(): Double  = impl.nextGaussian()
  inline def nextLong(n: Long): Long = impl.nextLong(n)

  def nextBytes(len: Int): Array[Byte] =
    val bytes = new Array[Byte](len)
    impl.nextBytes(bytes)
    bytes

  private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  private inline def nextAlphanumeric(): Char =
    chars charAt nextInt(chars.length) // Constant time

  def nextString(len: Int): String =
    val sb = new StringBuilder(len)
    for (_ <- 0 until len) sb += nextAlphanumeric()
    sb.result()

  def shuffle[T, C](xs: IterableOnce[T])(implicit bf: scala.collection.BuildFrom[xs.type, T, C]): C =
    new scala.util.Random(impl).shuffle(xs)

  def oneOf[A](vec: Vector[A]): Option[A] =
    if vec.nonEmpty then vec lift nextInt(vec.size) else None

  // odds(1) = 100% true
  // odds(2) = 50% true
  // odds(3) = 33% true
  def odds(n: Int): Boolean = nextInt(n) == 0
