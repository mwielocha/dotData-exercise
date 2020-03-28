package io.mwielocha.scheduler.model

import shapeless.tag.@@

trait Model[TagType, IdType] {
  type Id = IdType @@ TagType
}
