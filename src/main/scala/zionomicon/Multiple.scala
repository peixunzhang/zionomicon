package zionomicon

import zio._

object Multiple {
  lazy val readInt: ZIO[Any, NumberFormatException, Int] = ???

  lazy val readAndSumTwoInts: ZIO[Any, NumberFormatException, Int] = for {
    x <- readInt
    y <- readInt
  } yield x * y
}
