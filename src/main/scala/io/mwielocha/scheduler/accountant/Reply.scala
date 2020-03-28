package io.mwielocha.scheduler.accountant

import io.mwielocha.scheduler.model

sealed trait Reply
final case class Summary(
  summary: model.Summary
) extends Reply
