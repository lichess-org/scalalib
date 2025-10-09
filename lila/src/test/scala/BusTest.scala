package scalalib

import scala.concurrent.{ Future, Promise, ExecutionContext }
export scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

import scalalib.future.FutureAfter
import scalalib.bus.{ Bus as BusClass, NotBuseable }

def dummyFutureAfter(using ec: ExecutionContext): FutureAfter =
  [T] =>
    (_: FiniteDuration) =>
      (thunk: () => Future[T]) =>
        Future {
          // don't care about duration for tests
          thunk()
        }.flatten

given FutureAfter = dummyFutureAfter

case class A(i: Int)
case class B(s: String)
case class C(i: Int, s: String, f: Float)
case class D(init: Int, p: Promise[Int])
case class E(zombo: String)
case class Impossible(x: String) extends NotBuseable
case class NoAsk(init: Int, p: Promise[Int]) extends NotBuseable

enum Foo:
  case Bar(i: Int)
  case Baz(s: String)

class BusTest extends munit.FunSuite:
  import typemap.typeName

  test("Bus"):
    val Bus = BusClass(64)
    val a = A(1)
    var aResult: Option[A] = None
    val b = B("This is a B")
    var bResult: Option[B] = None
    val c = C(3, "4", 5.0f)
    var cResult: Option[C] = None
    val foo = Foo.Baz("baz")
    var fooResult: Option[Foo] = None

    val e = E("zombo")
    var eResult: Option[E] = None

    Bus.sub[A] { case x: A => aResult = Some(x) }
    Bus.sub[B] { case _: B => bResult = Some(b) }
    Bus.sub[C] { case C(i, s, f) => cResult = Some(C(i, s, f)) }
    Bus.sub[D] { case D(init, p) => p.completeWith(Future.successful(init + 42)) }
    Bus.sub[Foo] { case Foo.Bar(i) => fooResult = Some(Foo.Bar(i)) }

    val subE = Bus.sub[E] { case e: E => eResult = Some(e) }
    // check one subscriber
    val expectedSubE = Bus.entries.unsafeMap.get(typeName[E])
    assertEquals(Option(Set(subE.tellable)), expectedSubE)
    Bus.unsub(subE)
    // check for None
    val expectedSubENone = Bus.entries.unsafeMap.get(typeName[E])
    assertEquals(None, expectedSubENone)
    Bus.pub(e)
    assertEquals(eResult, None)

    Bus.pub(a)
    Bus.pub(b)
    assertNoDiff(
      compileErrors(" Bus.pub(b, \"bb\")"),
      """error: No given instance of type scala.util.NotGiven[(scalalib.B, String) <:< Tuple] was found for parameter x$2 of method pub in class Bus
 Bus.pub(b, "bb")
                ^""".stripMargin
    )

    Bus.pub(c)
    Bus.pub(foo)
    assertEquals(aResult, Some(a))
    assertEquals(bResult, Some(b))
    assertEquals(cResult, Some(c))
    assertEquals(fooResult, None)
    Bus.sub[Foo] { case Foo.Baz(s) => fooResult = Some(Foo.Baz(s)) }
    Bus.pub(foo)
    assertEquals(fooResult, Some(foo))
    Bus.ask[Int, D](D(6, _)).foreach { x => assertEquals(x, 48) }

    assertNoDiff(
      compileErrors(
        "Bus.sub:\n" +
          "  case e: E       => eResult = Some(e)\n" +
          "  case A(i)       => eResult = None\n" +
          "  case B(_)       => eResult = None\n" +
          "  case C(_, _, _) => eResult = None\n" +
          "  case D(_, _)    => eResult = None"
      ),
      """error: The type scala.Matchable should be case class, or enum (NOT enum member)
      compileErrors(
                  ^""".stripMargin
    )

    assertNoDiff(
      compileErrors("Bus.pub(Impossible(\"not buseable!!\"))"),
      """error: No given instance of type scala.util.NotGiven[scalalib.Impossible <:< scalalib.bus.NotBuseable] was found for parameter x$3 of method pub in class Bus
Bus.pub(Impossible("not buseable!!"))
                                    ^""".stripMargin
    )
    assertNoDiff(
      compileErrors("Bus.ask[Int, NoAsk](NoAsk(6, _))"),
      """error: No given instance of type scala.util.NotGiven[scalalib.NoAsk <:< scalalib.bus.NotBuseable] was found for parameter x$5 of method ask in class Bus
Bus.ask[Int, NoAsk](NoAsk(6, _))
                               ^"""
    )

  test("Bus.exists"):
    val Bus = BusClass(64)
    val chan = "subscribeFunDyn:test:exists"
    assert(!Bus.exists(chan))
    Bus.subscribeFunDyn(chan):
      case _ =>
    assert(Bus.exists(chan))
    assert(!Bus.exists("subscribeFunDyn:test:not-exists"))
