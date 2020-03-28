package io.mwielocha.scheduler

import akka.actor.typed.ActorRef

package object tracker {
  type Tracker = ActorRef[Protocol]
}
