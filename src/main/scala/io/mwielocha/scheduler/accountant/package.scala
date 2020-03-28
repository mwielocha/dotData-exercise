package io.mwielocha.scheduler

import akka.actor.typed.ActorRef

package object accountant {
  type Accountant = ActorRef[Protocol]
}
