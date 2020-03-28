package io.mwielocha.scheduler.api

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.testkit.ScalatestRouteTest
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.mwielocha.scheduler.model.{Failed, Job, Pending, Running, Succeeded}
import io.mwielocha.scheduler.{counter, tracker}
import org.scalatest.matchers.should.Matchers
import org.scalatest.wordspec.AnyWordSpec

class RoutesSpec extends AnyWordSpec with Matchers with ScalatestRouteTest with ErrorAccumulatingCirceSupport {

  implicit val classicSystem: akka.actor.typed.ActorSystem[Nothing] =
    akka.actor.typed.ActorSystem.wrap(system)

  val routes = new Routes(maxWorkers = 1)
  val id: Job.Id = Job.Id("some-job-id")
  val otherId: Job.Id = Job.Id("other-job-id")
  val pendingId: Job.Id = Job.Id("pending-job-id")

  "Scheduler" should {

    "submit a job" in {
      Post("/submitted", Submitted(id, 0)) ~> routes.routes() ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "return status" in {
      Get(s"/status/$id") ~> routes.routes() ~> check {
        responseAs[tracker.Status].status shouldBe Running
      }
    }

    "return summary" in {
      Get(s"/summary") ~> routes.routes() ~> check {
        responseAs[counter.Summary] shouldBe counter.Summary(running = 1)
      }
    }

    "finish a job" in {
      Post(s"/finished", Finished(id, Failed)) ~> routes.routes() ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "return finished status" in {
      Get(s"/status/$id") ~> routes.routes() ~> check {
        responseAs[tracker.Status].status shouldBe Failed
      }
    }

    "return finished summary" in {
      Get(s"/summary") ~> routes.routes() ~> check {
        responseAs[counter.Summary] shouldBe counter.Summary(failed = 1)
      }
    }

    "submit a second job" in {
      Post("/submitted", Submitted(otherId, 0)) ~> routes.routes() ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "return summary with running second job" in {
      Get(s"/summary") ~> routes.routes() ~> check {
        responseAs[counter.Summary] shouldBe counter.Summary(failed = 1, running = 1)
      }
    }

    "submit a third job" in {
      Post("/submitted", Submitted(pendingId, 0)) ~> routes.routes() ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "return status pending for third job" in {
      Get(s"/status/$pendingId") ~> routes.routes() ~> check {
        responseAs[tracker.Status].status shouldBe Pending
      }
    }

    "return summary with pending job" in {
      Get(s"/summary") ~> routes.routes() ~> check {
        responseAs[counter.Summary] shouldBe counter.Summary(failed = 1, running = 1, pending = 1)
      }
    }

    "finish a second job" in {
      Post(s"/finished", Finished(otherId, Succeeded)) ~> routes.routes() ~> check {
        response.status shouldBe StatusCodes.NoContent
      }
    }

    "return summary with succeeded job" in {
      Get(s"/summary") ~> routes.routes() ~> check {
        responseAs[counter.Summary] shouldBe counter.Summary(failed = 1, running = 1, succeeded = 1)
      }
    }
  }
}
