import zio._

object GroceryStore extends ZIOAppDefault {
  val goShopping = ZIO.attempt(println("Going to the grocery store"))
  val run = goShopping
  // .delay(5.seconds)

}
