package services

import java.time.LocalDate

import cats.effect.IO
import infra.repositories.CarAdvertsRepository
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}
import org.scalatestplus.play.PlaySpec
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito._
import org.mockito.ArgumentMatchers._


class CarAdvertsServiceSpec extends PlaySpec with MockitoSugar{

  "CarAdvertsService#get" must {
    "must call CarAdvertsRepository with Id and return CarAdvert" in {
      val repo = mock[CarAdvertsRepository]
      val carAdvert = mock[CarAdvert]
      val id = "id1"
      when(repo.get(id)) thenReturn IO.pure(Some(carAdvert))

      val service = new CarAdvertsService(repo)
      service.get(id).unsafeRunSync mustEqual Some(carAdvert)
    }
    "must return None when CarAdvert was not found in Repository" in {
      val repo = mock[CarAdvertsRepository]
      val id = "id1"
      when(repo.get(id)) thenReturn IO.pure(None: Option[CarAdvert])

      val service = new CarAdvertsService(repo)
      service.get(id).unsafeRunSync mustEqual None
    }
  }

  "CarAdvertsService#create" must {
    "must call repo with NewCarAdvert when given CreateNewCarAdvert command" in {
      val repo = mock[CarAdvertsRepository]

      val createNewCarAdvert = CreateNewCarAdvert("title1", Fuel.Diesel, 900)
      val newCarAdvert = NewCarAdvert("", createNewCarAdvert.title, createNewCarAdvert.fuel, createNewCarAdvert.price)

      when(repo.create(newCarAdvert)) thenReturn IO.pure(Some(newCarAdvert))

      val service = new CarAdvertsService(repo)
      service.create(createNewCarAdvert).unsafeRunSync mustEqual Some(newCarAdvert)
    }

    "must call repo with UsedCarAdvert when given CreateUsedCarAdvert command " in {
      val repo = mock[CarAdvertsRepository]

      val createUsedCarAdvert = CreateUsedCarAdvert("title1", Fuel.Diesel, 900, 200, LocalDate.now())
      val newCarAdvert = UsedCarAdvert("", createUsedCarAdvert.title, createUsedCarAdvert.fuel, createUsedCarAdvert.price, createUsedCarAdvert.mileage,createUsedCarAdvert.firstRegistration)

      when(repo.create(newCarAdvert)) thenReturn IO.pure(Some(newCarAdvert))

      val service = new CarAdvertsService(repo)
      service.create(createUsedCarAdvert).unsafeRunSync mustEqual Some(newCarAdvert)
    }

    "must return None when unable to create UsedCarAdvert" in {
      val repo = mock[CarAdvertsRepository]

      val createUsedCarAdvert = CreateUsedCarAdvert("title1", Fuel.Diesel, 900, 200, LocalDate.now())

      when(repo.create(any[CarAdvert])) thenReturn IO.pure(None: Option[CarAdvert])

      val service = new CarAdvertsService(repo)
      service.create(createUsedCarAdvert).unsafeRunSync mustEqual None
    }

    "must return none when unable to create NewCarAdvert" in {
      val repo = mock[CarAdvertsRepository]

      val createNewCarAdvert = CreateNewCarAdvert("title1", Fuel.Diesel, 900)
      when(repo.create(any[CarAdvert])) thenReturn IO.pure(None: Option[NewCarAdvert])

      val service = new CarAdvertsService(repo)
      service.create(createNewCarAdvert).unsafeRunSync mustEqual None
    }
  }

  "CarAdvertsService#delete" must {
    "return true when carAdvert is deleted, and call repo with CarAdvert id" in {
      val repo = mock[CarAdvertsRepository]

      val id = "id1"
      when(repo.delete(id)) thenReturn IO.pure(true)
      val service = new CarAdvertsService(repo)

      service.delete(id).unsafeRunSync() mustEqual true
    }

    "return false when carAdvert is not deleted" in {
      val repo = mock[CarAdvertsRepository]

      val id = "id1"
      when(repo.delete(id)) thenReturn IO.pure(false)
      val service = new CarAdvertsService(repo)

      service.delete(id).unsafeRunSync() mustEqual false
    }
  }

