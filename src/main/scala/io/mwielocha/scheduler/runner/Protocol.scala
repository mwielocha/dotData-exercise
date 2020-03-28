package io.mwielocha.scheduler.runner

import io.mwielocha.scheduler.model.{Job, _}
import io.mwielocha.scheduler.worker.Worker

sealed trait Protocol

final case class Submit(id: Job.Id, priotity: Int) extends Protocol
final case class Finish(id: Job.Id, status: FinishedStatus) extends Protocol

final case class Done(id: Job.Id, status: FinishedStatus, replyTo: Worker) extends Protocol