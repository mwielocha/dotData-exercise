package io.mwielocha.scheduler.api

import io.mwielocha.scheduler.model.{Job, FinishedStatus}

case class Finished(jobId: Job.Id, status: FinishedStatus)
