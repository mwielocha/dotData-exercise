package io.mwielocha.scheduler.tracker

import akka.actor.typed.ActorRef
import io.mwielocha.scheduler.model.{Job, Status}

sealed trait Protocol
final case class Track(
  id: Job.Id,
  status: Status
) extends Protocol

final case class GetStatus(
  id: Job.Id,
  replyTo: ActorRef[Reply]
) extends Protocol
