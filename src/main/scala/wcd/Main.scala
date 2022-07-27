package wcd

import zhttp.service.Server
import zio._

object Main extends ZIOAppDefault {

  override def run = for {
    args <- getArgs
    _ <- ZIO
      .fail("Missing argument! Path to the executable needs to be provided.")
      .when(args.isEmpty)

    // window size can be changed here
    repository <- CounterRepository.make(1.minute)

    // start http server reporting window word count
    _ <- Server.start(9000, HttpHandler.make(repository)).fork

    // execute binary and process events
    _ <- EventStream
      .fromCommand(args.head)
      .map(e => e.event.eventType -> e.event.word.split(" ").length)
      .mapZIO { case (key, count) => repository.update(key, count) }
      .runCollect
  } yield ()
}
