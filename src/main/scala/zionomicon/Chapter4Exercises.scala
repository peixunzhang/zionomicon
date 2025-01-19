package zionomicon

import zio._
import zio.Exit.Success
import zio.Exit.Failure

object Chapter4Exercises {
  object Exercise1 {
    def failWithMessage(string: String): ZIO[Any, Throwable, Nothing] = 
      ZIO.succeed(throw new Error(string))
  }

  object Exercise2 {
    def recoverFromSomeDefects[R, E, A](zio: ZIO[R, E, A])(f: Throwable => Option[A]): ZIO[R, E, A] = 
      zio.foldCauseZIO(
        cause => cause.defects.collectFirst(Function.unlift(f)).fold[ZIO[R, E, A]] (ZIO.failCause(cause))(a => ZIO.succeed(a)),
        a => ZIO.succeed(a)
      )
  }

  object Exercise3 {
    def logFailures[R, E, A](zio: ZIO[R, E, A]): ZIO[R, E, A] = zio.foldCauseZIO(
      cause => ZIO.succeed(cause.prettyPrint) *> ZIO.failCause(cause), 
      s => ZIO.succeed(s)
    )
  }

  object Exercise4 {
    def onAnyFailure[R, E, A](
      zio: ZIO[R, E, A],
      handler: ZIO[R, E, Any]
    ): ZIO[R, E, A] =
      zio.exit.flatMap{
        case Success(value) => ZIO.succeed(value)
        case Failure(cause) => handler *> ZIO.failCause(cause)
      }
  }

  object Exercise5 {
    def ioException[R, A](
      zio: ZIO[R, Throwable, A]
    ): ZIO[R, java.io.IOException, A] =
      zio.refineToOrDie[java.io.IOException]
  }

  object Exercise6 {
    val parseNumber: ZIO[Any, Throwable, Int] =
      ZIO.attempt("foo".toInt).refineToOrDie[NumberFormatException]
  }

  object Exercise7 {
    def left[R, E, A, B](
      zio: ZIO[R, E, Either[A, B]]
    ): ZIO[R, Either[E, B], A] =
      zio.foldZIO(
        e => ZIO.fail(Left(e)),
        success => success match {
          case Left(value) => ZIO.succeed(value)
          case Right(value) => ZIO.fail(Right(value))
        }
      )

    def unleft[R, E, A, B](
      zio: ZIO[R, Either[E, B], A]
    ): ZIO[R, E, Either[A, B]] =
      zio.foldZIO(
        e => e match {
          case Left(value) => ZIO.fail(value)
          case Right(value) => ZIO.succeed(Right(value))
        },
        s => ZIO.succeed(Left(s))
      )
  }

  object Exercise8 {
    def right[R, E, A, B](
      zio: ZIO[R, E, Either[A, B]]
    ): ZIO[R, Either[E, A], B] =
      zio.foldZIO(e =>
        ZIO.fail(Left(e)),
        _.fold(a => ZIO.fail(Right(a)), b => ZIO.succeed(b))  
      )

    def unright[R, E, A, B](
      zio: ZIO[R, Either[E, A], B]
    ): ZIO[R, E, Either[A, B]] = zio.foldZIO(
      _.fold(e => ZIO.fail(e), s => ZIO.succeed(Left(s))),
      b => ZIO.succeed(Right(b))
    )
  }

  object Exercise9 {
    def catchAllCause[R, E1, E2, A](
      zio: ZIO[R, E1, A],
      handler: Cause[E1] => ZIO[R, E2, A]
    ): ZIO[R, E2, A] =
      zio.sandbox.foldZIO(f => handler(f), s => ZIO.succeed(s))
  }

  /**
   * Using the `ZIO#foldCauseZIO` method, implement the following function.
   */
  object Exercise10 {
    def catchAllCause[R, E1, E2, A](
      zio: ZIO[R, E1, A],
      handler: Cause[E1] => ZIO[R, E2, A]
    ): ZIO[R, E2, A] =
      zio.foldCauseZIO(f => handler(f), s => ZIO.succeed(s))
  }
}
