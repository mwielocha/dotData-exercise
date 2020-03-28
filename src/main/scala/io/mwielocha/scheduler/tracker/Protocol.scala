package io.mwielocha.scheduler.tracker

import akka.actor.typed.ActorRef
import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.model

sealed trait Protocol
final case class Track(
  id: Job.Id,
  status: model.Status
) extends Protocol

final case class GetStatus(
  id: Job.Id,
  replyTo: ActorRef[Status]
) extends Protocol
