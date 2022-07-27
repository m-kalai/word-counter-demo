package wcd

import wcd.WindowedCounter.Event
import zio._
import zio.stm.{TQueue, TRef, ZSTM}

import java.util.concurrent.TimeUnit

trait Counter {
  def mark(count: Int): Task[Unit]
  def getCount: Task[Int]
}

object WindowedCounter {
  case class Event(value: Int, timestamp: Long)

  def make(duration: Duration): Task[WindowedCounter] = (for {
    queue   <- TQueue.unbounded[Event]
    counter <- TRef.make(0)
  } yield new WindowedCounter(queue, counter, duration)).commit
}

class WindowedCounter(
    queue: TQueue[Event],
    counter: TRef[Int],
    windowSize: Duration
) extends Counter {

  override def mark(count: Int): Task[Unit] = for {
    now <- Clock.currentTime(TimeUnit.MILLISECONDS)
    _   <- updateQueue(count, now)
  } yield ()

  override def getCount: Task[Int] = for {
    now   <- Clock.currentTime(TimeUnit.MILLISECONDS)
    count <- (expireItems(now) *> counter.get).commit
  } yield count

  private def updateQueue(count: Int, now: Long) = (for {
    _ <- queue.offer(Event(count, now))
    _ <- counter.update(_ + count)
    _ <- expireItems(now)
  } yield ()).commit

  private def expireItems(now: Long): ZSTM[Any, Nothing, Unit] = for {
    item <- queue.peekOption
    _ <-
      if (item.exists(_.timestamp < (now - windowSize.toMillis)))
        queue.take.flatMap(e => counter.update(_ - e.value)) *> expireItems(now)
      else ZSTM.unit
  } yield ()

}
