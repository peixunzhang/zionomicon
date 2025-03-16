package zionomicon

import zio._

object Fiber {
  trait ZIO[-R, +E, +A] {
    def fork: URIO[R, Fiber[E, A]]
  }

  trait Fiber[+E, +A] {
    def join: IO[E, A]
  }

  lazy val doSomething: UIO[Unit] = ???
  lazy val doSomethingElse: UIO[Unit] = ???

  lazy val ex1 = for {
    _ <- doSomething
    _ <- doSomethingElse
  } yield ()

  lazy val ex2 = for {
    _ <- doSomething.fork
    _ <- doSomethingElse
  } yield () 
}
  object ForkJoinEx extends ZIOAppDefault {
    lazy val doSomething: UIO[Unit] = ZIO.debug("Do Something").delay(10.seconds)
    lazy val doSomethingElse: UIO[Unit] = ZIO.debug("Do Something else").delay(2.seconds)

    override def run = for {
      _ <- ZIO.debug("Strat")
      fiber <- doSomething.fork
      _ <- doSomethingElse
      _ <- fiber.join
      _ <- ZIO.debug("The joined")
    } yield ()
  }
 
// error: level=ERROR thread=#zio-fiber-433440766 message="" cause="Exception in thread "zio-fiber-1320029582" java.lang.String: fail
  object ForkJoinFailedFiberEx extends ZIOAppDefault {
    lazy val doSomething: ZIO[Any, String, Nothing] = ZIO.debug("do something").delay(2.seconds) *> ZIO.fail("fail")

    override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] = for {
      _ <- ZIO.debug("start")
      fiber <- doSomething.fork
      _ <- fiber.join
      _ <- ZIO.debug("fiber joined")
    } yield ()
  }

  // do something
// fiber failed: fail
// fiber joined
  object ForkAwaitFailedFiberEx extends ZIOAppDefault {
    lazy val doSomething: ZIO[Any, String, Nothing] = ZIO.debug("do something").delay(2.seconds) *> ZIO.fail("fail")

    override def run: ZIO[Environment with ZIOAppArgs with Scope,Any,Any] = 
      for {
        fiber <- doSomething.fork
        exit <- fiber.await
        _ <- exit.foldZIO(
          e => ZIO.debug("fiber failed: " + e),
          s => ZIO.debug("fiber complated: " + s)
        )
        _ <- ZIO.debug("fiber joined")
      } yield ()
  }


  object InterruptFiberExample extends ZIOAppDefault {
    lazy val doSomething: ZIO[Any, Nothing, Long] = ZIO.debug("soem long running").repeat(Schedule.spaced(2.seconds))

    override def run = for {
      _ <- ZIO.debug("strat")
      fiber <- doSomething.fork
      _ <- ZIO.sleep(5.seconds)
      _ <- fiber.interrupt
      _ <- ZIO.debug("fiber interrupted")
    } yield ()
  }
