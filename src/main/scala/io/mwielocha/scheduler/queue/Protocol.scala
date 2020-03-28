package io.mwielocha.scheduler.queue

import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.runner.Runner

sealed trait Protocol

/** Dequeue a pending job */
final case class Dequeue(replyTo: Runner) extends Protocol

/** Enqueue a pending job */
final case class Enqueue(id: Job.Id, priority: Int, replyTo: Runner) extends Protocol
