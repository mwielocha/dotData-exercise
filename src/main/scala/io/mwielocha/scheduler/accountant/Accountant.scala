package io.mwielocha.scheduler.accountant

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model
import io.mwielocha.scheduler.model.{Completed, Failed, Succeeded}

object Accountant {

  def apply(): Behavior[Protocol] =
    counting(model.Summary())

  def counting(summary: model.Summary): Behavior[Protocol] =
    Behaviors.receive {

      case (_, Update(failed, pending, running, succeeded)) =>
        counting(
          model.Summary(
            summary.failed + failed,
            summary.pending + pending,
            summary.running + running,
            summary.succeeded + succeeded
          )
        )

      case (_, GetSummary(replyTo)) =>
        replyTo ! Summary(summary)

        Behaviors.same
    }

}
