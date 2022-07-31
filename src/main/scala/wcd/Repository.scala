package wcd

import zio._
import zio.concurrent.ConcurrentMap

trait Repository[T] {
  def duration(): Duration
  def getAll(): Task[Map[String, T]]
  def update(key: String, value: T): Task[Unit]
}

object CounterRepository {
  def make(duration: Duration) = for {
    m <- ConcurrentMap.empty[String, Aggregator[Int]]
  } yield new CounterRepository(m, duration)
}

class CounterRepository(
    storage: ConcurrentMap[String, Aggregator[Int]],
    duration: Duration
) extends Repository[Int] {
  override def getAll() =
    storage.toList
      .flatMap(l =>
        ZIO
          .foreach(l) { case (str, counter) =>
            counter.get.map(str -> _)
          }
          .map(_.toMap)
      )

  override def update(key: String, count: Int) = for {
    c1 <- storage.get(key)
    _ <- c1 match {
      case Some(value) => value.commit(count)
      case None =>
        for {
          c2 <- SlidingTimeWindowAggregator.makeCounter(duration)
          _  <- c2.commit(count)
          _  <- storage.put(key, c2)
        } yield ()
    }
  } yield ()

  override def duration() = duration
}
