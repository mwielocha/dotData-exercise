package io.mwielocha.scheduler

import io.circe.generic.extras.Configuration
import io.circe.generic.extras.semiauto._
import io.circe.{Codec, Decoder, Encoder}
import io.mwielocha.scheduler.counter.Summary
import io.mwielocha.scheduler.model.{Submitted => _, _}

package object api {

  private implicit val codecConfig: Configuration =
    Configuration.default.withSnakeCaseMemberNames

  implicit val jobIdDecoder: Decoder[Job.Id] =
    Decoder.decodeString.map(Job.Id(_))

  implicit val jobIdEncoder: Encoder[Job.Id] =
    Encoder.encodeString.contramap(identity)

  implicit val statusDecoder: Decoder[model.Status] =
    Decoder.decodeString.map {
      case  "FAILED" => Failed
      case  "PENDING" => Pending
      case  "UNKNOWN" => Unknown
      case  "RUNNING" => Running
      case  "SUCCEEDED" => Succeeded
    }

  implicit val statusEncoder: Encoder[model.Status] =
    Encoder.encodeString.contramap {
      case Failed => "FAILED"
      case Pending => "PENDING"
      case Unknown => "UNKNOWN"
      case Running => "RUNNING"
      case Succeeded => "SUCCEEDED"
    }

  implicit val finishedStatusDecoder: Decoder[model.FinishedStatus] =
    Decoder.decodeString.map {
      case  "FAILED" => Failed
      case  "SUCCEEDED" => Succeeded
    }

  implicit val finishedStatusEncoder: Encoder[model.FinishedStatus] =
    Encoder.encodeString.contramap {
      case Failed => "FAILED"
      case Succeeded => "SUCCEEDED"
    }

  implicit val finishedCoded: Codec[Finished] = deriveConfiguredCodec[Finished]
  implicit val submittedCoded: Codec[Submitted] = deriveConfiguredCodec[Submitted]

  implicit val summaryCoded: Codec[Summary] = deriveConfiguredCodec[Summary]

  implicit val trackerStatusEncoder: Encoder[tracker.Status] =
    deriveConfiguredEncoder[tracker.Status]

  implicit val trackerStatusDecoder: Decoder[tracker.Status] =
    deriveConfiguredDecoder[tracker.Status]


}
