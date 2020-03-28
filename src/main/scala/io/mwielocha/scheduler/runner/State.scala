package io.mwielocha.scheduler.runner

import io.mwielocha.scheduler.model.{Failed, Job, Pending, Running, Status, Succeeded, Summary, Unknown}
import io.mwielocha.scheduler.worker.Worker

case class State(
  pending: Seq[Job], // todo: priority queue
  running: Map[Job.Id, Worker],
  finished: Seq[Job],
  workers: Seq[Worker]
) {

  def statusOf(id: Job.Id): Status =
    pending.find(_.id == id)
    .map(_ => Pending)
    .orElse {
      finished.find(_.id == id)
        .map(_.status)
    } getOrElse {
      if(running.contains(id))
        Running
      else Unknown
    }

  def summary: Summary =
    Summary(
      finished
        .count(_.status == Failed),
      pending.size,
      running.size,
      finished
        .count(_.status == Succeeded)
    )
}

object State {
  def apply(workers: Seq[Worker]) : State =
    State(Nil, Map.empty, Nil, workers)
}
