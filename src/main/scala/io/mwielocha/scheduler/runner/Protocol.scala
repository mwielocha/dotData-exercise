package io.mwielocha.scheduler.runner

import io.mwielocha.scheduler.model.{Job, _}
import io.mwielocha.scheduler.worker.Worker

sealed trait Protocol

/** Submit a new job */
final case class Submit(id: Job.Id, priotity: Int) extends Protocol

/** Finish existing job */
final case class Finish(id: Job.Id, status: FinishedStatus) extends Protocol

/** Reply from worker: notify about job completion */
final case class Done(id: Job.Id, status: FinishedStatus, replyTo: Worker) extends Protocol