package coop.rchain.shared

import cats._, cats.data._, cats.implicits._
import coop.rchain.catscontrib._, Catscontrib._

trait Time[F[_]] {
  def currentMillis: F[Long]
  def nanoTime: F[Long]
  def sleep(millis: Int): F[Unit]
}

object Time extends TimeInstances {
  def apply[F[_]](implicit L: Time[F]): Time[F] = L

  def forTrans[F[_]: Monad, T[_[_], _]: MonadTrans](implicit TM: Time[F]): Time[T[F, ?]] =
    new Time[T[F, ?]] {
      def currentMillis: T[F, Long]      = TM.currentMillis.liftM[T]
      def nanoTime: T[F, Long]           = TM.nanoTime.liftM[T]
      def sleep(millis: Int): T[F, Unit] = TM.sleep(millis).liftM[T]
    }
}

sealed abstract class TimeInstances {
  implicit def eitherTTime[E, F[_]: Monad: Time[?[_]]]: Time[EitherT[F, E, ?]] =
    Time.forTrans[F, EitherT[?[_], E, ?]]

  implicit def stateTTime[S, F[_]: Monad: Time[?[_]]]: Time[StateT[F, S, ?]] =
    new Time[StateT[F, S, ?]] {
      override def currentMillis: StateT[F, S, Long]      = StateT.liftF(Time[F].currentMillis)
      override def nanoTime: StateT[F, S, Long]           = StateT.liftF(Time[F].nanoTime)
      override def sleep(millis: Int): StateT[F, S, Unit] = StateT.liftF(Time[F].sleep(millis))
    }
}
