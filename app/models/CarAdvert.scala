package models.cardadvert

import java.time.LocalDate

import play.api.libs.json.{Json, Writes}


object Fuel {
  type Fuel = String
  val Gasoline = "gasoline"
  val Diesel = "diesel"
}

sealed trait CarAdvert

object CarAdvert {
  implicit  val carAdvertWrites = new Writes[CarAdvert] {
    def writes(car: CarAdvert) = car match {
      case newCar:NewCar => NewCar.newCarWrites.writes(newCar)
      case usedCar: UsedCar => UsedCar.usedCardWrites.writes(usedCar)
    }
  }
}
// 1: is there a cleaner way to declare an enum without using Fuel.Fuel ??
// 2: one could debate that both cass classes could be merged into one while using Option for optional values
// , however it would be difficult to express that newcars lack the optional values while usedcards don't.
// I find that using an explicit ADT(CarAdvert) with a sum type helps in expressing the domain model via the type system better
case class NewCar(id: String, title: String, fuel: Fuel.Fuel, price: Int) extends  CarAdvert

case class UsedCar(id: String, title: String, fuel: Fuel.Fuel, price: Int, mileage: Int, firstRegisteration: LocalDate) extends CarAdvert



object NewCar {
  implicit val newCarWrites = new Writes[NewCar] {
    def writes(car: NewCar) = Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel,
      "price" -> car.price,
      "new" -> true
    )
  }
}

object UsedCar {
  implicit val usedCardWrites = new Writes[UsedCar] {
    def writes(car: UsedCar) = Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel,
      "price" -> car.price,
      "mileage" -> car.mileage,
      "firstRegisteration" -> car.firstRegisteration.toString,
      "new" -> false
    )
  }
}