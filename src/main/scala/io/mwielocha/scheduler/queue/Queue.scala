package io.mwielocha.scheduler.queue

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.util.StablePriorityQueue
import io.mwielocha.scheduler.counter.{Counter, Update}
import io.mwielocha.scheduler.model.Submitted
import io.mwielocha.scheduler.runner.{Dequeued, Enqueued, Submit}

object Queue {

  def apply(counter: Counter): Behavior[Protocol] =
    enqueuing(counter, new StablePriorityQueue(100, Submitted.priorityOrdering))

  def enqueuing(counter: Counter, queue: StablePriorityQueue[Submitted]): Behavior[Protocol] =
    Behaviors.receive {

      case (ctx, Enqueue(id, priority, replyTo)) =>
          ctx.log.info("Enqueueing job {}", id)
          queue add Submitted(id, priority)
          counter ! Update(pending = 1)
          replyTo ! Enqueued(id)
        Behaviors.same

      case (_, _: Dequeue) if queue.isEmpty =>
        Behaviors.same

      case (ctx, Dequeue(replyTo)) =>
        val Submitted(id, priority) = queue.poll()
        ctx.log.info("Dequeueing job {}", id)
        replyTo ! Dequeued(id)
        replyTo ! Submit(id, priority)
        counter ! Update(pending = -1)
        Behaviors.same


    }

}
