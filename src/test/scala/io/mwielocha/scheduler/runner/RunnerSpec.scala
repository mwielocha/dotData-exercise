package io.mwielocha.scheduler.runner

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.mwielocha.scheduler.model.{Job, Running, Succeeded}
import org.scalatest.wordspec.AnyWordSpecLike

class RunnerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Runner" should {

    "submit a job" in {

      val runner = testKit.spawn(Runner())
      val reponder = testKit.createTestProbe[Reply]()
      val id = Job.Id("id")
      runner ! Submit(id, 0)
      runner ! GetStatus(id, reponder.ref)
      reponder.expectMessage(Status(Running))
    }

    "finish a job" in {

      val runner = testKit.spawn(Runner())
      val reponder = testKit.createTestProbe[Reply]()
      val id = Job.Id("id")
      runner ! Submit(id, 0)
      runner ! GetStatus(id, reponder.ref)
      reponder.expectMessage(Status(Running))
      runner ! Finish(id, Succeeded)
      eventually {
        runner ! GetStatus(id, reponder.ref)
        reponder.expectMessage(Status(Succeeded))
      }
    }
  }
}
