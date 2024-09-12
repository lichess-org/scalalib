package scalalib

object ThreadLocalRandom extends RandomApi:
  protected[scalalib] def impl = java.util.concurrent.ThreadLocalRandom.current()

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

  def nextString(len: Int): String =
    val randomImpl = impl
    val chars      = RandomApi.chars
    val arr        = new Array[Char](len)

    var i = 0
    while i < len do
      arr(i) = chars(randomImpl.nextInt(chars.length))
      i += 1
    String.valueOf(arr)

  def shuffle[T, C](xs: IterableOnce[T])(using scala.collection.BuildFrom[xs.type, T, C]): C =
    scala.util.Random(impl).shuffle(xs)

  def oneOf[A](seq: scala.collection.IndexedSeq[A]): Option[A] =
    val len = seq.length
    if len > 0 then Some(seq(impl.nextInt(len))) else None

  // odds(1) = 100% true
  // odds(2) = 50% true
  // odds(3) = 33% true
  def odds(n: Int): Boolean = impl.nextFloat() * n < 1

private object RandomApi:
  // private vals are accessed directly as a static field.
  // final doesn't currently affect bytecode, but it could in the future and is good practice.
  private final val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
