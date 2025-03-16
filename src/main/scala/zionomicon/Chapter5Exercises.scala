package zionomicon

import zio._
import zionomicon.Chapter2Exercises.Exercise4.printLine

object Chapter5Exercises extends  ZIOAppDefault {
  lazy val hello = ZIO.debug("Hello")
  lazy val world = ZIO.debug("world")
  lazy val done = ZIO.debug("done")
  val longRun = ZIO.debug("something").repeat(Schedule.spaced(1.second))
  lazy val mightFail = ZIO.debug("do something might fail").delay(2.seconds) *> ZIO.fail("fail")
  val willNotStop = ZIO.debug("running").repeat(Schedule.spaced(1.second))
  val child1 = printLine("child 1 starts").orDie *> ZIO.sleep(2.seconds) *> printLine("Child 1 fiber")
  val child2 = printLine("child 2 starts").orDie *> ZIO.sleep(2.seconds) *> printLine("Child 2 fiber")
  val parent: ZIO[Any, Nothing, Unit] = printLine("Parten starts").orDie *> child1.fork *> ZIO.sleep(2.seconds) *> child2.fork *> ZIO.sleep(2.seconds) *> ZIO.debug("Parent 1 fiber")
  val parentWithDaemonFiber: ZIO[Any, Nothing, Unit] = printLine("Parten daemon fiber starts").orDie *> child2.forkDaemon *> ZIO.sleep(2.seconds)*> child1.forkDaemon *> ZIO.sleep(2.seconds) *> ZIO.debug("Parent 2 fiber")

  val program1 = for {
    _ <- hello.fork
    _ <- ZIO.sleep(2.seconds)
    _ <- world.fork
    _ <- ZIO.sleep(1.second)
  } yield ()

  val program2 =  for {
    fiber <- hello.fork
    _ <- ZIO.sleep(2.seconds)
    fiber2 <- world.fork
    _ <- ZIO.sleep(2.second)
    _ <- fiber.join
    _ <- fiber2.join
    _ <- done
  } yield ()

  val program3 =  for {
    fiber <- longRun.forkDaemon
    _ <- ZIO.sleep(5.seconds)
    _ <- fiber.interrupt
  } yield ()

  val program4 = for {
    fiber <- mightFail.fork
    exit <- fiber.await
    _ <- exit.foldZIO(
      e => ZIO.debug("fiber failed " + e),
      s => ZIO.debug("fiber worked " + s)
    )
    _ <- ZIO.debug("fiber joined")
  } yield ()

  val program5 = for{
    fiber <- willNotStop.forkDaemon
    _ <- ZIO.sleep(3.seconds)
    _ <- fiber.interrupt
  } yield ()

  val program6 = for {
    fiber <- parent.fork
    _ <- ZIO.sleep(1.second)
    _ <- fiber.interrupt
    _ <- ZIO.sleep(1.second)
  } yield ()

  def program7 = for {
    fiber <- parentWithDaemonFiber.forkDaemon
    _ <- ZIO.sleep(5.second)
    _ <- fiber.interrupt
    _ <- ZIO.sleep(10.second)
  } yield ()

  override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] = program7
}
