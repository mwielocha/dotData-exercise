package io.mwielocha.scheduler.runner

import akka.actor.typed.ActorRef
import io.mwielocha.scheduler.model.{FinishedStatus, Job}
import io.mwielocha.scheduler.worker.Worker

sealed trait Protocol

final case class Submit(id: Job.Id, priotity: Int) extends Protocol
final case class Finish(id: Job.Id, status: FinishedStatus) extends Protocol

final case class GetStatus(id: Job.Id, replyTo: ActorRef[Reply]) extends Protocol
final case class GetSummary(replyTo: ActorRef[Reply]) extends Protocol

final case class Executing(id: Job.Id) extends Protocol
final case class Finished(id: Job.Id, status: FinishedStatus, replyTo: Worker) extends Protocol