package io.mwielocha.scheduler.counter

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model

object Counter {

  def apply(): Behavior[Protocol] =
    counting(Summary())

  def counting(summary: Summary): Behavior[Protocol] =
    Behaviors.receive {

      case (_, Update(failed, pending, running, succeeded)) =>
        counting(
          Summary(
            summary.failed + failed,
            summary.pending + pending,
            summary.running + running,
            summary.succeeded + succeeded
          )
        )

      case (_, GetSummary(replyTo)) =>
        replyTo ! summary

        Behaviors.same
    }

}
