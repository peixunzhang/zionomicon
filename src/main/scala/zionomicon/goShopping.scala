import zio._

object GroceryStore extends ZIOAppDefault {

  val run = Shop.goShopping
}

object Shop {
  val goShopping =
  ZIO.attempt(println("Going to the grocery store"))
}
