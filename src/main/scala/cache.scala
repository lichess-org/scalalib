package scalalib

import com.github.blemale.scaffeine.Scaffeine
import com.github.benmanes.caffeine.cache.Scheduler
import java.util.concurrent.Executor

object cache:

  def scaffeine(using Executor): Scaffeine[Any, Any] =
    scaffeineNoScheduler.scheduler(Scheduler.systemScheduler)

  def scaffeineNoScheduler(using ex: Executor): Scaffeine[Any, Any] =
    Scaffeine().executor(ex)
