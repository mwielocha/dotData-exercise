package io.mwielocha.scheduler.queue

import java.util.Comparator

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import akka.util.StablePriorityQueue
import io.mwielocha.scheduler.counter.{Counter, Update}
import io.mwielocha.scheduler.model.{Pending, Submitted}
import io.mwielocha.scheduler.runner.Submit
import io.mwielocha.scheduler.tracker.{Track, Tracker}

object Queue {

  def apply(counter: Counter, tracker: Tracker)(implicit comparator: Comparator[Submitted]): Behavior[Protocol] =
    enqueuing(counter, tracker, new StablePriorityQueue(100, comparator))

  def enqueuing(counter: Counter, tracker: Tracker, queue: StablePriorityQueue[Submitted]): Behavior[Protocol] =
    Behaviors.receive {

      case (ctx, Enqueue(id, priority, _)) =>
          ctx.log.debug("Enqueueing job {}", id)
          queue add Submitted(id, priority)
          counter ! Update(pending = 1)
          tracker ! Track(id, Pending)
        Behaviors.same

      case (_, _: Dequeue) if queue.isEmpty =>
        Behaviors.same

      case (ctx, Dequeue(replyTo)) =>
        val Submitted(id, priority) = queue.poll()
        ctx.log.debug("Dequeueing job {}", id)
        replyTo ! Submit(id, priority)
        counter ! Update(pending = -1)
        Behaviors.same


    }

}
