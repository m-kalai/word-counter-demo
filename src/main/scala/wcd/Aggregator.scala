package wcd

import zio._

trait Aggregator[T] {

  /** commit an event
    * @param value
    *   event value
    * @return
    */
  def commit(value: T): Task[Unit]

  /** get current aggregate
    * @return
    *   aggregate value
    */
  def get: Task[T]
}
