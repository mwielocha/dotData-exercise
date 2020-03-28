package io.mwielocha.scheduler.runner

import io.mwielocha.scheduler.model

sealed trait Reply

final case class Status(status: model.Status) extends Reply
final case class Summary(status: model.Summary) extends Reply
