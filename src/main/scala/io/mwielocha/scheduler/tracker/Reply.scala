package io.mwielocha.scheduler.tracker

import io.mwielocha.scheduler.model

sealed trait Reply
final case class Status(status: model.Status) extends Reply
