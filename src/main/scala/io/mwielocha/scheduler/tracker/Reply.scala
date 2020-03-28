package io.mwielocha.scheduler.tracker

import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.model

sealed trait Reply
final case class Status(id: Job.Id, status: model.Status) extends Reply
