package io.mwielocha.scheduler.runner

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.circe.Decoder.state
import io.mwielocha.scheduler.model._
import io.mwielocha.scheduler.worker
import io.mwielocha.scheduler.worker.Worker

object Runner {


  private val maxHistory = 10
  private val maxWorkers = 10

  def apply(): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      val workers = for(n <- 0 until maxWorkers) yield
        ctx.spawn(Worker(), s"worker-$n")
      ctx.log.info("Created {} workers, now accepting jobs...", workers.size)
      accepting(Seq.empty, Set.empty, Seq.empty, Workers(workers.toSet, Set.empty))
    }


  def accepting(
    pending: Seq[Submitted],
    running: Set[Job.Id],
    finished: Seq[Completed],
    workers: Workers
  ): Behavior[Protocol] =

    Behaviors.receive {

      case (ctx, Submit(id, _)) if workers.idle.nonEmpty =>

        val Workers(idle, busy) = workers
        val w = idle.head
        idle.head ! worker.Work(id, ctx.self)

        accepting(
          pending,
          running + id,
          finished,
          Workers(
            idle - w,
            busy + w
          )
        )

      case (_, Submit(id, priotity)) =>

        accepting(
          (pending :+ Submitted(id, priotity))
            .sorted,
          running,
          finished,
          workers
        )

      case (ctx, Working(id)) =>
        ctx.log.info("Execution started: {}", id)

        Behaviors.same

      case (ctx, Finish(id, status)) =>

        for(w <- workers.all) w ! worker.Finish(id, status, ctx.self)

        Behaviors.same

      case (ctx, Done(id, status, w)) =>

        val Workers(idle, busy) = workers

        val newPending = pending match {
          case head +: tail =>
              ctx.self ! Submit(head.id, head.priority)
            tail
          case Nil => pending
        }

        accepting(
          newPending,
          running - id,
          (Completed(id, status) +: finished)
            .take(maxHistory),
          Workers(
            idle + w,
            busy - w
          )
        )

      case (_, GetStatus(id, replyTo)) =>

        replyTo ! Status (
          if(running.contains(id)) Running
          else if(pending.exists(_.id == id)) Pending
          else finished.find(_.id == id).map(_.status).getOrElse(Unknown)
        )

        Behaviors.same

      case (_, GetSummary(replyTo)) =>



        Behaviors.same

    }
}
