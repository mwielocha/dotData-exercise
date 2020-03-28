package io.mwielocha.scheduler.runner


import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.mwielocha.scheduler.counter.{Counter, GetSummary, Summary}
import io.mwielocha.scheduler.model.{Job, Pending, Running, Succeeded}
import io.mwielocha.scheduler.model
import io.mwielocha.scheduler.tracker.{GetStatus, Status, Tracker}
import io.mwielocha.scheduler.counter
import io.mwielocha.scheduler.runner
import io.mwielocha.scheduler.tracker
import org.scalatest.wordspec.AnyWordSpecLike

class RunnerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Runner" should {

    "submit a job" in {
      val trackerActor = testKit.spawn(Tracker())
      val counterActor = testKit.spawn(Counter())
      val runnerActor = testKit.spawn(Runner(counterActor, trackerActor))
      val responder = testKit.createTestProbe[tracker.Reply]()
      val id = Job.Id("id")
      runnerActor ! Submit(id, 0)
      eventually {
        trackerActor ! GetStatus(id, responder.ref)
        responder.expectMessage(Status(id, Running))
      }
    }

    "finish a job" in {
      val trackerActor = testKit.spawn(Tracker())
      val counterActor = testKit.spawn(Counter())
      val runnerActor = testKit.spawn(Runner(counterActor, trackerActor))
      val trackerResponder = testKit.createTestProbe[tracker.Reply]()
      val counterResponder = testKit.createTestProbe[counter.Reply]()
      val id = Job.Id("id")
      runnerActor ! Submit(id, 0)
      eventually {
        trackerActor ! GetStatus(id, trackerResponder.ref)
        trackerResponder.expectMessage(Status(id, Running))
      }
      runnerActor ! Finish(id, Succeeded)
      eventually {
        trackerActor ! GetStatus(id, trackerResponder.ref)
        trackerResponder.expectMessage(Status(id, Succeeded))
        counterActor ! GetSummary(counterResponder.ref)
        counterResponder.expectMessage(Summary(model.Summary(succeeded = 1)))
      }
    }

    "enqeue a job if no workers available" in {
      val trackerActor = testKit.spawn(Tracker())
      val counterActor = testKit.spawn(Counter())
      val runnerActor = testKit.spawn(Runner(counterActor, trackerActor, maxWorkers = 2))
      val trackerResponder = testKit.createTestProbe[tracker.Reply]()
      val counterResponder = testKit.createTestProbe[counter.Reply]()
      val running = for(n <- 0 until 2) yield Job.Id(s"running-$n")
      val pending = for(n <- 0 until 2) yield Job.Id(s"pending-$n")
      for(id <- running) runnerActor ! Submit(id, 1)
      runnerActor ! Submit(pending.head, 1)
      runnerActor ! Submit(pending.last, 2)

      eventually {
        for (id <- running) {
          trackerActor ! GetStatus(id, trackerResponder.ref)
          trackerResponder.expectMessage(Status(id, Running))
        }
      }

      eventually {
        for (id <- pending) {
          trackerActor ! GetStatus(id, trackerResponder.ref)
          trackerResponder.expectMessage(Status(id, Pending))
        }
      }

      runnerActor ! Finish(running.head, Succeeded)

      eventually {
        trackerActor ! GetStatus(running.head, trackerResponder.ref)
        trackerResponder.expectMessage(Status(running.head, Succeeded))
        trackerActor ! GetStatus(pending.head, trackerResponder.ref)
        trackerResponder.expectMessage(Status(pending.head, Pending))
        trackerActor ! GetStatus(pending.last, trackerResponder.ref)
        trackerResponder.expectMessage(Status(pending.last, Running))
        counterActor ! GetSummary(counterResponder.ref)
        counterResponder.expectMessage(Summary(model.Summary(succeeded = 1, running = 2, pending = 1)))
      }
    }

    "finish a batch of jobs" in {
      val trackerActor = testKit.spawn(Tracker())
      val counterResponder = testKit.createTestProbe[counter.Reply]()
      val counterActor = testKit.spawn(Counter())
      val total = 50
      val runnerActor = testKit.spawn(Runner(counterActor, trackerActor, maxWorkers = total))
      val jobs = for(n <- 0 until total) yield Job.Id(s"batch-$n")

      for(id <- jobs) runnerActor ! Submit(id, 0)
      for(id <- jobs) runnerActor ! Finish(id, Succeeded)

      eventually {
        counterActor ! GetSummary(counterResponder.ref)
        counterResponder.expectMessage(Summary(model.Summary(succeeded = total)))
      }

    }
  }
}
