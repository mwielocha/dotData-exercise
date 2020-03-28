package io.mwielocha.scheduler.runner

import java.util.UUID

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.mwielocha.scheduler.counter.{Counter, GetSummary, Summary}
import io.mwielocha.scheduler.model.{Job, Pending, Running, Succeeded}
import io.mwielocha.scheduler.model
import io.mwielocha.scheduler.{counter, runner}
import org.scalatest.wordspec.AnyWordSpecLike

class RunnerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {


  "Runner" should {

    "submit a job" in {
      val accountantActor = testKit.spawn(Counter())
      val runnerActor = testKit.spawn(Runner(accountantActor))
      val reponder = testKit.createTestProbe[runner.Reply]()
      val id = Job.Id("id")
      runnerActor ! Submit(id, 0)
      eventually {
        runnerActor ! GetStatus(id, reponder.ref)
        reponder.expectMessage(Status(Running))
      }
    }

    "finish a job" in {
      val runnerResponder = testKit.createTestProbe[runner.Reply]()
      val summaryReponder = testKit.createTestProbe[counter.Reply]()
      val accountantActor = testKit.spawn(Counter())
      val runnerActor = testKit.spawn(Runner(accountantActor))
      val id = Job.Id("id")
      runnerActor ! Submit(id, 0)
      eventually {
        runnerActor ! GetStatus(id, runnerResponder.ref)
        runnerResponder.expectMessage(Status(Running))
      }
      runnerActor ! Finish(id, Succeeded)
      eventually {
        runnerActor ! GetStatus(id, runnerResponder.ref)
        runnerResponder.expectMessage(Status(Succeeded))
        accountantActor ! GetSummary(summaryReponder.ref)
        summaryReponder.expectMessage(Summary(model.Summary(succeeded = 1)))
      }
    }

    "enqeue a job if no workers available" in {
      val runnerResponder = testKit.createTestProbe[runner.Reply]()
      val summaryReponder = testKit.createTestProbe[counter.Reply]()
      val counterActor = testKit.spawn(Counter())
      val runnerActor = testKit.spawn(Runner(counterActor, maxWorkers = 2))
      val running = for(n <- 0 until 2) yield Job.Id(s"running-$n")
      val pending = for(n <- 0 until 2) yield Job.Id(s"pending-$n")
      for(id <- running) runnerActor ! Submit(id, 1)
      runnerActor ! Submit(pending.head, 1)
      runnerActor ! Submit(pending.last, 2)

      eventually {
        for (id <- running) {
          runnerActor ! GetStatus(id, runnerResponder.ref)
          runnerResponder.expectMessage(Status(Running))
        }
      }

      eventually {
        for (id <- pending) {
          runnerActor ! GetStatus(id, runnerResponder.ref)
          runnerResponder.expectMessage(Status(Pending))
        }
      }

      runnerActor ! Finish(running.head, Succeeded)

      eventually {
        runnerActor ! GetStatus(running.head, runnerResponder.ref)
        runnerResponder.expectMessage(Status(Succeeded))
        runnerActor ! GetStatus(pending.head, runnerResponder.ref)
        runnerResponder.expectMessage(Status(Pending))
        runnerActor ! GetStatus(pending.last, runnerResponder.ref)
        runnerResponder.expectMessage(Status(Running))
        counterActor ! GetSummary(summaryReponder.ref)
        summaryReponder.expectMessage(Summary(model.Summary(succeeded = 1, running = 2, pending = 1)))
      }
    }
  }
}
