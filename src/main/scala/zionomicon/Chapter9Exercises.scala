package zionomicon

import zio._
import scala.collection.immutable
import scala.collection.immutable.Queue.EmptyQueue


//1 Write a simple `Counter` with the following interface that can be incremented and decremented concurrently:

trait Counter {
  def increment: UIO[Long]
  def decrement: UIO[Long]
  def get: UIO[Long]
  def reset: UIO[Unit]
}

final case class CounterImpl(ref: Ref[Long]) {
  def increment: UIO[Long] = ref.getAndUpdate(_+1)
  def decrement: UIO[Long] = ref.getAndUpdate(_-1)
  def get: UIO[Long] = ref.get
  def reset: UIO[Unit] = ref.set(0L)
}


//2 Implement a bounded queue using `Ref` that has a maximum capacity that supports the following interface:
trait BoundedQueue[A] {
   def enqueue(a: A): UIO[Boolean] // Returns false if queue is full
   def dequeue: UIO[Option[A]]     // Returns None if queue is empty
   def size: UIO[Int]
   def capacity: UIO[Int]
 }

final case class BoundedQueueImpl[A](
   enqueue: A => UIO[Boolean], // Returns false if queue is full
   dequeue: UIO[Option[A]],     // Returns None if queue is empty
   size: UIO[Int],
   capacity: UIO[Int],
)

object BoundedQueueImpl {
  def make[A](maxSpot: Int): UIO[BoundedQueueImpl[A]] = 
    for {
      queueRef <- Ref.make(immutable.Queue.empty[A])
    } yield BoundedQueueImpl[A](
      enqueue = (a: A) => queueRef.modify{ queue =>
        if (queue.size >= maxSpot) {
          (false, queue)
        } else {
          (true, queue.appended(a))
        }
        }, 
      dequeue = queueRef.modify{ queue => 
        queue.dequeueOption match {
          case Some((item, newQueue)) => (Some(item), newQueue)
          case None => (None, queue)
        }
      }, 
      size = queueRef.get.map(_.size), 
      capacity = ZIO.succeed(maxSpot)
    )
}

//3 Write a `CounterManager` service that manages multiple counters with the following interface:
trait CounterManager {
  type CounterId = String
  def increment(id: CounterId): UIO[Int]
  def decrement(id: CounterId): UIO[Int]
  def get(id: CounterId): UIO[Int]
  def reset(id: CounterId): UIO[Unit]
  def remove(id: CounterId): UIO[Unit]
  }

object CounterManagerImpl {
  type CounterId = String

  final case class CounterManagerImpl(ref: Ref[Map[CounterId, Int]]) {
    def increment(id: CounterId): UIO[Int] = ref.modify{ counter => 
      val newVal = counter.getOrElse(id, 0) +1
      (newVal, counter.updated(id, newVal))
    }

    def decrement(id: CounterId): UIO[Int] = ref.modify{ counter => 
      val newVal = counter.getOrElse(id, 0) -1
      (newVal, counter.updated(id, newVal))
    }

    def get(id: CounterId): UIO[Int] = ref.get.map(_.getOrElse(id, 0))
  
    def reset(id: CounterId): UIO[Unit] = ref.update(_.updated(id, 0))

    def remove(id: CounterId): UIO[Unit] = ref.update(_ - id)
  }
}
