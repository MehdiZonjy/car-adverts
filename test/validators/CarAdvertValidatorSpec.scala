package validators

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import models.cardadvert.Fuel
import org.scalatestplus.play.PlaySpec
import services.{CreateNewCarAdvert, CreateUsedCarAdvert, UpdateCarAdvert}
import validators.CarAdvertValidator.{newCarAdvertForm, usedCarAdvertForm, updateCarAdvertForm}

class CarAdvertValidatorSpec extends PlaySpec{

  "CreateNewCarAdvert Form Validator" must {
    "Fail when price is not numeric" in {
      newCarAdvertForm.bind(Map("price" -> "Hello World")).error("price").isDefined mustBe true
    }
    "Fail when title is missing" in {
      newCarAdvertForm.bind(Map("" -> "")).error("title").isDefined mustBe true
    }
    "Fail when Fuel is random" in {
      newCarAdvertForm.bind(Map("fuel" -> "Random")).error("fuel").isDefined mustBe true
    }
    "Create a valid CreateNewCarAdvert" in {
      val expectedCmd = CreateNewCarAdvert("advert title", Fuel.Diesel, 90)
      val createNewCarAdvert = newCarAdvertForm.bind(Map(
        "price" -> expectedCmd.price.toString,
        "title" -> expectedCmd.title,
        "fuel" -> expectedCmd.fuel.value
      )).get

      createNewCarAdvert mustEqual expectedCmd
    }
  }

  "CreateUsedCarAdvert Form Validator" must {
    "Fail when price is not numeric" in {
      usedCarAdvertForm.bind(Map("price" -> "Hello World")).error("price").isDefined mustBe true
    }
    "Fail when title is missing" in {
      usedCarAdvertForm.bind(Map("" -> "")).error("title").isDefined mustBe true
    }
    "Fail when Fuel is random" in {
      usedCarAdvertForm.bind(Map("fuel" -> "Random")).error("fuel").isDefined mustBe true
    }
    "Fail when mileage is not numberic" in {
      usedCarAdvertForm.bind(Map("mileage" -> "Random")).error("mileage").isDefined mustBe true
    }
    "Fail when firstRegisteration is not valid date format dd-mm-yyyy" in {
      usedCarAdvertForm.bind(Map("firstRegisteration" -> "1990-09-07")).error("firstRegisteration").isDefined mustBe true
    }
    "Create a valid CreateUsedCarAdvert" in {
      val expectedCmd = CreateUsedCarAdvert("advert title", Fuel.Diesel, 90, 1000, LocalDate.now())
      val createUsedCarAdvert = usedCarAdvertForm.bind(Map(
        "price" -> expectedCmd.price.toString,
        "title" -> expectedCmd.title,
        "fuel" -> expectedCmd.fuel.value,
        "mileage" -> expectedCmd.mileage.toString,
        "firstRegisteration" -> expectedCmd.firstRegisteration.format( DateTimeFormatter.ofPattern("dd-MM-yyyy"))
      )).get

      createUsedCarAdvert mustEqual expectedCmd
    }
  }

  "UpdateCarAdvert Form Validator" must {
    "Fail when price is not numeric" in {
      updateCarAdvertForm.bind(Map("price" -> "Hello World")).error("price").isDefined mustBe true
    }
    "Fail when firstRegisteration format is not correct" in {
      updateCarAdvertForm.bind(Map("firstRegisteration" -> "random")).error("firstRegisteration").isDefined mustBe true
    }
    "Fail when fuel type is not correct" in {
      updateCarAdvertForm.bind(Map("fuel" -> "random")).error("fuel").isDefined mustBe true

    }
    "Create Optional fields for UpdateCarAdvert cmd" in {
      updateCarAdvertForm.bind(Map[String,String]()).get mustEqual UpdateCarAdvert(None,None,None,None,None)
    }
    "Fill in provided fields for UpdateCarAdvert cmd" in {
      val expectedCmd = UpdateCarAdvert(Some("advert title"), Some(Fuel.Diesel), Some(90), Some(1000), Some(LocalDate.now()))
      val cmd =updateCarAdvertForm.bind(Map(
        "title" -> expectedCmd.title.get,
        "fuel" -> expectedCmd.fuel.get.value,
        "price" -> expectedCmd.price.get.toString,
        "mileage" -> expectedCmd.mileage.get.toString,
        "firstRegisteration" -> expectedCmd.firstRegisteration.get.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"))
      )).get

      cmd mustEqual expectedCmd
    }
  }
}
