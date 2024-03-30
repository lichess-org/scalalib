package scalalib

import com.github.blemale.scaffeine.Scaffeine
import com.github.benmanes.caffeine.cache.Scheduler

object cache:

  def scaffeine: Scaffeine[Any, Any] =
    scaffeineNoScheduler.scheduler(Scheduler.systemScheduler)

  def scaffeineNoScheduler: Scaffeine[Any, Any] =
    Scaffeine().executor(defaultExecutor)

  // https://www.scala-lang.org/api/2.13.4/Executor%24.html#global:Executor
  private val defaultExecutor: scala.concurrent.ExecutionContextExecutor =
    scala.concurrent.ExecutionContext.getClass
      .getDeclaredMethod("opportunistic")
      .invoke(scala.concurrent.ExecutionContext)
      .asInstanceOf[scala.concurrent.ExecutionContextExecutor]
