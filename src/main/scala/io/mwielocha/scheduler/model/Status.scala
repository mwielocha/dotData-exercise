package io.mwielocha.scheduler.model

sealed trait Status
sealed trait FinishedStatus extends Status

case object Pending extends Status
case object Running extends Status
case object Unknown extends Status

case object Failed extends FinishedStatus
case object Succeeded extends FinishedStatus

