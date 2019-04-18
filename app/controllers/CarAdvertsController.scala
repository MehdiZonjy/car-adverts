package controllers

import java.time.LocalDate

import javax.inject.Inject
import models.cardadvert.{Fuel, NewCar, UsedCar, CarAdvert}
import play.api.mvc.ControllerComponents
import play.api.libs.json.Json
import play.api.mvc._

class CarAdvertsController @Inject()(cc: ControllerComponents) extends AbstractController(cc) {

  def hello = Action {
    Ok("Hello World")
  }

  def list = Action {
    val cars: Array[CarAdvert] = Array(NewCar("car1", "Hello", Fuel.Diesel, 123), UsedCar("car2", "used", Fuel.Gasoline, 123, 333, LocalDate.now() ))
    Ok(Json.toJson(cars))
  }
}
