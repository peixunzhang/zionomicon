package zionomicon

import java.util.concurrent.atomic.AtomicReference
import zio._

trait Ref[A] {

  def modify[B](f: A => (B, A)): UIO[B]

  def get: UIO[A] = modify(a => (a, a))

  def set(a: A): UIO[Unit] = modify(_ => ((), a))

  def update(f: A => A): UIO[Unit] = modify(a => ((), f(a)))
}


object Ref {
  def make[A](a: A): UIO[Ref[A]] = zio.ZIO.succeed{
    new Ref[A] {
      val atomic = new AtomicReference(a)


  def modify[B](f: A => (B, A)): UIO[B] = zio.ZIO.succeed{
    var loop = true
    var b: B = null.asInstanceOf[B]
    while (loop) {
      val current = atomic.get
      val tuple = f(current)
      b = tuple._1
      loop = !atomic.compareAndSet(current, tuple._2)
    }
    b
  }

      }
  }

  def updateAndLog[A](ref: Ref[A])(f: A => A): URIO[Any, Unit] = ref.modify{
    oldVal => 
      val newVal = f(oldVal)
      ((oldVal, newVal), newVal)
  }.flatMap{ case (oldVal, newVal) => zio.Console.printLine(s"update $oldVal to $newVal").orDie
  }
}

trait RefCache[K ,V] {
  def getOrElseCompute(k: K)(f: K => V): UIO[Ref[V]]
}

object RefCache {
  def make[K, V]: UIO[RefCache[K, V]] = 
    zio.Ref.Synchronized.make(Map.empty[K, Ref[V]]).map { ref => 
      new RefCache[K, V] {
        def getOrElseCompute(k: K)(f: K => V): UIO[Ref[V]] = 
          ref.modifyZIO{ map => 
            map.get(k) match {
              case Some(ref) => zio.ZIO.succeed((ref, map)) 
              case None => Ref.make(f(k)).map(ref => (ref, map + (k -> ref)))
        }
        }
    }
    }
}
