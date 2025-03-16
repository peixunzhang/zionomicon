package zionomicon
import doobie._
import doobie.implicits._
import zio._
import zio.interop.catz._

object Chapter4ExerciseNewbook {
  val query: ConnectionIO[Int] = sql"""CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    name TEXT NOT NULL,
    age INT NOT NILL)""".query[Int].unique

    final case class User(id: Int, name: String, age: Int) {}

    val transactor: Transactor[Task] = Transactor.fromDriverManager[Task](
      "org.postgresql.Driver", // JDBC driver
            "jdbc:postgresql://localhost:5432/mydb", // Database URL
            "user", // Database username
            "password" // Database password
    )
    val effect: Task[Int] = query.transact(transactor)

}
