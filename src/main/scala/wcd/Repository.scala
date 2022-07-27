package wcd

import zio._
import zio.concurrent.ConcurrentMap

trait Repository {
  def duration(): Duration
  def getAll(): Task[Map[String, Int]]
  def update(key: String, count: Int): Task[Unit]
}

object CounterRepository {
  def make(duration: Duration) = for {
    m <- ConcurrentMap.empty[String, Counter]
  } yield new CounterRepository(m, duration)
}

class CounterRepository(
    storage: ConcurrentMap[String, Counter],
    duration: Duration
) extends Repository {
  override def getAll() =
    storage.toList
      .flatMap(l =>
        ZIO
          .foreach(l) { case (str, counter) =>
            counter.getCount.map(str -> _)
          }
          .map(_.toMap)
      )

  override def update(key: String, count: Int) = for {
    c1 <- storage.get(key)
    _ <- c1 match {
      case Some(value) => value.mark(count)
      case None =>
        for {
          c2 <- WindowedCounter.make(duration)
          _  <- c2.mark(count)
          _  <- storage.put(key, c2)
        } yield ()
    }
  } yield ()

  override def duration() = duration
}
