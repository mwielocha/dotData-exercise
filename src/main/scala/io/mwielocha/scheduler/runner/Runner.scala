package io.mwielocha.scheduler.runner

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.accountant.{Accountant, Update}
import io.mwielocha.scheduler.model._
import io.mwielocha.scheduler.queue.{Dequeue, Enqueue, Queue}
import io.mwielocha.scheduler.worker
import io.mwielocha.scheduler.worker.Worker

object Runner {

  def apply(accountant: Accountant, maxWorkers: Int = 10, maxHistory: Int = 10): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      val workers = for(n <- 0 until maxWorkers) yield
        ctx.spawn(Worker(), s"worker-$n")
      ctx.log.info("Created {} workers, now accepting jobs...", workers.size)
      val queue = ctx.spawn(Queue(), "queue")
      accepting(
        Set.empty,
        Set.empty,
        Seq.empty,
        queue,
        accountant,
        Workers(
          workers.toSet,
          Set.empty
        ),
        maxHistory
      )
    }


  def accepting(
    pending: Set[Job.Id],
    running: Set[Job.Id],
    completed: Seq[Completed],
    queue: Queue,
    accountant: Accountant,
    workers: Workers,
    maxHistory: Int
  ): Behavior[Protocol] =

    Behaviors.receive {

      case (ctx, Submit(id, _)) if workers.idle.nonEmpty =>

        val Workers(idle, busy) = workers
        val w = idle.head
        idle.head ! worker.Work(id, ctx.self)
        accountant ! Update(running = 1)

        accepting(
          pending,
          running + id,
          completed,
          queue,
          accountant,
          Workers(
            idle - w,
            busy + w
          ),
          maxHistory
        )

      case (_, Submit(id, priotity)) =>

        queue ! Enqueue(id, priotity)
        accountant ! Update(pending = 1)

        accepting(
          pending + id,
          running,
          completed,
          queue,
          accountant,
          workers,
          maxHistory
        )

      case (ctx, Working(id)) =>
        ctx.log.info("Execution started: {}", id)

        Behaviors.same

      case (ctx, Finish(id, status)) =>

        for(w <- workers.all) w ! worker.Finish(id, status, ctx.self)

        Behaviors.same

      case (ctx, Done(id, status, w)) =>

        val Workers(idle, busy) = workers

        queue ! Dequeue(ctx.self)

        accountant ! Update(
          running = -1,
          failed = if(status == Failed) 1 else 0,
          succeeded = if(status == Succeeded) 1 else 0
        )

        accepting(
          pending,
          running - id,
          (Completed(id, status) +: completed)
            .take(maxHistory),
          queue,
          accountant,
          Workers(
            idle + w,
            busy - w
          ),
          maxHistory
        )

      case (_, GetStatus(id, replyTo)) =>

        replyTo ! Status (
          if(running(id)) Running
          else if(pending(id)) Pending
          else completed.find(_.id == id).map(_.status)
            .getOrElse(Unknown)
        )

        Behaviors.same

    }
}
