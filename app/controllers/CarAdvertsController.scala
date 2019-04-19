package controllers


import javax.inject.Inject
import play.api.i18n.I18nSupport
import play.api.mvc.ControllerComponents
import play.api.libs.json.Json
import play.api.mvc._
import services.{CarAdvertsService, CreateUsedCarAdvert}
import validators.CreateCarAdvertValidator

class CarAdvertsController @Inject()(cc: ControllerComponents, carAdvertsService: CarAdvertsService) extends AbstractController(cc) with I18nSupport {

  def hello = Action {
    Ok("Hello World")
  }

  def list = Action {
    Ok(Json.toJson(carAdvertsService.list().unsafeRunSync()))
  }

  def createNewAdvert = Action {
    implicit request =>
    CreateCarAdvertValidator.newCarAdvertForm.bindFromRequest().fold(
      badForm => BadRequest(badForm.errorsAsJson),
      cmd => Ok(Json.toJson(carAdvertsService.create(cmd).unsafeRunSync()))
    )
  }

  def createUsedAdvert = Action {
    implicit request =>
      CreateCarAdvertValidator.usedCarAdvertForm.bindFromRequest().fold(
        badForm => BadRequest(badForm.errorsAsJson),
        cmd => Ok(Json.toJson(carAdvertsService.create(cmd).unsafeRunSync()))
      )
  }
}
