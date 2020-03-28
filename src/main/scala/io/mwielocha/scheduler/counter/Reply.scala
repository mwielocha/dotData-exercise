package io.mwielocha.scheduler.counter

import io.mwielocha.scheduler.model

sealed trait Reply
final case class Summary(
  summary: model.Summary
) extends Reply
