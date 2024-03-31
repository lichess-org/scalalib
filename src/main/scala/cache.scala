package scalalib

import com.github.blemale.scaffeine.Scaffeine
import com.github.benmanes.caffeine.cache.Scheduler
import scala.concurrent.ExecutionContextExecutor

object cache:

  def scaffeine(using ExecutionContextExecutor): Scaffeine[Any, Any] =
    scaffeineNoScheduler.scheduler(Scheduler.systemScheduler)

  def scaffeineNoScheduler(using ecx: ExecutionContextExecutor): Scaffeine[Any, Any] =
    Scaffeine().executor(ecx)
