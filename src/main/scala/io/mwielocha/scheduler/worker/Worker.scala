package io.mwielocha.scheduler.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.counter.{Counter, Update}
import io.mwielocha.scheduler.model.{Failed, Job, Succeeded}
import io.mwielocha.scheduler.runner.{Done, Working}

object Worker {

  def apply(counter: Counter): Behavior[Protocol] = idle(counter)

  def idle(counter: Counter): Behavior[Protocol] =
    Behaviors.receive {
      case (_, Work(id, replyTo)) =>
        counter ! Update(running = 1)
        replyTo ! Working(id)
        working(counter, id)
      case (_, _: Finish) =>
        Behaviors.same
    }

  def working(counter: Counter, currentJobId: Job.Id): Behavior[Protocol] =
    Behaviors.receive {
      case (ctx, Finish(id, status, replyTo)) if id == currentJobId =>
        replyTo ! Done(id, status, ctx.self)
        counter ! Update(
          running = -1,
          failed = if(status == Failed) 1 else 0,
          succeeded = if(status == Succeeded) 1 else 0
        )
        idle(counter)
      case _ =>
        Behaviors.same
    }

}
