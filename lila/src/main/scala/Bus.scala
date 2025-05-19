package scalalib
package bus

import scala.annotation.nowarn
import scala.reflect.Typeable
import scala.util.NotGiven
import scala.concurrent.duration.*
import scala.concurrent.{ ExecutionContext, Future, Promise }

import typemap.{ MutableTypeMap, typeName, assertBuseable }

import scalalib.future.extensions.withTimeout
import scalalib.future.FutureAfter

type Channel = String

// BC
// constructor is private so instances can only be created by extending the `GivenChannel` trait
final class WithChannel[T](private val key: Channel):
  def channel: Channel = key

// BC
transparent trait GivenChannel[T](val channel: Channel):
  given WithChannel[T] = WithChannel[T](channel)

// Marker trait to implement on types you specifically do not want to Bus
trait NotBuseable

trait Tellable extends Any:
  def !(msg: Matchable): Unit

final case class TypedTellable[T] private[bus] (tellable: Tellable) extends NotBuseable

private def makeTypedTellable[T](tellable: Tellable): TypedTellable[T] = TypedTellable(tellable)

object Tellable:

  case class SyncActor(ref: scalalib.actor.SyncActor) extends Tellable:
    def !(msg: Matchable) = ref ! msg

  def apply(f: PartialFunction[Matchable, Unit]): Tellable = new:
    def !(msg: Matchable) = f.applyOrElse(msg, _ => ())

object Bus:

  type Payload            = Matchable
  type SubscriberFunction = PartialFunction[Payload, Unit]

@nowarn("msg=unused implicit parameter")
final class Bus(initialCapacity: Int = 4096):

  import Bus.*

  inline def pub[T <: Payload](t: T)(using NotGiven[T <:< Tuple], NotGiven[T <:< NotBuseable]): Unit =
    assertBuseable[T]
    entries.get[T].foreach(_.foreach(_ ! t))

  inline def sub[T <: Payload: Typeable](f: PartialFunction[T, Unit])(using
      NotGiven[T <:< NotBuseable]
  ): TypedTellable[T] =
    val buseableFunction: SubscriberFunction = buseableFunctionBuilder[T](f)
    subTellable[T](Tellable(buseableFunction))

  // LOGIC : It is up to the caller to make sure `T`'s channel is relevant to the `tellable`
  inline def subTellable[T <: Payload](tellable: Tellable)(using
      NotGiven[T <:< NotBuseable]
  ): TypedTellable[T] =
    assertBuseable[T]
    entries.compute[T](subs => Some(subs.fold(Set(tellable))(_ + tellable)))
    makeTypedTellable[T](tellable)

  // extracted from `subscribe` to avoid warning about definition being duplicated at each callsite
  private def buseableFunctionBuilder[T <: Payload: Typeable](
      f: PartialFunction[T, Unit]
  ): PartialFunction[Payload, Unit] =
    case x: T =>
      // it's not always error when type T is enum, and matching only one variant
      f.applyOrElse(x, _ => ())
    // logic error in `scalalib`, because events are based by types
    case y =>
      println(s"Subscribe error: wanted ${typeName[T]}, received: $y. This is a bug, report to scalalib")

  // BC
  def publish = publishDyn

  def publishDyn(payload: Payload, channel: Channel): Unit =
    entries.unsafeMap.get(channel).foreach(_.foreach(_ ! payload))

  // BC
  def subscribe(subscriber: Tellable, to: Channel*): Unit =
    subscribeDyn(subscriber, to*)

  def subscribeDyn(subscriber: Tellable, to: Channel*): Unit =
    to.foreach:
      entries.unsafeMap.compute(_): prev =>
        Some(prev.fold(Set(subscriber))(_ + subscriber))

  // BC
  def subscribe(ref: scalalib.actor.SyncActor, to: Channel*): Unit =
    subscribeDyn(Tellable.SyncActor(ref), to*)

  // LOGIC : It is up to the caller to make sure `tellable` is expecting payload of type `T`
  inline def subscribeActor[T <: Payload](ref: scalalib.actor.SyncActor)(using
      NotGiven[T <:< NotBuseable]
  ): TypedTellable[T] =
    subTellable[T](Tellable.SyncActor(ref))

  // BC
  def subscribeFun(to: Channel*)(f: SubscriberFunction): Tellable =
    subscribeFunDyn(to*)(f)

  def subscribeFunDyn(to: Channel*)(f: SubscriberFunction): Tellable =
    val t = Tellable(f)
    subscribeDyn(t, to*)
    t

  // BC
  def subscribeFuns(subscriptions: (Channel, SubscriberFunction)*): Unit =
    subscriptions.foreach: (channel, subscriber) =>
      subscribeFun(channel)(subscriber)

  inline def unsub[T <: Payload](subscriber: TypedTellable[T])(using
      NotGiven[T <:< NotBuseable]
  ): Option[Set[Tellable]] =
    assertBuseable[T]
    unsubUnchecked[T](subscriber.tellable)

  // LOGIC : It is up to the caller to maka sure `tellable` was subscribed in `T` channel in the first place
  inline def unsubUnchecked[T <: Payload](subscriber: Tellable)(using
      NotGiven[T <:< NotBuseable]
  ): Option[Set[Tellable]] =
    assertBuseable[T]
    entries.computeIfPresent[T]: subs =>
      val subsLeft = subs - subscriber
      Option.when(subsLeft.nonEmpty)(subsLeft)

  def unsubscribeDyn(subscriber: Tellable, from: Iterable[Channel]): Unit =
    from.foreach:
      entries.unsafeMap.computeIfPresent(_): subs =>
        val newSubs = subs - subscriber
        Option.when(newSubs.nonEmpty)(newSubs)

  // BC
  def unsubscribe(subscriber: Tellable, from: Channel*): Unit =
    unsubscribeDyn(subscriber, from)

  // BC
  def unsubscribe(subscriber: Tellable, from: Iterable[Channel]): Unit =
    unsubscribeDyn(subscriber, from)

  // BC
  def ask[A](channel: Channel, timeout: FiniteDuration = 2.second)(
      makeMsg: Promise[A] => Matchable
  )(using
      ExecutionContext,
      FutureAfter
  ): Future[A] =
    askDyn[A](channel, timeout)(makeMsg)

  def askDyn[A](channel: Channel, timeout: FiniteDuration = 2.second)(
      makeMsg: Promise[A] => Matchable
  )(using
      ExecutionContext,
      FutureAfter
  ): Future[A] =
    val promise = Promise[A]()
    val msg     = makeMsg(promise)
    publishDyn(msg, channel)
    promise.future.withTimeout(timeout, s"Bus.askDyn $channel $msg")

  // TODO rename to `ask` once migration is complete
  inline def safeAsk[A, T <: Payload](makeMsg: Promise[A] => T, timeout: FiniteDuration = 2.second)(using
      ExecutionContext,
      FutureAfter,
      NotGiven[T <:< NotBuseable]
  ): Future[A] =
    val promise = Promise[A]()
    val msg     = makeMsg(promise)
    pub[T](msg)

    promise.future.withTimeout(timeout, s"Bus.safeAsk ${typeName[T]} $msg")

  private[scalalib] val entries: MutableTypeMap[Set[Tellable], ConcurrentMap.Backend] =
    MutableTypeMap.make(initialCapacity)

  def size(): Int = entries.unsafeMap.size()
