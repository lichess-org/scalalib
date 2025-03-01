package scalalib
package bus

import scala.jdk.CollectionConverters.*
import scala.reflect.Typeable
import scala.concurrent.duration.*
import scala.concurrent.{ ExecutionContext, Future, Promise }
import com.github.benmanes.caffeine.cache.Scheduler
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

  def pub[T <: Payload](payload: T)(using wc: WithChannel[T]) =
    publish(payload, wc.channel)

  def sub[T <: Payload: Typeable](f: PartialFunction[T, Unit])(using wc: WithChannel[T]) =
    subscribeFun(wc.channel):
      case x: T => f.applyOrElse(x, _ => ())

  def publish(payload: Payload, channel: Channel): Unit = bus.publish(payload, channel)

  export bus.{ size, subscribe, unsubscribe }

  def subscribe(subscriber: Tellable, to: Channel*) =
    to.foreach(bus.subscribe(subscriber, _))

  def subscribe(ref: scalalib.actor.SyncActor, to: Channel*) =
    to.foreach(bus.subscribe(Tellable.SyncActor(ref), _))

  def subscribeFun(to: Channel*)(f: SubscriberFunction): Tellable =
    val t = Tellable(f)
    subscribe(t, to*)
    t

  def subscribeFuns(subscriptions: (Channel, SubscriberFunction)*): Unit =
    subscriptions.foreach: (channel, subscriber) =>
      subscribeFun(channel)(subscriber)

  def unsubscribe(subscriber: Tellable, from: Iterable[Channel]) =
    from.foreach:
      bus.unsubscribe(subscriber, _)

  def ask[A](channel: Channel, timeout: FiniteDuration = 2.second)(makeMsg: Promise[A] => Matchable)(using
      ExecutionContext,
      Scheduler,
      FutureAfter
  ): Future[A] =
    val promise = Promise[A]()
    val msg     = makeMsg(promise)
    publish(msg, channel)
    promise.future.withTimeout(timeout, s"Bus.ask $channel $msg")

  def safeAsk[A, T <: Payload](makeMsg: Promise[A] => T, timeout: FiniteDuration = 2.second)(using
      wc: WithChannel[T]
  )(using
      ExecutionContext,
      Scheduler,
      FutureAfter
  ): Future[A] =
    val promise = Promise[A]()
    val channel = wc.channel
    val msg     = makeMsg(promise)
    pub(msg)
    promise.future.withTimeout(timeout, s"Bus.safeAsk $channel $msg")

  private val bus = EventBus[Payload, Channel, Tellable](
    initialCapacity = 4096,
    publish = (tellable, event) => tellable ! event
  )

final private class EventBus[Event, Channel, Subscriber](
    initialCapacity: Int,
    publish: (Subscriber, Event) => Unit
):

  private val entries = scalalib.ConcurrentMap[Channel, Set[Subscriber]](initialCapacity)
  export entries.size

  def subscribe(subscriber: Subscriber, channel: Channel): Unit =
    entries.compute(channel): prev =>
      Some(prev.fold(Set(subscriber))(_ + subscriber))

  def unsubscribe(subscriber: Subscriber, channel: Channel): Unit =
    entries.computeIfPresent(channel): subs =>
      val newSubs = subs - subscriber
      Option.when(newSubs.nonEmpty)(newSubs)

  def publish(event: Event, channel: Channel): Unit =
    entries.get(channel).foreach(_.foreach(publish(_, event)))
