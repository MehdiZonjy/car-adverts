package services

import java.time.LocalDate

import cats.effect.IO
import infra.repositories.CarAdvertsRepository
import javax.inject.{Inject, Singleton}
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}
import scala.language.implicitConversions



  case class CreateUsedCarAdvert(title: String, fuel: Fuel, price: Int,  mileage: Int, firstRegisteration: LocalDate)
case class CreateNewCarAdvert(title: String, fuel: Fuel, price: Int)



trait CarAdvertError
case class CarAlreadyExists(id: String) extends CarAdvertError
case class CarNotFound(id: String) extends CarAdvertError
case class InvalidCarAdvert(errors: Map[String, String]) extends CarAdvertError

@Singleton
class CarAdvertsService @Inject()(carAdvertsRepository: CarAdvertsRepository){


//  carAdvertsRepository.get("ad").runAsync()
//  def get(id: String): EitherT[IO, CarAdvertError, CarAdvert] = for {
//    maybeCarAdvert <- EitherT.liftF(carAdvertsRepository.get(id))
//  } yield maybeCarAdvert match {
//    case None => EitherT.left(CarNotFound)
//    case Some(carAdvert) => EitherT.right(carAdvert)
//  }


  implicit def cmdToCarAdvert(createUsedCar: CreateUsedCarAdvert): CarAdvert = UsedCarAdvert("", createUsedCar.title, createUsedCar.fuel, createUsedCar.price, createUsedCar.mileage, createUsedCar.firstRegisteration)
  implicit def cmdToCarAdvert(createNewCar: CreateNewCarAdvert): CarAdvert = NewCarAdvert("", createNewCar.title, createNewCar.fuel, createNewCar.price)

  def get(id: String): IO[Option[CarAdvert]] = carAdvertsRepository.get(id)


  def list(): IO[Iterable[CarAdvert]] = carAdvertsRepository.list

  def create(cmd: CreateNewCarAdvert): IO[Option[CarAdvert]] = carAdvertsRepository.create(cmd)
  def create(cmd: CreateUsedCarAdvert): IO[Option[CarAdvert]] = carAdvertsRepository.create(cmd)

}
