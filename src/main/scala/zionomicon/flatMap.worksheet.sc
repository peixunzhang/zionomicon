// package zionomicon
import scala.io.StdIn
// import zio._

trait ZIO[R, E, A] {
  def flatMap[B](andThen: A => ZIO[R, E, B]): ZIO[R, E, B] = ???
}

object ZIO {
  def attempt[A](effect: => A): ZIO[Any, Throwable, A] = ???
  val readLine = ZIO.attempt(StdIn.readLine())
  def printlnLine(line: String) = ZIO.attempt(println(line))
  val echo = readLine.flatMap(line => printlnLine(line))
}

// val helloWorld = ZIO.attempt(print("Hello, ")) *> ZIO.attempt(print("World!\n")) 
// print(helloWorld) 

val foo = 1
foo + 1
