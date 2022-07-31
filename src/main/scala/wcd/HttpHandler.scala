package wcd

import zhttp.http._
import zio.Duration
import zio.json._

object HttpHandler {
  implicit val encoder: JsonEncoder[WindowCount] = DeriveJsonEncoder.gen[WindowCount]

  case class WindowCount(size: Duration, eventCount: Map[String, Int])

  def make(repository: Repository[Int]): HttpApp[Any, Throwable] =
    Http.collectZIO[Request] { case Method.GET -> !! =>
      repository
        .getAll()
        .map(m => WindowCount(repository.duration(), m).toJson)
        .map(Response.json(_))
    }
}