  "CarAdvertsService#update" must {
    "return None when CarAdvert is not found" in {
      val repo = mock[CarAdvertsRepository]
      val id = "id1"
      when(repo.get(id)) thenReturn IO.pure(None: Option[CarAdvert])
      val updateCarAdvert = mock[UpdateCarAdvert]
      val service = new CarAdvertsService(repo)
      service.update(id, updateCarAdvert).unsafeRunSync mustEqual None
    }

    "Update NewCarAdvert properties and return new values" in {
      val repo = mock[CarAdvertsRepository]
      val id = "id1"
      val oldAdvert = NewCarAdvert(id, "title1", Fuel.Diesel, 90)
      val newTitle = "title2"
      val newFuel = Fuel.Gasoline
      val newPrice = 100
      val cmd = UpdateCarAdvert(Some("title2"), Some(newFuel), Some(newPrice), None, None)
      val updatedCarAdvert = NewCarAdvert(id, newTitle, newFuel, newPrice)

      when(repo.get(id)) thenReturn IO.pure(Some(oldAdvert))
      when(repo.update(updatedCarAdvert)) thenReturn IO.pure(Some(updatedCarAdvert))


      val service = new CarAdvertsService(repo)
      service.update(id, cmd).unsafeRunSync mustEqual Some(updatedCarAdvert)
    }
    "Update UsedCarAdvert properties and return new values" in {
      val repo = mock[CarAdvertsRepository]
      val id = "id1"
      val oldAdvert = UsedCarAdvert(id, "title1", Fuel.Diesel, 90, 100, LocalDate.now())

      val newTitle = "title2"
      val newFuel = Fuel.Gasoline
      val newPrice = 100
      val newMileage = 900
      val newFirstRegisteration = utils.Date.strToDate("1990-09-07")


      val cmd = UpdateCarAdvert(Some("title2"), Some(newFuel), Some(newPrice), Some(newMileage), newFirstRegisteration)
      val updatedCarAdvert = UsedCarAdvert(id, newTitle, newFuel, newPrice, newMileage, newFirstRegisteration.get)

      when(repo.get(id)) thenReturn IO.pure(Some(oldAdvert))
      when(repo.update(updatedCarAdvert)) thenReturn IO.pure(Some(updatedCarAdvert))


      val service = new CarAdvertsService(repo)
      service.update(id, cmd).unsafeRunSync mustEqual Some(updatedCarAdvert)
    }
  }
  "CarAdvertsService#list" must{
    "return list of CarAdverts  sorted by id" in {
      val repo = mock[CarAdvertsRepository]
      val ad1 = NewCarAdvert("id1", "title", Fuel.Diesel, 1)
      val ad2 = NewCarAdvert("id2", "title", Fuel.Diesel, 1)

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.Id)).unsafeRunSync() mustEqual List(ad1, ad2)
    }
    "return list of CarAdverts  sorted by title" in {
      val repo = mock[CarAdvertsRepository]
      val ad1 = NewCarAdvert("id", "title1", Fuel.Diesel, 1)
      val ad2 = NewCarAdvert("id", "title2", Fuel.Diesel, 1)

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.Title)).unsafeRunSync() mustEqual List(ad1, ad2)
    }
    "return list of CarAdverts  sorted by price" in {
      val repo = mock[CarAdvertsRepository]
      val ad1 = NewCarAdvert("id", "title", Fuel.Diesel, 10)
      val ad2 = NewCarAdvert("id", "title", Fuel.Diesel, 20)

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.Price)).unsafeRunSync() mustEqual List(ad1, ad2)
    }
    "return list of CarAdverts  sorted by mileage while prioritising NewCarAdvert" in {
      val repo = mock[CarAdvertsRepository]
      val ad0 = NewCarAdvert("id", "title", Fuel.Diesel, 1)
      val ad1 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 10, LocalDate.now())
      val ad2 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 20, LocalDate.now())

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1, ad0))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.Mileage)).unsafeRunSync() mustEqual List(ad0, ad1, ad2)
    }

    "return list of CarAdverts  sorted by firstRegistration" in {
      val repo = mock[CarAdvertsRepository]
      val ad1 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 10, utils.Date.strToDate("1990-09-07").get)
      val ad2 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 20, utils.Date.strToDate("2019-04-20").get)

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.FirstRegistration)).unsafeRunSync() mustEqual List(ad1, ad2)
    }
    "return list of CarAdverts  sorted by firstRegistration while prioritising NewCarAdvert" in {
      val repo = mock[CarAdvertsRepository]
      val ad0 = NewCarAdvert("id", "title", Fuel.Diesel, 1)
      val ad1 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 10, utils.Date.strToDate("1990-09-07").get)
      val ad2 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 20, utils.Date.strToDate("2019-04-20").get)

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1, ad0))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.FirstRegistration)).unsafeRunSync() mustEqual List(ad0, ad1, ad2)
    }
    "return list of carAdverts sorted by new first" in {
      val repo = mock[CarAdvertsRepository]
      val ad1 = NewCarAdvert("id", "title", Fuel.Diesel, 1)
      val ad2 = UsedCarAdvert("id", "title", Fuel.Diesel, 1, 10, utils.Date.strToDate("1990-09-07").get)

      when(repo.list()) thenReturn IO.pure(List(ad2, ad1))
      val service = new CarAdvertsService(repo)
      service.list(QueryCarAdverts(CarAdvertOrderBy.FirstRegistration)).unsafeRunSync() mustEqual List(ad1, ad2)
    }
  }


}
