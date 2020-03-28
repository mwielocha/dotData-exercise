package io.mwielocha.scheduler.model

case class Job(
  id: Job.Id,
  status: Status,
  priority: Int
)

object Job extends Model[Job, String] {

  implicit val priorityOrdering: Ordering[Job] =
    Ordering.by[Job, Int](_.priority)

  def apply(id: Job.Id, priority: Int): Job =
    Job(id, Pending, priority)
}
