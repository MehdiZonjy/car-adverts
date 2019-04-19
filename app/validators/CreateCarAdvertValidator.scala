package validators

import models.cardadvert.Fuel
import models.cardadvert.Fuel._
import play.api.data.{Form, FormError}
import play.api.data.format.Formatter
import services.{CreateNewCarAdvert, CreateUsedCarAdvert}



object CreateCarAdvertValidator {
  import play.api.data.Forms._

  implicit val fuelFormatter = new Formatter[Fuel] {
    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Fuel] =
      data.get(key).flatMap(fromString).toRight(Seq(FormError(key, "can only be gasoline or diesel")))

    override def unbind(key: String, value: Fuel): Map[String, String] = Map(key -> value.value)
  }

  val newCarAdvertForm: Form[CreateNewCarAdvert] = {

    Form(
      mapping(
        "title" -> nonEmptyText,
        "fuel" -> of[Fuel],
        "price" -> number(min = 1)
      )(CreateNewCarAdvert.apply)(CreateNewCarAdvert.unapply)
    )
  }

  val usedCarAdvertForm : Form[CreateUsedCarAdvert] = {
    Form(
      mapping(
        "title" -> nonEmptyText,
        "fuel" -> of[Fuel],
        "price" -> number(min = 1),
        "mileage" -> number,
        "firstRegisteration" -> localDate("dd-MM-yyyy")
      )(CreateUsedCarAdvert.apply)(CreateUsedCarAdvert.unapply)
    )
  }

}
