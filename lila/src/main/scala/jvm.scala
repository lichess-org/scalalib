package scalalib

import scala.jdk.CollectionConverters.*

object Jvm:

  case class ThreadGroup(name: String, states: Map[Thread.State, Int]):
    def total = states.values.sum
    def running = states.getOrElse(Thread.State.RUNNABLE, 0)
    def blocked = states.getOrElse(Thread.State.BLOCKED, 0)
    override def toString = s"$name total: $total runnable: $running blocked: $blocked"

  def threadGroups(): List[ThreadGroup] = threadList()
    .map(thread => """-\d+$""".r.replaceAllIn(thread.getName, "") -> thread.getState)
    .groupBy(_._1)
    .view
    .map((name, states) => ThreadGroup(name, states.groupMapReduce(_._2)(_ => 1)(_ + _)))
    .toList
    .sortBy(-_.total)
    .map: group =>
      val allStates = threadStates.map(state => state -> group.states.getOrElse(state, 0)).toMap
      ThreadGroup(group.name, allStates)

  def blocked: List[(String, List[String])] = Thread
    .getAllStackTraces()
    .asScala
    .filter: (thread, _) =>
      thread.getState == Thread.State.BLOCKED
    .map: (thread, stack) =>
      thread.getName -> stack.map(_.toString).toList
    .toList

  def blockedStr =
    val threads = blocked
    s"Found ${threads.size} blocked threads:\n" +
      threads
        .map: (name, stack) =>
          s"$name\n${stack.mkString("\n")}\n---------"
        .mkString("\n")

  private def threadList(): List[Thread] = Thread
    .getAllStackTraces()
    .keySet()
    .asScala
    .toList

  private val threadStates = List(
    Thread.State.NEW,
    Thread.State.RUNNABLE,
    Thread.State.BLOCKED,
    Thread.State.WAITING,
    Thread.State.TIMED_WAITING,
    Thread.State.TERMINATED
  )
