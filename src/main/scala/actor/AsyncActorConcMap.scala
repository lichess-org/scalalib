package scalalib
package actor

import alleycats.Zero
import scala.concurrent.{ ExecutionContext, Future, Promise }
import java.util.concurrent.ConcurrentHashMap
import java.util.function.Function
import scala.jdk.CollectionConverters.*
import scalalib.extensions.*
import scalalib.future.FutureExtension.*

trait TellMap[Id]:
  def tell(id: Id, msg: Matchable): Unit

final class AsyncActorConcMap[Id, D <: AsyncActor](
    mkAsyncActor: Id => D,
    initialCapacity: Int
) extends TellMap[Id]:

  def tell(id: Id, msg: Matchable): Unit = getOrMake(id) ! msg

  def getOrMake(id: Id): D = asyncActors.computeIfAbsent(id, loadFunction)

  def getIfPresent(id: Id): Option[D] = Option(asyncActors.get(id))

  def tellIfPresent(id: Id, msg: => Matchable): Unit = getIfPresent(id).foreach(_ ! msg)

  def tellAll(msg: Matchable) = asyncActors.forEachValue(16, _ ! msg)

  def tellIds(ids: Seq[Id], msg: Matchable): Unit = ids.foreach { tell(_, msg) }

  def ask[A](id: Id)(makeMsg: Promise[A] => Matchable): Future[A] = getOrMake(id).ask(makeMsg)

  def askIfPresent[A](id: Id)(makeMsg: Promise[A] => Matchable): Future[Option[A]] =
    getIfPresent(id).soFu:
      _.ask(makeMsg)

  def askIfPresentOrZero[A: Zero](id: Id)(makeMsg: Promise[A] => Matchable): Future[A] =
    askIfPresent(id)(makeMsg).dmap(_.orZero)

  def exists(id: Id): Boolean = asyncActors.get(id) != null

  def foreachKey(f: Id => Unit): Unit =
    asyncActors.forEachKey(16, f(_))

  def tellAllWithAck(makeMsg: Promise[Unit] => Matchable)(using ExecutionContext): Future[Int] =
    Future
      .sequence(asyncActors.values.asScala.map(_.ask(makeMsg)))
      .map(_.size)

  def size: Int = asyncActors.size()

  def loadOrTell(id: Id, load: () => D, tell: D => Unit): Unit =
    asyncActors.compute(
      id,
      (_, a) =>
        Option(a).fold(load()) { present =>
          tell(present)
          present
        }
    )

  def terminate(id: Id, lastWill: AsyncActor => Unit): Unit =
    asyncActors.computeIfPresent(
      id,
      (_, d) =>
        lastWill(d)
        nullD
    )

  private val asyncActors = ConcurrentHashMap[Id, D](initialCapacity)

  private val loadFunction = new Function[Id, D]:
    def apply(k: Id) = mkAsyncActor(k)

  // used to remove entries
  var nullD: D = scala.compiletime.uninitialized
