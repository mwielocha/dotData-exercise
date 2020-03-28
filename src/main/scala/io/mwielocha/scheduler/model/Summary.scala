package io.mwielocha.scheduler.model

case class Summary(
  failed: Int,
  pending: Int,
  running: Int,
  succeeded: Int
)
