package io.mwielocha.scheduler

import akka.actor.typed.ActorRef

package object runner {

  type Runner = ActorRef[Protocol]

}
