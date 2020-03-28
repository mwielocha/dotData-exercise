package io.mwielocha.scheduler.counter

import akka.actor.typed.ActorRef
import io.mwielocha.scheduler.model.{FinishedStatus, Job}

sealed trait Protocol

/** Update summary */
final case class Update(
  failed: Int = 0,
  pending: Int = 0,
  running: Int = 0,
  succeeded: Int = 0
) extends Protocol

/** Return summary */
final case class GetSummary(replyTo: ActorRef[Summary]) extends Protocol
