package wcd

import zio._
import zio.json._
import zio.process.{Command, CommandError}
import zio.stream.ZStream

object EventStream {
  implicit val payloadDecoder = DeriveJsonDecoder.gen[EventPayload]
  implicit val eventDecoder   = DeriveJsonDecoder.gen[Event]

  case class EventPayload(
      @jsonField("event_type") eventType: String,
      word: String
  )

  case class Event(event: EventPayload, timestamp: Long)

  def fromCommand(command: String): ZStream[Any, CommandError, Event] =
    Command(command).linesStream.flatMap(line =>
      line.fromJson[Event] match {
        case Left(message) =>
          ZStream.unwrap(ZIO.logWarning(message).as(ZStream.empty))
        case Right(value) => ZStream.succeed(value)
      }
    )
}
