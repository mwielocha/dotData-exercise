package io.mwielocha.scheduler.queue

import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.runner.Runner

sealed trait Protocol
final case class Dequeue(replyTo: Runner) extends Protocol
final case class Enqueue(id: Job.Id, priority: Int, replyTo: Runner) extends Protocol
