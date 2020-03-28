package io.mwielocha.scheduler.tracker

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model._

object Tracker {

  def apply(maxHistory: Int = 100): Behavior[Protocol] =
    tracking(Set.empty, Set.empty, Seq.empty, maxHistory)

  def tracking(
    pending: Set[Job.Id],
    running: Set[Job.Id],
    completed: Seq[Completed],
    maxHistory: Int
  ): Behavior[Protocol] =
    Behaviors.receive {

      case (_, Track(id, Pending)) =>
        tracking(pending + id, running - id, completed, maxHistory)

      case (_, Track(id, Running)) =>
        tracking(pending - id, running + id, completed, maxHistory)

      case (ctx, Track(id, status: FinishedStatus)) =>

        ctx.log.debug("Tracking {} with status {}", id, status)

        tracking(
          pending - id,
          running - id,
          (Completed(id, status) +: completed)
            .take(maxHistory),
          maxHistory
        )

      case (_, Track(_, Unknown)) =>
        Behaviors.same

      case (_, GetStatus(id, replyTo)) =>

        replyTo ! Status(
          id,
          if(running(id)) Running
          else if(pending(id)) Pending
          else completed.find(_.id == id)
            .map(_.status)
            .getOrElse(Unknown)
        )

        Behaviors.same

    }


}
