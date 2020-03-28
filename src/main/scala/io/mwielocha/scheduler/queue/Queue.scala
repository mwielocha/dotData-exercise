package io.mwielocha.scheduler.queue

import akka.actor.typed.Behavior
import akka.actor.typed.scaladsl.Behaviors
import io.mwielocha.scheduler.model.Submitted
import io.mwielocha.scheduler.runner.Submit

import scala.collection.mutable

object Queue {

  def apply(): Behavior[Protocol] =
    enqueuing(mutable.PriorityQueue.empty)

  def enqueuing(queue: mutable.PriorityQueue[Submitted]): Behavior[Protocol] =
    Behaviors.receive {

      case (_, Enqueue(id, priority)) =>
          queue addOne Submitted(id, priority)
        Behaviors.same

      case (_, Dequeue(replyTo)) if queue.nonEmpty =>
        val Submitted(id, priority) = queue.dequeue()
        replyTo ! Submit(id, priority)
        Behaviors.same

      case _ =>
        Behaviors.same
    }

}
