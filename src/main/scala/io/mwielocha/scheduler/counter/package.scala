package io.mwielocha.scheduler

import akka.actor.typed.ActorRef

package object counter {
  type Counter = ActorRef[Protocol]
}
