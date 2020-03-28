package io.mwielocha.scheduler.runner

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model.Job
import io.mwielocha.scheduler.worker
import io.mwielocha.scheduler.worker.Worker

object Runner {

  private val maxHistory = 10
  private val maxWorkers = 10

  def apply(): Behavior[Protocol] =
    Behaviors.setup { ctx =>
      val workers = for(n <- 0 to maxWorkers) yield
        ctx.spawn(Worker(), s"runner-$n")
      accepting(State(workers))
    }


  def accepting(state: State): Behavior[Protocol] =

    Behaviors.receive {

      case (ctx, Submit(id, _)) if state.workers.nonEmpty =>

        val State(pending, running, finished, w +: idle) = state

        w ! worker.Execute(id, ctx.self)

        accepting(
          State(
            pending,
            running + (id -> w),
            finished,
            idle
          )
        )

      case (_, Submit(id, _)) =>

        val State(pending, running, finished, idle) = state

        accepting(
          State(
            pending :+ id,
            running,
            finished,
            idle
          )
        )

      case (ctx, Finish(id, status)) =>

        val State(_, running, _, _) = state

        for(w <- running.get(id)) w ! worker.Finish(status, ctx.self)

        accepting(state)

      case (_, Finished(id, status, w)) =>

        val State(pending, running, finished, idle) = state

        accepting(
          State(
            pending,
            running - id,
            (Job(id, status, 0) +: finished)
              .take(maxHistory),
            idle :+ w
          )
        )

      case (_, GetStatus(id, replyTo)) =>

        replyTo ! Status(state.statusOf(id))

        accepting(state)

      case (_, GetSummary(replyTo)) =>

        replyTo ! Summary(state.summary)

        accepting(state)

    }
}
