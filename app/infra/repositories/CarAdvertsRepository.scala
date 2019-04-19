package infra.repositories

import java.util.UUID

import cats.data.OptionT
import cats.effect.IO
import javax.inject.{Inject, Singleton}
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}

import scala.collection.mutable
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext


trait CarAdvertsRepository {
  def list(): IO[Iterable[CarAdvert]]
  def get(id: String): IO[Option[CarAdvert]]
  def create(carAdvert: CarAdvert): IO[CarAdvert]
//  def delete(id: String): IO[Boolean]
//  def update(carAdvert: CarAdvert): IO[CarAdvert]
}


@Singleton
class InMemoryCarAdvertsRepository @Inject()()(implicit ex: ExecutionContext) extends CarAdvertsRepository {

  var storage: Map[String, CarAdvert] = new mutable.HashMap[String, CarAdvert]()

  override def list(): IO[Iterable[CarAdvert]] = IO { storage.values }

  override def get(id: String): IO[Option[CarAdvert]] = IO { storage.get(id)}

  override def create(carAdvert: CarAdvert): IO[CarAdvert] = IO {
    val guid = UUID.randomUUID().toString
    val car: CarAdvert = carAdvert match {
      case NewCarAdvert(_, title, fuel, price) => NewCarAdvert(guid, title, fuel, price)
      case UsedCarAdvert(_, title, fuel, price, mileage, firstRegisteration) => UsedCarAdvert(guid ,title, fuel, price, mileage, firstRegisteration)
    }
    storage.update(guid, car)
    car
  }
//
//  override def delete(id: String): Future[Boolean] = ???
//
//  override def update(carAdvert: CarAdvert): Future[CarAdvert] = ???
}

