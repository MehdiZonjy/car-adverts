package models.cardadvert

import java.time.LocalDate

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json.{Json, Writes}


sealed case class Fuel(value: String)

object Fuel {
  object Gasoline extends Fuel("gasoline")
  object Diesel extends Fuel("diesel")

  val values = Seq(Gasoline, Diesel)


  def fromString (str: String) : Option[Fuel] = str.toLowerCase match {
    case "gasoline" => Some(Gasoline)
    case "diesel" => Some(Diesel)
    case _ => None
  }



}

sealed trait CarAdvert
// 1: is there a cleaner way to declare an enum without using Fuel.Fuel ??
// 2: one could debate that both cass classes could be merged into one while using Option for optional values
// , however it would be difficult to express that newcars lack the optional values while usedcards don't.
// I find that using an explicit ADT(CarAdvert) with a sum type helps in expressing the domain model via the type system better
case class NewCarAdvert(id: String, title: String, fuel: Fuel, price: Int) extends  CarAdvert

case class UsedCarAdvert(id: String, title: String, fuel: Fuel, price: Int, mileage: Int, firstRegisteration: LocalDate) extends CarAdvert



object NewCarAdvert {
  implicit val newCarWrites = new Writes[NewCarAdvert] {
    def writes(car: NewCarAdvert) = Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel.value,
      "price" -> car.price,
      "new" -> true
    )
  }
}

object UsedCarAdvert {
  implicit val usedCardWrites = new Writes[UsedCarAdvert] {
    def writes(car: UsedCarAdvert) = Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel.value,
      "price" -> car.price,
      "mileage" -> car.mileage,
      "firstRegisteration" -> car.firstRegisteration.toString,
      "new" -> false
    )
  }
}

object CarAdvert {
  implicit  val carAdvertWrites = new Writes[CarAdvert] {
    def writes(car: CarAdvert) = car match {
      case newCar:NewCarAdvert => NewCarAdvert.newCarWrites.writes(newCar)
      case usedCar: UsedCarAdvert => UsedCarAdvert.usedCardWrites.writes(usedCar)
    }
  }
}