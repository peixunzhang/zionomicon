package zionomicon

import zio._
import java.io._
import zionomicon.Chapter2Exercises.Exercise8.foreach
import scala.collection.immutable

object Chapter2Exercises {
  object Exercise1 {
    def readFile(file: String): String = {
      val source = scala.io.Source.fromFile(file)
      try source.getLines().mkString finally source.close()
    }

    def readFileZio(file: String): ZIO[Any, Throwable, String] = {
      ZIO.attempt(readFile(file))
    }
  }
 
  object Exercise2 {
    def writeFile(file: String, text: String): Unit = {
      val pw = new PrintWriter(new File(file))
      try pw.write(text)
      finally pw.close
    }

    def writeFileZio(file: String, text: String) = ZIO.attempt(writeFile(file, text))
  }

  object Exercise3 {
    import Exercise1._
    import Exercise2._

    def copyFile(source: String, dest: String): Unit = {
      val contents = readFile(source)
      writeFile(dest, contents)
    }

    def copyFileZio(source: String, dest: String) = readFileZio(source).flatMap(contents => writeFileZio(dest, contents))
  }

  object Exercise4 {
    def printLine(line: String) = ZIO.attempt(println(line))
    val readLine                = ZIO.attempt(scala.io.StdIn.readLine())

    for {
      _ <- printLine("What's your name?")
      name <- readLine
      _ <- printLine(s"Hello, ${name}!")
    } yield ()
  }

  object Exercise5 {
    val random                  = ZIO.attempt(scala.util.Random.nextInt(3) + 1)
    def printLine(line: String) = ZIO.attempt(println(line))
    val readLine                = ZIO.attempt(scala.io.StdIn.readLine())

    for {
      int <- random
      _ <- printLine("Guess a number from 1 to 3:")
      num <- readLine
      _ <- if (num == int.toString) printLine("You guessed right!")
            else printLine(s"You guessed wrong, the number was $int!")
    } yield ()
  }

  object Exercise6 {
    final case class ZIO[-R, +E, +A](run: R => Either[E, A])

    def zipWith[R, E, A, B, C](
      self: ZIO[R, E, A],
      that: ZIO[R, E, B]
    )(f: (A, B) => C): ZIO[R, E, C] =
      ZIO(r => self.run(r).flatMap(a => that.run(r).map(b => f(a, b))))
  }

  object Exercise7 {
    import Exercise6._

    def succeed[A](a: => A): ZIO[Any, Nothing, A] =
      ZIO(_ => Right(a))

    def collectAll[R, E, A](
      in: Iterable[ZIO[R, E, A]]
    ): ZIO[R, E, List[A]] = 
      if (in.isEmpty) succeed(List.empty)
      else zipWith(in.head, collectAll(in.tail))(_ :: _)
  }

  object Exercise8 { //??
    import Exercise6._
  import Exercise7._

  def foreach[R, E, A, B](
    in: Iterable[A]
  )(f: A => ZIO[R, E, B]): ZIO[R, E, List[B]] =
    collectAll(in.map(f))
  }

  object Exercise9 {
    final case class ZIO[-R, +E, +A](run: R => Either[E, A])

  def orElse[R, E1, E2, A](
    self: ZIO[R, E1, A],
    that: ZIO[R, E2, A]
  ): ZIO[R, E2, A] = ZIO {
      r => self.run(r) match {
        case Left(value) => that.run(r)
        case Right(value) => Right(value)
      }
    }
  }
  
  object Exercise10 {
    import Exercise1._
    import Exercise5._

    object App extends ZIOAppDefault {
      def read(commandLineArgumnets: Chunk[String]) = 
        ZIO.foreach(commandLineArgumnets)(readFileZio(_).flatMap((printLine)))

      val run =
        for {
          args <- ZIOAppArgs.getArgs
          _ <- read(args)
        } yield ()
    }

  }

  object Exercise11 {
    def eitherToZIO[E, A](either: Either[E, A]): ZIO[Any, E, A] = either match {
      case Left(e) => ZIO.fail(e)
      case Right(a) => ZIO.succeed(a)
    } 
  }

  object Exercise12 {
    def listToZIO[A](list: List[A]): ZIO[Any, None.type, A] = list match {
      case head :: _ => ZIO.succeed(head)
      case immutable.Nil => ZIO.fail(None)
    }
  }

  object Exercise13 {
    def currentTime(): Long = java.lang.System.currentTimeMillis()

    lazy val currentTimeZio: ZIO[Any, Nothing, Long] = ZIO.succeed(currentTime())
  }

  object Exercise14 {
    def getCacheValue(
      key: String,
      onSuccess: String => Unit,
      onFailure: Throwable => Unit
    ): Unit =
      ???

    def getCacheValueZio(key: String): ZIO[Any, Throwable, String] = 
      ZIO.async{cb => getCacheValue(key, s => cb(ZIO.succeed(s)), f => cb(ZIO.fail(f)))}
  }

  object Exercise15 {
     trait User

    def saveUserRecord(
      user: User,
      onSuccess: () => Unit,
      onFailure: Throwable => Unit
    ): Unit =
      ???

    def saveUserRecordZio(user: User): ZIO[Any, Throwable, Unit] = ZIO.async{cb => saveUserRecord(user, () => cb(ZIO.succeed(())), thr => cb(ZIO.fail(thr)))}
  }

  object Exercise16 {
    import scala.concurrent.{ExecutionContext, Future}
    trait Query
    trait Result

    def doQuery(query: Query)(implicit ec: ExecutionContext): Future[Result] =
      ???

    def doQueryZio(query: Query): ZIO[Any, Throwable, Result] = ZIO.fromFuture(ec => doQuery(query)(ec))
  }

  object Exercise17 {
    object HelloHuman extends ZIOAppDefault {
      val run = 
        for {
          _ <- zio.Console.printLine("What is your name?")
          name <- zio.Console.readLine
          _  <- zio.Console.printLine(s"Hello $name")
        } yield ()
    }
  }

object Exercise18 {
  object NumberGuessing extends ZIOAppDefault {
    val run = 
      for {
        random <- Random.nextIntBounded(2).map(_ +1)
        _ <- zio.Console.printLine("Geuss a number from 1 to 3")
        num <- zio.Console.readLine
        _ <- if (num == random.toString) zio.Console.printLine("You guessed right!")
               else zio.Console.printLine(s"You guessed wrong, the number was $random!")
      } yield ()
  }
}

object Exercise19 {
  import java.io.IOException

    def readUntil(
      acceptInput: String => Boolean
    ): ZIO[zio.Console, IOException, String] =
      zio.Console.readLine.flatMap{input => 
        if (acceptInput(input)) ZIO.succeed(input)
        else readUntil(acceptInput)
      }
}

object Exercise20 {
  def doWhile[R, E, A](
      body: ZIO[R, E, A]
    )(condition: A => Boolean): ZIO[R, E, A] =
    body.flatMap{ f =>
      if (condition(f)) ZIO.succeed(f)
      else doWhile(body)(condition)

    }
  }
}
