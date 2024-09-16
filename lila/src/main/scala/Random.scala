package scalalib

object ThreadLocalRandom extends RandomApi:
  protected def impl = java.util.concurrent.ThreadLocalRandom.current()

/** Thread-safe, but the underlying impl, [[java.security.SecureRandom]], uses synchronized methods, which has
  * degraded performance under high contention. See [[ThreadLocalSecureRandom]].
  */
object SecureRandom extends RandomApi:
  protected val impl = java.security.SecureRandom.getInstanceStrong()

/** An alternative to [[SecureRandom]] which offers improved performance under high contention.
  */
object ThreadLocalSecureRandom extends RandomApi:
  private val store = new java.lang.ThreadLocal[java.util.Random]:
    override def initialValue = java.security.SecureRandom.getInstanceStrong()
  protected def impl = store.get

/** A deterministic random number generator for testing purposes.
  */
final class TestableRandom(seed: Long) extends RandomApi:
  protected val impl = new java.util.Random(seed)

sealed abstract class RandomApi:
  protected def impl: java.util.Random

  final def nextBoolean()     = impl.nextBoolean
  final def nextDouble()      = impl.nextDouble
  final def nextFloat()       = impl.nextFloat
  final def nextGaussian()    = impl.nextGaussian
  final def nextInt()         = impl.nextInt
  final def nextInt(n: Int)   = impl.nextInt(n)
  final def nextLong()        = impl.nextLong
  final def nextLong(l: Long) = impl.nextLong(l)

  final def nextBytes(len: Int): Array[Byte] =
    val bytes = new Array[Byte](len)
    impl.nextBytes(bytes)
    bytes

  final def nextString(len: Int): String =
    val randomImpl = impl
    val chars      = RandomApi.chars
    val arr        = new Array[Char](len)

    var i = 0
    while i < len do
      arr(i) = chars(randomImpl.nextInt(chars.length))
      i += 1
    String.valueOf(arr)

  final def shuffle[T, C](xs: IterableOnce[T])(using scala.collection.BuildFrom[xs.type, T, C]): C =
    scala.util.Random(impl).shuffle(xs)

  final def oneOf[A](seq: scala.collection.IndexedSeq[A]): Option[A] =
    val len = seq.length
    if len > 0 then Some(seq(impl.nextInt(len))) else None

  // odds(1) = 100% true
  // odds(2) = 50% true
  // odds(3) = 33% true
  final def odds(n: Int): Boolean = impl.nextFloat() * n < 1f

private object RandomApi:
  // private vals are accessed directly as a static field.
  // final doesn't currently affect bytecode, but it could in the future and is good practice.
  private final val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789".toCharArray()
