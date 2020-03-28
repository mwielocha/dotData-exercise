package io.mwielocha.scheduler

import akka.actor.typed.ActorRef

package object queue {
  type Queue = ActorRef[Protocol]
}
