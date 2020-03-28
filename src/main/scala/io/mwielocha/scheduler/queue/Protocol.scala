package io.mwielocha.scheduler.queue

import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.runner.Runner

sealed trait Protocol
case class Dequeue(replyTo: Runner) extends Protocol
case class Enqueue(id: Job.Id, priority: Int) extends Protocol
