package controllers


import javax.inject.Inject
import play.api.Logger
import play.api.i18n.I18nSupport
import play.api.mvc.ControllerComponents
import play.api.libs.json.Json
import play.api.mvc._
import services.{CarAdvertsService, CreateNewCarAdvert, CreateUsedCarAdvert, UpdateCarAdvert}
import validators.CarAdvertValidator

class CarAdvertsController @Inject()(cc: ControllerComponents, carAdvertsService: CarAdvertsService) extends AbstractController(cc) with I18nSupport {

  val logger = Logger("CarAdvertsController")
  def hello = Action {
    Ok("Hello World")
  }

  def list = Action{
    carAdvertsService.list().attempt.unsafeRunSync() match {
      case Right(adverts) =>  Ok(Json.toJson(adverts))
      case Left(err) => logger.error("Failed to fetch adverts list",err)
                        ServiceUnavailable
    }
  }

  def createNewAdvert = Action {
    implicit request =>

    def create(cmd: CreateNewCarAdvert) = carAdvertsService.create(cmd).attempt.unsafeRunSync match {
      case Right(Some(advert)) => Created(Json.toJson(advert))
      case Right(None) => Conflict("Unable to create NewCarAdvert due to id collision, try again")
      case Left(err) => logger.error("Failed to create newCarAdvert", err)
                        ServiceUnavailable
    }

    CarAdvertValidator.newCarAdvertForm.bindFromRequest().fold(
      badForm => BadRequest(badForm.errorsAsJson),
      cmd => create(cmd)
    )
  }

  def createUsedAdvert = Action {
    implicit request =>

      def create(cmd: CreateUsedCarAdvert) = carAdvertsService.create(cmd).attempt.unsafeRunSync match {
        case Right(Some(advert)) => Created(Json.toJson(advert))
        case Right(None) => Conflict("Unable to create UsedCarAdvert due to id collision, try again")
        case Left(err) => logger.error("Failed to create UsedCarAdvert", err)
          ServiceUnavailable
      }

      CarAdvertValidator.usedCarAdvertForm.bindFromRequest().fold(
        badForm => BadRequest(badForm.errorsAsJson),
        cmd => create(cmd)
      )
  }


  def show(id: String) = Action {
    carAdvertsService.get(id).attempt.unsafeRunSync match {
      case Right(None) => NotFound("Advert not found")
      case Right(Some(advert)) => Ok(Json.toJson(advert))
      case Left(err) => logger.error("Failed to fetch car advert", err)
        ServiceUnavailable
    }
  }

  def update(id: String) = Action {
    implicit request =>

      def update(cmd: UpdateCarAdvert) = carAdvertsService.update(id, cmd).attempt.unsafeRunSync match {
        case Right(None) => NotFound("Car advert not found")
        case Right(Some(advert)) => Ok(Json.toJson(advert))
        case Left(err) => logger.error("Failed to update advert", err)
                          ServiceUnavailable
      }
      CarAdvertValidator.updateCarAdvertForm.bindFromRequest().fold(
        badForm => BadRequest(badForm.errorsAsJson),
        cmd => update(cmd)
      )
  }

  def delete(id: String) = Action {
    carAdvertsService.delete(id).attempt.unsafeRunSync match {
      case Left(err) => logger.error("Failed to delete CarAdvert", err)
                        ServiceUnavailable
      case Right(true) => NoContent
      case Right(false) => NotFound
    }
  }
}
