package wcd

import zio._
import zio.test.{TestClock, ZIOSpecDefault, assertTrue}

object WindowedCounterSpec extends ZIOSpecDefault {
  override def spec = suite("WindowedCounter")(
    test("should add event to the window") {
      for {
        counter <- WindowedCounter.make(5.seconds)
        _       <- counter.mark(3)
        count   <- counter.getCount
      } yield assertTrue(count == 3)
    },
    test("should expire events outside of the window") {
      for {
        counter <- WindowedCounter.make(5.seconds)
        _       <- counter.mark(3)
        _       <- TestClock.adjust(3.seconds)
        count1  <- counter.getCount
        _       <- TestClock.adjust(3.seconds)
        count2  <- counter.getCount
      } yield assertTrue(count1 == 3) && assertTrue(count2 == 0)
    },
    test("should handle multiple expirations") {
      for {
        counter <- WindowedCounter.make(5.seconds)
        _       <- counter.mark(1)
        _       <- counter.mark(4)
        _       <- TestClock.adjust(3.seconds)
        _       <- counter.mark(2)
        count1  <- counter.getCount
        _       <- TestClock.adjust(3.seconds)
        _       <- counter.mark(3)
        count2  <- counter.getCount
      } yield assertTrue(count1 == 7) && assertTrue(count2 == 5)
    }
  )
}
