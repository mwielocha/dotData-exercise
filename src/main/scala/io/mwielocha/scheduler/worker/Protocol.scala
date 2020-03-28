package io.mwielocha.scheduler.worker

import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.model._
import io.mwielocha.scheduler.runner.Runner

sealed trait Protocol {
  def replyTo: Runner
}

/** Change state into working */
final case class Work(
  id: Job.Id,
  replyTo: Runner
) extends Protocol

/** Mark job as completed, change state to idle */
final case class Finish(
  id: Job.Id,
  status: FinishedStatus,
  replyTo: Runner
) extends Protocol