package io.mwielocha.scheduler.model

case class Summary(
  failed: Int = 0,
  pending: Int = 0,
  running: Int = 0,
  succeeded: Int = 0
)
