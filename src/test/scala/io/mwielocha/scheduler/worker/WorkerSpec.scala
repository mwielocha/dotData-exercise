package io.mwielocha.scheduler.worker

import akka.actor.testkit.typed.scaladsl.ScalaTestWithActorTestKit
import io.mwielocha.scheduler.model.{Failed, FinishedStatus, Job, Succeeded}
import io.mwielocha.scheduler.runner
import io.mwielocha.scheduler.runner.{Working, Done}
import org.scalatest.wordspec.AnyWordSpecLike

class WorkerSpec extends ScalaTestWithActorTestKit with AnyWordSpecLike {

  "Worker" should {

    def verifyCorrectStatus(expected: FinishedStatus): Unit = {
      val worker = testKit.spawn(Worker())
      val reponder = testKit.createTestProbe[runner.Protocol]()
      val id = Job.Id("id")
      worker ! Work(id, reponder.ref)
      reponder.expectMessage(Working(id))
      worker ! Finish(id, expected, reponder.ref)
      reponder.expectMessage(Done(id, expected, worker))
    }

    "finish a job with a Failed status" in verifyCorrectStatus(Failed)
    "finish a job with a Succeeded status" in verifyCorrectStatus(Succeeded)
  }
}
