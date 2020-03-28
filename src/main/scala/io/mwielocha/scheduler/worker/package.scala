package io.mwielocha.scheduler

import akka.actor.typed.ActorRef

package object worker {

  type Worker = ActorRef[Protocol]

}
