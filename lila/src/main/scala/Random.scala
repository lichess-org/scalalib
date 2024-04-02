package scalalib

import scala.collection.mutable.StringBuilder

val ThreadLocalRandom = RandomApi(java.util.concurrent.ThreadLocalRandom.current)

val SecureRandom = RandomApi(java.security.SecureRandom())

final class RandomApi(impl: java.util.Random):

  export impl.{ nextBoolean, nextDouble, nextFloat, nextGaussian, nextInt, nextLong }

  def nextBytes(len: Int): Array[Byte] =
    val bytes = new Array[Byte](len)
    impl.nextBytes(bytes)
    bytes

  private val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
  private inline def nextAlphanumeric(): Char =
    chars.charAt(nextInt(chars.length)) // Constant time

  def nextString(len: Int): String =
    val sb = StringBuilder(len)
    for _ <- 0 until len do sb += nextAlphanumeric()
    sb.result()

  def shuffle[T, C](xs: IterableOnce[T])(using scala.collection.BuildFrom[xs.type, T, C]): C =
    scala.util.Random(impl).shuffle(xs)

  def oneOf[A](vec: Vector[A]): Option[A] =
    if vec.nonEmpty then vec.lift(nextInt(vec.size)) else None

  // odds(1) = 100% true
  // odds(2) = 50% true
  // odds(3) = 33% true
  def odds(n: Int): Boolean = nextInt(n) == 0
