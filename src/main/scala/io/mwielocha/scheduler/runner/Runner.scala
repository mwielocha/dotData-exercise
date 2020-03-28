package io.mwielocha.scheduler.runner

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.counter.Counter
import io.mwielocha.scheduler.queue.{Dequeue, Enqueue, Queue}
import io.mwielocha.scheduler.tracker.Tracker
import io.mwielocha.scheduler.worker
import io.mwielocha.scheduler.worker.Worker

object Runner {

  def apply(counter: Counter, tracker: Tracker, maxWorkers: Int = 10, maxHistory: Int = 10): Behavior[Protocol] =
    Behaviors.setup { ctx =>

      val workers = for(n <- 0 until maxWorkers) yield
        ctx.spawn(Worker(counter, tracker), s"worker-$n")

      ctx.log.info("Created {} workers, now accepting jobs...", workers.size)

      val queue = ctx.spawn(Queue(counter, tracker), "queue")

      accepting(
        queue,
        Workers(
          workers.toSet,
          Set.empty
        ),
        maxHistory
      )
    }


  def accepting(
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

      case (ctx, Finish(id, status)) =>

        for(w <- workers.all) w ! worker.Finish(id, status, ctx.self)

        Behaviors.same

      case (ctx, Done(_, _, w)) =>

        val Workers(idle, busy) = workers

        queue ! Dequeue(ctx.self)

        accepting(
          queue,
          Workers(
            idle + w,
            busy - w
          ),
          maxHistory
        )

    }
}
