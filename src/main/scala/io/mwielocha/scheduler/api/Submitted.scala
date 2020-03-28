package io.mwielocha.scheduler.api

import io.mwielocha.scheduler.model.Job

case class Submitted(jobId: Job.Id, priority: Int)
