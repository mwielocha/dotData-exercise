package io.mwielocha.scheduler.model

sealed trait Job

final case class Submitted(
  id: Job.Id,
  priority: Int
) extends Job

final case class Completed(
  id: Job.Id,
  status: FinishedStatus
) extends Job

object Job extends Model[Job, String]

object Submitted {
  implicit val priorityOrdering: Ordering[Submitted] =
    Ordering.by[Submitted, Int](_.priority).reverse
}
