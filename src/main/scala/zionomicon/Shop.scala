package zionomicon

import zio._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

object GroceryStore extends ZIOAppDefault {
  val goShopping = ZIO.attempt(println("Going to the grocery store"))
  val run = goShopping
  // .delay(5.seconds)

  def fromFuture[A](make: ExecutionContext => Future[A]): Task[A] = ???

  def goShoppingFuture(implicit ec: ExecutionContext): Future[Unit] = Future(println("Going to the shop"))

  // val goShoppingTask: Task[Unit] = Task.fromFuture(implicit ec => goShoppingFuture)
}
