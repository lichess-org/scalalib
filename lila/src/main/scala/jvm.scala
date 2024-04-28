package scalalib

import scala.jdk.CollectionConverters.*

object Jvm:

  case class ThreadGroup(name: String, states: Map[Thread.State, Int]):
    def total             = states.values.sum
    def running           = states.getOrElse(Thread.State.RUNNABLE, 0)
    override def toString = s"$name total: $total runnable: $running"

  def threadGroups(): List[ThreadGroup] = threadList()
    .map(thread => """-\d+$""".r.replaceAllIn(thread.getName, "") -> thread.getState)
    .groupBy(_._1)
    .view
    .map((name, states) => ThreadGroup(name, states.groupMapReduce(_._2)(_ => 1)(_ + _)))
    .toList
    .sortBy(-_.total)

  private def threadList(): List[Thread] = Thread
    .getAllStackTraces()
    .keySet()
    .asScala
    .toList
