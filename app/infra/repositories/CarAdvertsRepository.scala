package infra.repositories


import java.util.UUID

import cats.effect.IO
import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder
import javax.inject.{Inject, Singleton}
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}

import scala.collection.mutable
import scala.collection.mutable.Map
import scala.concurrent.ExecutionContext
import org.scanamo._
import org.scanamo.syntax._
import org.scanamo.error.{DynamoReadError, ScanamoError}
import play.api.Logger

import scala.language.implicitConversions
import utils.Date._

import scala.concurrent.ExecutionContext.Implicits.global

case class CarAdvertEntity(id: String, title: String, fuel: Fuel, price: Int, mileage: Option[Int], firstRegisteration: Option[String], used: Boolean, version: Int)
object CarAdvertEntity {
  def toCarAdvert(entity: CarAdvertEntity): Option[CarAdvert] = entity match {
    case CarAdvertEntity(id, title, fuel, price, Some(mileage), Some(firstRegisteration), true, _) => Some(UsedCarAdvert(id, title, fuel, price, mileage, strToDate(firstRegisteration)))
    case CarAdvertEntity(id, title, fuel, price, _, _, false, _) => Some(NewCarAdvert(id, title, fuel, price))
    case _ => None
  }

  def fromCarAdvert(carAdvert: CarAdvert): CarAdvertEntity = carAdvert match {
    case NewCarAdvert(id, title, fuel, price) => CarAdvertEntity(id, title, fuel, price, None, None, false, 1)
    case UsedCarAdvert(id, title, fuel, price, mileage, firstRegisteration) => CarAdvertEntity(id, title, fuel, price, Some(mileage), Some(dateToStr(firstRegisteration)), true, 1)
  }

}

trait CarAdvertsRepository {
  def list(): IO[Iterable[CarAdvert]]

  def get(id: String): IO[Option[CarAdvert]]

  def create(carAdvert: CarAdvert): IO[Option[CarAdvert]]
  def update(carAdvert: CarAdvert): IO[Option[CarAdvert]]
}


object Utils {
  def generateRandomId(carAdvert: CarAdvert): (String, CarAdvert) = {
    val guid = UUID.randomUUID().toString
    val car: CarAdvert = carAdvert match {
      case NewCarAdvert(_, title, fuel, price) => NewCarAdvert(guid, title, fuel, price)
      case UsedCarAdvert(_, title, fuel, price, mileage, firstRegisteration) => UsedCarAdvert(guid, title, fuel, price, mileage, firstRegisteration)
    }
    (guid, car)
  }
}

@Singleton
class DynamodbCarAdvertsRepository @Inject()(region: String, hostEndpoint: String, key: String, secret: String) extends CarAdvertsRepository {
  import org.scanamo.auto._

  val logger = Logger("DynamodbCarAdvertsRepository")


  val creds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret))
  val client = AmazonDynamoDBAsyncClientBuilder.standard()
    .withClientConfiguration(
      new ClientConfiguration()
        .withMaxErrorRetry(0)
        .withConnectionTimeout(3000))    .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(hostEndpoint, region))
    .withCredentials(creds).build()
  val table = Table[CarAdvertEntity]("carAdverts")


  def unwrapEithers[T](iter: List[Either[DynamoReadError, T]]): List[T] = iter.foldRight(List[T]())((either, acc) => either match {
    case Left(err) => logger.warn("Failed to parse Rec from DB" + err)
      acc
    case Right(t) => t :: acc
  })

  def unwrapOption[T](wrapper: Option[Either[DynamoReadError, T]]): Option[T] = wrapper.flatMap {
    case Right(t) => Some(t)
    case Left(err) => logger.warn("Failed to parse Rec from db" + err)
                      None
  }

  def unwrapEither[T](eth: Either[ScanamoError, T]): Option[T] = eth match {
    case Right(t) => Some(t)
    case Left(err) => logger.error("ScanamoError" + err)
                      None
  }



  override def list() = IO.fromFuture(IO.pure {
    val ops = for {
      adverts <- table.scan.map(unwrapEithers)
    } yield adverts.map(CarAdvertEntity.toCarAdvert).flatten.toIterable

    ScanamoAsync.exec(client)(ops)
  })

  override def get(id: String): IO[Option[CarAdvert]] = IO.fromFuture(IO.pure{
    val ops = for {
      advert <- table.get('id -> id)
    } yield unwrapOption(advert).flatMap(CarAdvertEntity.toCarAdvert)
    ScanamoAsync.exec(client)(ops)
  })

  override def create(carAdvert: CarAdvert) = IO.fromFuture(IO.pure {
    val (_, car) = Utils.generateRandomId(carAdvert)
    val ops = for {
      res <- table.given(not(attributeExists('id))).put(CarAdvertEntity.fromCarAdvert(car))
    } yield res.toOption.map(_ => car)

    ScanamoAsync.exec(client)(ops)
  })

  override def update(carAdvert: CarAdvert) = IO.fromFuture(IO.pure{
    val entity = CarAdvertEntity.fromCarAdvert(carAdvert)
    val ops = for {
      res <- table.given(attributeExists('id)).update('id -> entity.id,
        set('title -> entity.title) and
        set('fuel -> entity.fuel) and
        set('price -> entity.price) and
        set('mileage -> entity.mileage) and
        set('firstRegisteration -> entity.firstRegisteration))
    }yield unwrapEither(res).flatMap(CarAdvertEntity.toCarAdvert)
    ScanamoAsync.exec(client)(ops)
  })
}


@Singleton
class InMemoryCarAdvertsRepository @Inject()()(implicit ex: ExecutionContext) extends CarAdvertsRepository {

  var storage: Map[String, CarAdvert] = new mutable.HashMap[String, CarAdvert]()

  override def list(): IO[Iterable[CarAdvert]] = IO {
    storage.values
  }

  override def get(id: String): IO[Option[CarAdvert]] = IO {
    storage.get(id)
  }

  override def create(carAdvert: CarAdvert): IO[Option[CarAdvert]] = IO {
    val (id, car) = Utils.generateRandomId(carAdvert)
    storage.put(id, car)
  }

  //
  //  override def delete(id: String): Future[Boolean] = ???
  //
  //  override def update(carAdvert: CarAdvert): Future[CarAdvert] = ???
  override def update(carAdvert: CarAdvert): IO[Option[CarAdvert]] = ???
}

