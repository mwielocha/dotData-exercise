package io.mwielocha.scheduler.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.runner.{Working, Done}

object Worker {

  def apply(): Behavior[Protocol] = idle

  def idle: Behavior[Protocol] =
    Behaviors.receive {
      case (_, Work(id, replyTo)) =>
        replyTo ! Working(id)
        working(id)
      case (_, _: Finish) =>
        Behaviors.same
    }

  def working(currentJobId: Job.Id): Behavior[Protocol] =
    Behaviors.receive {
      case (ctx, Finish(id, status, replyTo)) if id == currentJobId =>
        replyTo ! Done(id, status, ctx.self)
        idle
      case _ =>
        Behaviors.same
    }

}
