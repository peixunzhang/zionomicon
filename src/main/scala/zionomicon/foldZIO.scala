package zionomicon

final case class ZIO[-R, +E, +A](run: R => Either[E, A]) {self =>
  def foldZIO[R1 <: R, E1, B](
    failure: E => ZIO[R1, E1, B],
    success: A => ZIO[R1, E1, B]
  ): ZIO[R1, E1, B] = ZIO(r => self.run(r).fold(failure, success).run(r))
}
