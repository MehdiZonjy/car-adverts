package services

import java.time.LocalDate

import cats.data.OptionT
import cats.effect.IO
import infra.repositories.CarAdvertsRepository
import javax.inject.{Inject, Singleton}
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}

import scala.language.implicitConversions



case class CreateUsedCarAdvert(title: String, fuel: Fuel, price: Int,  mileage: Int, firstRegisteration: LocalDate)
case class CreateNewCarAdvert(title: String, fuel: Fuel, price: Int)
case class UpdateCarAdvert(title: Option[String], fuel: Option[Fuel], price: Option[Int], mileage: Option[Int], firstRegisteration: Option[LocalDate])

trait CarAdvertError
case class CarAlreadyExists(id: String) extends CarAdvertError
case class CarNotFound(id: String) extends CarAdvertError

@Singleton
class CarAdvertsService @Inject()(carAdvertsRepository: CarAdvertsRepository){

  implicit def cmdToCarAdvert(createUsedCar: CreateUsedCarAdvert): CarAdvert = UsedCarAdvert("", createUsedCar.title, createUsedCar.fuel, createUsedCar.price, createUsedCar.mileage, createUsedCar.firstRegisteration)
  implicit def cmdToCarAdvert(createNewCar: CreateNewCarAdvert): CarAdvert = NewCarAdvert("", createNewCar.title, createNewCar.fuel, createNewCar.price)

  def get(id: String): IO[Option[CarAdvert]] = carAdvertsRepository.get(id)


  def list(): IO[Iterable[CarAdvert]] = carAdvertsRepository.list


  def create(cmd: CreateNewCarAdvert): IO[Option[CarAdvert]] = carAdvertsRepository.create(cmd)
  def create(cmd: CreateUsedCarAdvert): IO[Option[CarAdvert]] = carAdvertsRepository.create(cmd)


  private def applyUpdate(advert: CarAdvert, updateCmd: UpdateCarAdvert) = advert match {
    case NewCarAdvert(id, title, fuel, price) => NewCarAdvert(id,
                                                              updateCmd.title.getOrElse(title),
                                                              updateCmd.fuel.getOrElse(fuel),
                                                              updateCmd.price.getOrElse(price))
    case UsedCarAdvert(id, title, fuel, price, mileage, firstRegisteration) => UsedCarAdvert(id,
                                                                                updateCmd.title.getOrElse(title),
                                                                                updateCmd.fuel.getOrElse(fuel),
                                                                                updateCmd.price.getOrElse(price),
                                                                                updateCmd.mileage.getOrElse(mileage),
                                                                                updateCmd.firstRegisteration.getOrElse(firstRegisteration))

  }

  def update(id: String, cmd: UpdateCarAdvert): IO[Option[CarAdvert]] = {
   val updatedAdvert: OptionT[IO, CarAdvert] = for {
      advert <- OptionT(get(id))
      updatedAdvert <- OptionT(carAdvertsRepository.update(applyUpdate(advert, cmd)))

    } yield updatedAdvert

    updatedAdvert.value
  }

}
