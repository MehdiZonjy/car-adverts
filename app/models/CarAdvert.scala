package models.cardadvert

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.data.FormError
import play.api.data.format.Formatter
import play.api.libs.json._
import play.api.libs.json.Reads._
import play.api.libs.functional.syntax._
import utils.Date._

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

  implicit val fuelReads = new Reads[Fuel] {
    override def reads(json: JsValue): JsResult[Fuel] = json.asOpt[String].flatMap(fromString).map( f => JsSuccess(f)).getOrElse(JsError("Invalid Fuel Type"))
  }


}

sealed trait CarAdvert
// 1: is there a cleaner way to declare an enum without using Fuel.Fuel ??
// 2: one could debate that both cass classes could be merged into one while using Option for optional values
// , however it would be difficult to express that newcars lack the optional values while usedcards don't.
// I find that using an explicit ADT(CarAdvert) with a sum type helps in expressing the domain model via the type system better
case class NewCarAdvert(id: String, title: String, fuel: Fuel, price: Int) extends  CarAdvert

case class UsedCarAdvert(id: String, title: String, fuel: Fuel, price: Int, mileage: Int, firstRegistration: LocalDate) extends CarAdvert



object NewCarAdvert {
  implicit val newCarWrites = new Writes[NewCarAdvert] {
    def writes(car: NewCarAdvert): JsObject = Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel.value,
      "price" -> car.price,
      "new" -> true
    )
  }

  implicit val newCarReads = (
    (JsPath \ "id").read[String] and
    (JsPath \ "title").read[String] and
    (JsPath \ "fuel").read[Fuel] and
    (JsPath \ "price").read[Int]
  )(NewCarAdvert.apply _)
}

object UsedCarAdvert {
  implicit val usedCardWrites = new Writes[UsedCarAdvert] {
    def writes(car: UsedCarAdvert): JsObject = Json.obj(
      "id" -> car.id,
      "title" -> car.title,
      "fuel" -> car.fuel.value,
      "price" -> car.price,
      "mileage" -> car.mileage,
      "firstRegistration" -> dateToStr(car.firstRegistration),
      "new" -> false
    )
  }



  implicit val usedCarAdvert = (
    (JsPath \ "id").read[String] and
      (JsPath \ "title").read[String] and
      (JsPath \ "fuel").read[Fuel] and
      (JsPath \ "price").read[Int] and
      (JsPath \ "mileage").read[Int] and
      (JsPath \ "firstRegistration").read[LocalDate]
    )(UsedCarAdvert.apply _)
}

object CarAdvert {
  implicit  val carAdvertWrites = new Writes[CarAdvert] {
    def writes(car: CarAdvert): JsObject = car match {
      case newCar:NewCarAdvert => NewCarAdvert.newCarWrites.writes(newCar)
      case usedCar: UsedCarAdvert => UsedCarAdvert.usedCardWrites.writes(usedCar)
    }
  }

  implicit val carAdvertReads = new Reads[CarAdvert] {
    override def reads(json: JsValue): JsResult[CarAdvert] ={
      val res = for {
        obj <- json.validate[JsObject]
        isNew <- (obj \ "new").validate[Boolean]
      } yield if (isNew) obj.validate[NewCarAdvert] else obj.validate[UsedCarAdvert]

      res.flatMap(identity)
    }

  }
}