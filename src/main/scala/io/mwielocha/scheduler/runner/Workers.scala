package io.mwielocha.scheduler.runner

import io.mwielocha.scheduler.worker.Worker

case class Workers(idle: Set[Worker], busy: Set[Worker]) {
  val all: Set[Worker] = idle ++ busy
}
