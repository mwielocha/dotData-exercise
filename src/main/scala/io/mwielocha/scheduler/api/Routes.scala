package io.mwielocha.scheduler.api

import akka.actor.typed.ActorSystem
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives.{path, _}
import akka.http.scaladsl.server
import de.heikoseeberger.akkahttpcirce.ErrorAccumulatingCirceSupport
import io.mwielocha.scheduler.counter.{Counter, GetSummary}
import io.mwielocha.scheduler.runner.{Finish, Runner, Submit}
import io.mwielocha.scheduler.tracker.{GetStatus, Tracker}
import akka.actor.typed.scaladsl.AskPattern._
import akka.util.Timeout
import com.typesafe.scalalogging.LazyLogging
import io.mwielocha.scheduler.{counter, tracker}
import io.mwielocha.scheduler.model.Job

import scala.concurrent.duration._

class Routes(
  maxWorkers: Int = 10,
  maxHistory: Int = 10
)(implicit val system: ActorSystem[Nothing]) extends ErrorAccumulatingCirceSupport with LazyLogging {

  logger.info("Creating actor system with maxWorkers: {} and maxHistory: {}", maxWorkers, maxHistory)

  implicit val timeout: Timeout = 3.seconds

  private val trackerActor = system.systemActorOf(Tracker(), "tracker")
  private val counterActor = system.systemActorOf(Counter(), "counter")
  private val runnerActor = system.systemActorOf(Runner(
    counterActor,
    trackerActor,
    maxWorkers,
    maxHistory
  ), "runner")

  def routes(): server.Route =
    extractExecutionContext { implicit ec =>
      concat(
        post {
          (path("submitted") & entity(as[Submitted])) {
            case Submitted(id, priority) =>
              runnerActor ! Submit(id, priority)
              complete(StatusCodes.NoContent)
          } ~
            (path("finished") & entity(as[Finished])) {
              case Finished(id, status) =>
                runnerActor ! Finish(id, status)
                complete(StatusCodes.NoContent)
            }
        },
        get {
          path("summary") {
            complete {
              counterActor
                .ask[counter.Summary](GetSummary)
            }
          } ~
            (path("status" / Segment)) { id =>
              complete {
                trackerActor
                  .ask[tracker.Status](GetStatus(Job.Id(id), _))
              }
            }

        }
      )
    }

}
