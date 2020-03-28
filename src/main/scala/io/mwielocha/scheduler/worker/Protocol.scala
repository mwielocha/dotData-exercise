package io.mwielocha.scheduler.worker

import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.model._
import io.mwielocha.scheduler.runner.Runner

sealed trait Protocol {
  def replyTo: Runner
}
final case class Work(
  id: Job.Id,
  replyTo: Runner
) extends Protocol

final case class Finish(
  id: Job.Id,
  status: FinishedStatus,
  replyTo: Runner
) extends Protocol