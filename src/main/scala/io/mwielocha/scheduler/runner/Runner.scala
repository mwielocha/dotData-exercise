package io.mwielocha.scheduler.runner

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.counter.{Counter, Update}
import io.mwielocha.scheduler.model._
import io.mwielocha.scheduler.queue.{Dequeue, Enqueue, Queue}
import io.mwielocha.scheduler.worker
import io.mwielocha.scheduler.worker.Worker

object Runner {

  def apply(counter: Counter, maxWorkers: Int = 10, maxHistory: Int = 10): Behavior[Protocol] =
    Behaviors.setup { ctx =>

      val workers = for(n <- 0 until maxWorkers) yield
        ctx.spawn(Worker(counter), s"worker-$n")

      ctx.log.info("Created {} workers, now accepting jobs...", workers.size)

      val queue = ctx.spawn(Queue(counter), "queue")

      accepting(
        Set.empty,
        Set.empty,
        Seq.empty,
        queue,
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
    workers: Workers,
    maxHistory: Int
  ): Behavior[Protocol] =

    Behaviors.receive {

      case (ctx, Submit(id, _)) if workers.idle.nonEmpty =>

        val Workers(idle, busy) = workers
        val w = idle.head
        idle.head ! worker.Work(id, ctx.self)

        accepting(
          pending,
          running,
          completed,
          queue,
          Workers(
            idle - w,
            busy + w
          ),
          maxHistory
        )

      case (ctx, Submit(id, priotity)) =>

        queue ! Enqueue(id, priotity, ctx.self)

        Behaviors.same

      case (ctx, Working(id)) =>
        ctx.log.info("Execution started: {}", id)

        accepting(
          pending,
          running + id,
          completed,
          queue,
          workers,
          maxHistory
        )

      case (ctx, Finish(id, status)) =>

        for(w <- workers.all) w ! worker.Finish(id, status, ctx.self)

        Behaviors.same

      case (ctx, Done(id, status, w)) =>

        val Workers(idle, busy) = workers

        queue ! Dequeue(ctx.self)

        accepting(
          pending,
          running - id,
          (Completed(id, status) +: completed)
            .take(maxHistory),
          queue,
          Workers(
            idle + w,
            busy - w
          ),
          maxHistory
        )

      case (_, Enqueued(id)) =>
        accepting(
          pending + id,
          running,
          completed,
          queue,
          workers,
          maxHistory
        )

      case (_, Dequeued(id)) =>
        accepting(
          pending - id,
          running,
          completed,
          queue,
          workers,
          maxHistory
        )

      case (_, GetStatus(id, replyTo)) =>

        replyTo ! Status (
          if(running(id)) Running
          else if(pending(id)) Pending
          else completed.find(_.id == id)
            .map(_.status)
            .getOrElse(Unknown)
        )

        Behaviors.same

    }
}
