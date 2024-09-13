package scalalib

class RandomTest extends munit.FunSuite:

  test("different threads get different randoms"):
    val r1 = ThreadLocalRandom

    @volatile var r2: RandomApi = null
    val thread2                 = new Thread(() => r2 = ThreadLocalRandom)
    thread2.start()
    thread2.join()
    assertNotEquals(r1, r2)
