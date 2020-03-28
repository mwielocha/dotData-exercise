package io.mwielocha.scheduler.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.runner.{Executing, Finished}

object Worker {

  def apply(): Behavior[Protocol] = idle

  def idle: Behavior[Protocol] =
    Behaviors.receive {
      case (_, Execute(id, replyTo)) =>
        replyTo ! Executing(id)
        working(id)
    }

  def working(id: Job.Id): Behavior[Protocol] =
    Behaviors.receive {
      case (ctx, Finish(status, replyTo)) =>
        replyTo ! Finished(id, status, ctx.self)
        idle
    }

}
