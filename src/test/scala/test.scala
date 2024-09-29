import zio.test._
import zio._
import zio.test.Assertion.equalTo

object ExampleSpec extends ZIOSpecDefault {
  def spec = suite("ExampleSpec")(
    test("greet says hello to the user") {
        val greet = 
          for {
            name <- Console.readLine.orDie
            _ <- Console.printLine(s"Hello, $name")
          } yield () 
      for {
        _ <- TestConsole.feedLines("Sam")
        _ <- greet
        value <- TestConsole.output
      } yield assert(value)(equalTo(Vector("Hello, Sam")))
    }
  )
}
