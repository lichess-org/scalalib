package scalalib
package bus

import scala.jdk.CollectionConverters.*
import scala.reflect.Typeable
import scala.util.NotGiven
import scala.concurrent.duration.*
import scala.concurrent.{ ExecutionContext, Future, Promise }

import typemap.{ MutableTypeMap, typeName, assertBuseable }

import scalalib.future.extensions.withTimeout
import scalalib.future.FutureAfter

type Channel = String

// constructor is private so instances can only be created by extending the `GivenChannel` trait
final class WithChannel[T](private val key: Channel):
  def channel: Channel = key

transparent trait GivenChannel[T](val channel: Channel):
  given WithChannel[T] = WithChannel[T](channel)

trait Tellable extends Any:
  def !(msg: Matchable): Unit

object Tellable:

  case class SyncActor(ref: scalalib.actor.SyncActor) extends Tellable:
    def !(msg: Matchable) = ref ! msg

  def apply(f: PartialFunction[Matchable, Unit]): Tellable = new:
    def !(msg: Matchable) = f.applyOrElse(msg, _ => ())

object Bus:

  type Payload            = Matchable
  type SubscriberFunction = PartialFunction[Payload, Unit]

final class Bus(initialCapacity: Int = 4096):

  import Bus.*

  inline def pub[T <: Payload](t: T)(using NotGiven[T <:< Tuple]): Unit =
    assertBuseable[T]
    bus.entries.get[T].foreach(_.foreach(_ ! t))

  inline def sub[T <: Payload: Typeable](f: PartialFunction[T, Unit]): Tellable =
    val buseableFunction: SubscriberFunction = buseableFunctionBuilder[T](f)
    subTellable[T](Tellable(buseableFunction))

  // LOGIC : It is up to the caller to make sure `T`'s channel is relevant to the `tellable`
  inline def subTellable[T <: Payload](tellable: Tellable): Tellable =
    assertBuseable[T]
    bus.entries.compute[T](_.fold(Set(tellable))(_ + tellable))
    tellable

  // extracted from `subscribe` to avoid warning about definition being duplicated at each callsite
  private def buseableFunctionBuilder[T <: Payload: Typeable](
      f: PartialFunction[T, Unit]
  ): PartialFunction[Payload, Unit] =
    case x: T =>
      // it's not always error when type T is enum, and matching only one variant
      f.applyOrElse(x, _ => ())
    // error because events are based by types
    case y => println(s"Subscribe error: Incorrect message type, wanted: ${typeName[T]}, received: $y")

  def publish2(payload: Payload, channel: Channel): Unit = bus.publish(payload, channel)

  export bus.{ size, subscribe, unsubscribe }

  def subscribe2(subscriber: Tellable, to: Channel*) =
    to.foreach(bus.subscribe(subscriber, _))

  // LOGIC : It is up to the caller to make sure `T`'s channel is relevant to the `tellable`
  inline def subscribeActor[T <: Payload](ref: scalalib.actor.SyncActor) =
    subTellable[T](Tellable.SyncActor(ref))

  def subscribeFun(to: Channel*)(f: SubscriberFunction): Tellable =
    val t = Tellable(f)
    subscribe2(t, to*)
    t

  def subscribeFuns(subscriptions: (Channel, SubscriberFunction)*): Unit =
    subscriptions.foreach: (channel, subscriber) =>
      subscribeFun(channel)(subscriber)

  inline def unsub[T <: Payload](subscriber: Tellable) =
    assertBuseable[T]
    bus.entries.computeIfPresent[T]: subs =>
      subs - subscriber

  def unsubscribe(subscriber: Tellable, from: Iterable[Channel]) =
    from.foreach:
      bus.unsubscribe(subscriber, _)

  def ask[A](channel: Channel, timeout: FiniteDuration = 2.second)(makeMsg: Promise[A] => Matchable)(using
      ExecutionContext,
      FutureAfter
  ): Future[A] =
    val promise = Promise[A]()
    val msg     = makeMsg(promise)
    publish2(msg, channel)
    promise.future.withTimeout(timeout, s"Bus.ask $channel $msg")

  inline def safeAsk[A, T <: Payload](makeMsg: Promise[A] => T, timeout: FiniteDuration = 2.second)(using
      ExecutionContext,
      FutureAfter
  ): Future[A] =
    val promise = Promise[A]()
    val msg     = makeMsg(promise)
    pub[T](msg)
    promise.future.withTimeout(timeout, s"Bus.safeAsk ${typeName[T]} $msg")

  private val bus = EventBus[Payload, Tellable](
    initialCapacity = initialCapacity,
    publish = (tellable, event) => tellable ! event
  )

final private class EventBus[Event, Subscriber](
    initialCapacity: Int,
    publish: (Subscriber, Event) => Unit
):

  val entries: MutableTypeMap[Set[Subscriber], ConcurrentMap.Backend] =
    MutableTypeMap.make(initialCapacity)
  def size = entries.unsafeMap.size()

  def subscribe(subscriber: Subscriber, channel: Channel): Unit =
    entries.unsafeMap.compute(channel): prev =>
      Some(prev.fold(Set(subscriber))(_ + subscriber))

  def unsubscribe(subscriber: Subscriber, channel: Channel): Unit =
    entries.unsafeMap.computeIfPresent(channel): subs =>
      val newSubs = subs - subscriber
      Option.when(newSubs.nonEmpty)(newSubs)

  def publish(event: Event, channel: Channel): Unit =
    entries.unsafeMap.get(channel).foreach(_.foreach(publish(_, event)))
