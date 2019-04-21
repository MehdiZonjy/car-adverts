package services

import java.time.LocalDate

import cats.data.OptionT
import cats.effect.IO
import infra.repositories.CarAdvertsRepository
import javax.inject.{Inject, Singleton}
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}

import scala.language.implicitConversions



case class CreateUsedCarAdvert(title: String, fuel: Fuel, price: Int, mileage: Int, firstRegistration: LocalDate)
case class CreateNewCarAdvert(title: String, fuel: Fuel, price: Int)
case class UpdateCarAdvert(title: Option[String], fuel: Option[Fuel], price: Option[Int], mileage: Option[Int], firstRegistration: Option[LocalDate])
case class QueryCarAdverts(orderBy: CarAdvertOrderBy)


trait CarAdvertOrderBy {
  def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean
}
object CarAdvertOrderBy {
  object Id extends CarAdvertOrderBy{
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1,a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.id.compareToIgnoreCase(a2.id) <= 0
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) => a1.id.compareToIgnoreCase(a2.id) <= 0
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => a1.id.compareToIgnoreCase(a2.id) <= 0
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => a1.id.compareToIgnoreCase(a2.id) <= 0
    }
  }

  object Title extends CarAdvertOrderBy {
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1, a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.title.compareToIgnoreCase( a2.title)<=0
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) => a1.title.compareToIgnoreCase( a2.title)<=0
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => a1.title.compareToIgnoreCase( a2.title)<=0
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => a1.title.compareToIgnoreCase( a2.title)<=0
    }
  }
  object Fuel extends CarAdvertOrderBy {
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1, a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.fuel.value.compareToIgnoreCase(a2.fuel.value) <=0
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) =>a1.fuel.value.compareToIgnoreCase(a2.fuel.value) <=0
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => a1.fuel.value.compareToIgnoreCase(a2.fuel.value) <=0
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => a1.fuel.value.compareToIgnoreCase(a2.fuel.value) <=0
    }
  }
  object Price extends CarAdvertOrderBy {
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1, a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.price < a2.price
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) => a1.price < a2.price
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => a1.price < a2.price
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => a1.price < a2.price
    }
  }
  object Mileage extends CarAdvertOrderBy {
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1, a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.id < a2.id
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) => a1.mileage < a2.mileage
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => true
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => false
    }
  }
  object FirstRegistration extends CarAdvertOrderBy {
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1, a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.id < a2.id
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) => a1.firstRegistration.compareTo(a2.firstRegistration) <=0
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => true
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => false
    }
  }

  object New extends CarAdvertOrderBy {
    override def lessThan(a1: CarAdvert, a2: CarAdvert): Boolean = (a1, a2) match {
      case (a1: NewCarAdvert, a2: NewCarAdvert) => a1.id < a2.id
      case (a1: UsedCarAdvert, a2: UsedCarAdvert) => a1.id < a2.id
      case (a1: NewCarAdvert, a2: UsedCarAdvert) => true
      case (a1: UsedCarAdvert, a2: NewCarAdvert) => false
    }
  }

  def fromString (str: String) : Option[CarAdvertOrderBy] = str.toLowerCase match {
    case "id" => Some(Id)
    case "title" => Some(Title)
    case "fuel" => Some(Fuel)
    case "price" => Some(Price)
    case "mileage" => Some(Mileage)
    case "firstregistration" => Some(FirstRegistration)
    case "new" => Some(New)
    case _ => None
  }
}


@Singleton
class CarAdvertsService @Inject()(carAdvertsRepository: CarAdvertsRepository){

  private implicit def cmdToCarAdvert(createUsedCar: CreateUsedCarAdvert): CarAdvert = UsedCarAdvert("", createUsedCar.title, createUsedCar.fuel, createUsedCar.price, createUsedCar.mileage, createUsedCar.firstRegistration)
  private implicit def cmdToCarAdvert(createNewCar: CreateNewCarAdvert): CarAdvert = NewCarAdvert("", createNewCar.title, createNewCar.fuel, createNewCar.price)

  def get(id: String): IO[Option[CarAdvert]] = carAdvertsRepository.get(id)


  def list(query: QueryCarAdverts): IO[List[CarAdvert]] = carAdvertsRepository.list.map(_.sortWith(query.orderBy.lessThan))


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
                                                                                updateCmd.firstRegistration.getOrElse(firstRegisteration))

  }

  def update(id: String, cmd: UpdateCarAdvert): IO[Option[CarAdvert]] = {
   val updatedAdvert: OptionT[IO, CarAdvert] = for {
      advert <- OptionT(get(id))
      updatedAdvert <- OptionT(carAdvertsRepository.update(applyUpdate(advert, cmd)))

    } yield updatedAdvert

    updatedAdvert.value
  }


  def delete(id: String): IO[Boolean] = carAdvertsRepository.delete(id)

}
