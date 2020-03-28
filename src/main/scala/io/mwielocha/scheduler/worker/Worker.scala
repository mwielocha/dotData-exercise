package io.mwielocha.scheduler.worker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.counter.{Counter, Update}
import io.mwielocha.scheduler.model.{Failed, Job, Running, Succeeded}
import io.mwielocha.scheduler.runner.Done
import io.mwielocha.scheduler.tracker.{Track, Tracker}

object Worker {

  def apply(
    counter: Counter,
    tracker: Tracker
  ): Behavior[Protocol] =
    idle(counter, tracker)

  def idle(
    counter: Counter,
    tracker: Tracker
  ): Behavior[Protocol] =

    Behaviors.receive {
      case (_, Work(id, _)) =>
        counter ! Update(running = 1)
        tracker ! Track(id, Running)
        working(counter, tracker, id)
      case (_, _: Finish) =>
        Behaviors.same
    }

  def working(
    counter: Counter,
    tracker: Tracker,
    currentJobId: Job.Id
  ): Behavior[Protocol] =

    Behaviors.receive {
      case (ctx, Finish(id, status, replyTo)) if id == currentJobId =>
        replyTo ! Done(id, status, ctx.self)
        counter ! Update(
          running = -1,
          failed = if(status is Failed) 1 else 0,
          succeeded = if(status is Succeeded) 1 else 0
        )
        tracker ! Track(id, status)
        idle(counter, tracker)
      case _ =>
        Behaviors.same
    }

}
