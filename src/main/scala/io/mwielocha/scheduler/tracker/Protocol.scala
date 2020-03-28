package io.mwielocha.scheduler.tracker

import akka.actor.typed.ActorRef
import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.model

sealed trait Protocol

/** Updated tracker for a specified job */
final case class Track(
  id: Job.Id,
  status: model.Status
) extends Protocol

/** Return job's status */
final case class GetStatus(
  id: Job.Id,
  replyTo: ActorRef[Status]
) extends Protocol
