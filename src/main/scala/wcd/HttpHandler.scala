package wcd

import zhttp.http._
import zio.Duration
import zio.json._

object HttpHandler {
  implicit val encoder: JsonEncoder[Window] = DeriveJsonEncoder.gen[Window]

  case class Window(size: Duration, eventCount: Map[String, Int])

  def make(repository: Repository): HttpApp[Any, Throwable] =
    Http.collectZIO[Request] { case Method.GET -> !! =>
      repository
        .getAll()
        .map(m => Window(repository.duration(), m).toJson)
        .map(Response.json(_))
    }
}
