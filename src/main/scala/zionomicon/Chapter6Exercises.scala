package zionomicon

import zio._
import java.net.URL

object Chapter6Exercises {

  def collectAllPar[R, E, A] (in: Iterable[ZIO[R, E, A]]): ZIO[R, E, List[A]] = 
    ZIO.foreachPar(in.toList)(identity)

  def collectAllParResults[R, E, A](in: Iterable[ZIO[R, E, A]]): ZIO[R, Nothing, (List[A], List[E])] =
    ZIO.partitionPar(in)(identity).map{
      case (e, s) => (s.toList, e.toList)
    }

  def fetchUrl(url: URL): ZIO[Any, Throwable, String] = ZIO.attemptBlocking(url.toString)

  def fetchAllUrlsPar(urls: List[URL]): ZIO[Any, Nothing, (List[(URL, Throwable)], List[(URL, String)])] = 
    ZIO.partitionPar(urls)(fetchUrl(_)).map {
      case (es, ss) => (urls.zip(es.toList), urls.zip(ss.toList))
    }

}
