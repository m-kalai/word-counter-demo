package wcd

import wcd.SlidingTimeWindowAggregator.Event
import zio.{Clock, Duration, Task}
import zio.stm.{TQueue, TRef, ZSTM}

import java.util.concurrent.TimeUnit

object SlidingTimeWindowAggregator {
  case class Event[T](value: T, timestamp: Long)

  def makeCounter(duration: Duration): Task[SlidingTimeWindowAggregator[Int]] =
    (for {
      queue   <- TQueue.unbounded[Event[Int]]
      counter <- TRef.make(0)
    } yield new SlidingTimeWindowAggregator(
      queue,
      counter,
      duration,
      (a: Int, b: Int) => a + b,
      (a: Int, b: Int) => a - b
    )).commit
}

// works only for reversible aggregation functions
class SlidingTimeWindowAggregator[T](
    queue: TQueue[Event[T]],
    current: TRef[T],
    windowSize: Duration,
    add: (T, T) => T,
    expire: (T, T) => T
) extends Aggregator[T] {

  override def commit(value: T): Task[Unit] = for {
    now <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _   <- updateQueue(value, now)
  } yield ()

  override def get: Task[T] = for {
    now   <- Clock.currentTime(TimeUnit.MILLISECONDS)
    count <- (expireItems(now) *> current.get).commit
  } yield count

  private def updateQueue(value: T, now: Long) = (for {
    _ <- queue.offer(Event(value, now))
    _ <- current.update(add(value, _))
    _ <- expireItems(now)
  } yield ()).commit

  private def expireItems(now: Long): ZSTM[Any, Nothing, Unit] = for {
    item <- queue.peekOption
    _ <-
      if (item.exists(_.timestamp < (now - windowSize.toMillis)))
        queue.take.flatMap(e =>
          current.update(expire(_, e.value))
        ) *> expireItems(now)
      else ZSTM.unit
  } yield ()

}
