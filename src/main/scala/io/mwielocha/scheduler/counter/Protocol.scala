package io.mwielocha.scheduler.counter

import akka.actor.typed.ActorRef
import io.mwielocha.scheduler.model.{FinishedStatus, Job}

sealed trait Protocol
final case class Update(
  failed: Int = 0,
  pending: Int = 0,
  running: Int = 0,
  succeeded: Int = 0
) extends Protocol
final case class GetSummary(replyTo: ActorRef[Reply]) extends Protocol
