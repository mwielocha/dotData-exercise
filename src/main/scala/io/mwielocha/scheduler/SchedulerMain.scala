package io.mwielocha.scheduler

import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.actor.typed.scaladsl.adapter._
import akka.http.scaladsl.Http
import akka.stream.{ActorMaterializer, Materializer}
import akka.{Done, actor}
import io.mwielocha.scheduler.api.Routes

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object SchedulerMain {

  def main(args: Array[String]): Unit = {

    ActorSystem[Done](Behaviors.setup { ctx =>

      implicit val system: ActorSystem[Nothing] = ctx.system
      implicit val classicSystem: actor.ActorSystem = system.toClassic
      implicit val materializer: Materializer = ActorMaterializer()
      import system.executionContext

      val routes = new Routes()

        for {
          _ <- Http().bindAndHandle(
            routes.routes(),
            "localhost",
            port = 8080
          )
        } yield Await.result(system.whenTerminated, Duration.Inf)

      Behaviors.receiveMessage {
        case Done =>
          Behaviors.stopped
      }

    }, "scheduler")
  }
}
