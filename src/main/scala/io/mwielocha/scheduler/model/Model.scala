package io.mwielocha.scheduler.model

import shapeless.tag.@@

trait Model[TagType, IdType] {
  type Id = IdType @@ TagType

  object Id {
    def apply(in: IdType): Id =
      shapeless.tag[TagType][IdType](in)
  }
}
