package io.mwielocha.scheduler.worker

import io.mwielocha.scheduler.model.{FinishedStatus, Job}
import io.mwielocha.scheduler.runner.Runner

sealed trait Protocol {
  def replyTo: Runner
}
final case class Execute(
  id: Job.Id,
  replyTo: Runner
) extends Protocol

final case class Finish(
  status: FinishedStatus,
  replyTo: Runner
) extends Protocol