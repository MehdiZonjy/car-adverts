import com.amazonaws.ClientConfiguration
import com.amazonaws.auth.{AWSStaticCredentialsProvider, BasicAWSCredentials}
import com.amazonaws.client.builder.AwsClientBuilder
import com.amazonaws.services.dynamodbv2.{AmazonDynamoDBAsync, AmazonDynamoDBAsyncClientBuilder}
import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
import models.cardadvert.{CarAdvert, Fuel, NewCarAdvert, UsedCarAdvert}
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{RequestHeader, Result}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._
import org.scalatest.Matchers._
import org.scalactic.Explicitly._
import org.scalatest.{TestData, TestSuite}
import play.api.libs.json._
import play.api.Application
import play.api.http.Status
import play.api.inject.guice.GuiceApplicationBuilder

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try


class CarAdvertsRouterSpec
  extends PlaySpec with GuiceOneAppPerTest   {

  val table = "carAdvertsTest"
  override def newAppForTest(td: TestData): Application = {
    GuiceApplicationBuilder().configure(Map("tables.car-adverts" -> table)).build()
  }

  lazy val dynamoClient: AmazonDynamoDBAsync = {
    val configuration = app.configuration
    val key = configuration.get[String]("aws.key")
    val secret = configuration.get[String]("aws.secret")
    val region = configuration.get[String]("aws.region")
    val dynamoEndpoint = configuration.get[String]("aws.dynamoUrl")

    val creds = new AWSStaticCredentialsProvider(new BasicAWSCredentials(key, secret))
    val client: AmazonDynamoDBAsync = AmazonDynamoDBAsyncClientBuilder.standard()
      .withClientConfiguration(
        new ClientConfiguration()
          .withMaxErrorRetry(0)
          .withConnectionTimeout(3000)).withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(dynamoEndpoint, region))
      .withCredentials(creds).build()

    client
  }

  def resetTable(): Unit ={
    import com.amazonaws.services.dynamodbv2.model.AttributeDefinition
    import com.amazonaws.services.dynamodbv2.model.CreateTableRequest
    import com.amazonaws.services.dynamodbv2.model.KeySchemaElement
    import com.amazonaws.services.dynamodbv2.model.KeyType
    import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughput
    import com.amazonaws.services.dynamodbv2.model.ScalarAttributeType
    import java.util
    val elements = new util.ArrayList[KeySchemaElement]
    val hashKey = new KeySchemaElement().withKeyType(KeyType.HASH).withAttributeName("id")
    elements.add(hashKey)
    val attributeDefinitions = new util.ArrayList[AttributeDefinition]
    attributeDefinitions.add(new AttributeDefinition().withAttributeName("id").withAttributeType(ScalarAttributeType.S))

    val createTableRequest = new CreateTableRequest()
      .withTableName(table)
      .withKeySchema(elements)
      .withProvisionedThroughput(new ProvisionedThroughput().withReadCapacityUnits(5L)
        .withWriteCapacityUnits(5L))
      .withAttributeDefinitions(attributeDefinitions)


    Try(dynamoClient.deleteTable(table))
    dynamoClient.createTable(createTableRequest)
  }

  def advertsCount: Int = {
    val request = FakeRequest(GET, "/v1/caradverts").withHeaders(HOST -> "localhost:9000")
    val fResponse:Future[Result] = route(app, request).get
    Json.fromJson[Seq[CarAdvert]](contentAsJson(fResponse)).get.length
  }

  def addCarAdvert(advert: UsedCarAdvert): Future[Result] = {
    val request = FakeRequest(POST, "/v1/caradverts/used").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map(
      "title" -> JsString(advert.title),
      "price" -> JsNumber(advert.price),
      "fuel" -> JsString(advert.fuel.value),
      "mileage" -> JsNumber(advert.mileage),
      "firstRegistration" -> JsString(utils.Date.dateToStr(advert.firstRegistration))
    )))
    val response: Future[Result] = route(app, request).get

    response
  }

  def addCarAdvert(advert: NewCarAdvert): Future[Result] = {
    val request = FakeRequest(POST, "/v1/caradverts/new").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map(
      "title" -> JsString(advert.title),
      "price" -> JsNumber(advert.price),
      "fuel" -> JsString(advert.fuel.value)
    )))
    val response: Future[Result] = route(app, request).get

    response
  }

  "CarAdverts Router" should  {

    "POST /v1/caradverts/newadvert" should {
      "create NewCarAdvert and return CREATED" in  {
        resetTable()
        advertsCount should equal(0)

        val expectedAd = NewCarAdvert("", "title1", Fuel.Gasoline, 300)

        val response: Future[Result] = addCarAdvert(expectedAd)

        status(response) should equal(Status.CREATED)
        val car: NewCarAdvert = Json.fromJson[NewCarAdvert](contentAsJson(response)).get

        car mustEqual expectedAd.copy(id = car.id)
        advertsCount should equal(1)
      }

      "returns BadRequest when NewCarAdvert payload is not valid" in {
        resetTable()
        advertsCount should equal(0)
        val request = FakeRequest(POST, "/v1/caradverts/new").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map[String, JsValue]()))
        val response: Future[Result] = route(app, request).get
        status(response) should equal(Status.BAD_REQUEST)
        advertsCount should equal(0)

      }
    }

    "POST /v1/caradverts/usedadvert" should {


      "create UsedCardAdvert and return CREATED" in {
        resetTable()
        advertsCount should equal(0)

        val expectedAd = UsedCarAdvert("","title1", Fuel.Diesel, 300, 500 ,utils.Date.strToDate("2019-04-22").get)



        val response: Future[Result] = addCarAdvert(expectedAd)
        status(response) should equal(Status.CREATED)

        val car: UsedCarAdvert = Json.fromJson[UsedCarAdvert](contentAsJson(response)).get
        car mustEqual expectedAd.copy(id=car.id)

        advertsCount should equal(1)
      }

      "returns BadRequest when NewCarAdvert payload is not valid" in {
        resetTable()
        advertsCount should equal(0)
        val request = FakeRequest(POST, "/v1/caradverts/used").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map[String, JsValue]()))
        val response: Future[Result] = route(app, request).get
        status(response) should equal(Status.BAD_REQUEST)
        advertsCount should equal(0)
      }
    }

    "GET /v1/caradverts/:id" should {
      "return NotFound when calling advert id does not exist" in {
        resetTable()
        advertsCount should equal(0)
        val request = FakeRequest(POST, "/v1/caradverts/random-id").withHeaders(HOST -> "localhost:9000")
        val response: Future[Result] = route(app, request).get
        status(response) should equal (Status.NOT_FOUND)
      }
      "return request NewCarAdvert" in {
        resetTable()
        advertsCount should equal(0)

        val newAdPayload = NewCarAdvert("", "title1", Fuel.Gasoline, 300)
        val expectedAd = Json.fromJson[NewCarAdvert](contentAsJson(addCarAdvert(newAdPayload))).get

        val request = FakeRequest(GET, s"/v1/caradverts/${expectedAd.id}").withHeaders(HOST -> "localhost:9000")

        val response: Future[Result] = route(app, request).get

        status(response) should equal(Status.OK)

        val carAd: NewCarAdvert = Json.fromJson[NewCarAdvert](contentAsJson(response)).get
        carAd mustEqual expectedAd

      }

      "return requested UsedCarAdvert" in {
        resetTable()
        advertsCount should equal(0)

        val usedCarPayload = UsedCarAdvert("","title1", Fuel.Diesel, 300, 500 ,utils.Date.strToDate("2019-04-22").get)
        val expectedAd = Json.fromJson[UsedCarAdvert](contentAsJson(addCarAdvert(usedCarPayload))).get

        val request = FakeRequest(GET, s"/v1/caradverts/${expectedAd.id}").withHeaders(HOST -> "localhost:9000")

        val response: Future[Result] = route(app, request).get

        status(response) should equal(Status.OK)

        val carAd: UsedCarAdvert = Json.fromJson[UsedCarAdvert](contentAsJson(response)).get
        carAd mustEqual expectedAd
      }
    }
  }

  "PUT /v1/caradverts/:id" should {
    "return NotFound when id does not exist" in {
      resetTable()
      advertsCount should equal(0)
      val request = FakeRequest(PUT, s"/v1/caradverts/random-stuff").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map[String, JsValue]()))
      val response: Future[Result] = route(app, request).get
      status(response) should equal (NOT_FOUND)
    }
    "update UsedCarAdvert" in {
      resetTable()

      val usedCarPayload = UsedCarAdvert("","title1", Fuel.Diesel, 300, 500 ,utils.Date.strToDate("2019-04-22").get)
      val oldAd = Json.fromJson[UsedCarAdvert](contentAsJson(addCarAdvert(usedCarPayload))).get

      var newTitle = "New Title"
      val request = FakeRequest(PUT, s"/v1/caradverts/${oldAd.id}").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map(
        "title" -> JsString(newTitle),
      )))

      val response: Future[Result] = route(app, request).get
      status(response) should equal(Status.OK)

      val updatedAd: UsedCarAdvert = Json.fromJson[UsedCarAdvert](contentAsJson(response)).get
      updatedAd mustEqual oldAd.copy(title = newTitle)
    }
    "update NewCarAdvert" in {
      resetTable()

      val newCarPayload = NewCarAdvert("", "title1", Fuel.Gasoline, 300)
      val oldAd = Json.fromJson[NewCarAdvert](contentAsJson(addCarAdvert(newCarPayload))).get

      var newTitle = "New Title"
      val request = FakeRequest(PUT, s"/v1/caradverts/${oldAd.id}").withHeaders(HOST -> "localhost:9000").withJsonBody(JsObject(Map(
        "title" -> JsString(newTitle),
      )))

      val response: Future[Result] = route(app, request).get
      status(response) should equal(Status.OK)

      val updatedAd: NewCarAdvert = Json.fromJson[NewCarAdvert](contentAsJson(response)).get
      updatedAd mustEqual oldAd.copy(title = newTitle)
    }
  }

  "DELETE /v1/caradverts/:id" should {
    "return NOTFound when CarAdvert does not exist" in {
      resetTable()
      val request = FakeRequest(DELETE, s"/v1/caradverts/random-id").withHeaders(HOST -> "localhost:9000")
      val response: Future[Result] = route(app, request).get
      status(response) should equal(Status.NOT_FOUND)
    }
    "return NoContent when CarAdvert is deleted" in {
      resetTable()
      val payload = NewCarAdvert("", "title1", Fuel.Gasoline, 300)
      val carAdvert = Json.fromJson[NewCarAdvert](contentAsJson(addCarAdvert(payload))).get

      advertsCount should equal(1)

      val request = FakeRequest(DELETE, s"/v1/caradverts/${carAdvert.id}").withHeaders(HOST -> "localhost:9000")
      val response: Future[Result] = route(app, request).get
      status(response) should equal(Status.NO_CONTENT)

      advertsCount should equal(0)
    }
  }

  "GET /v1/caradverts" should {
    "return all CarAdverts sorted by id" in {
      resetTable()

      val payload1 = NewCarAdvert("", "title1", Fuel.Gasoline, 300)
      val payload2 = UsedCarAdvert("","title1", Fuel.Diesel, 300, 500 ,utils.Date.strToDate("2019-04-22").get)


      val ad1 = Json.fromJson[NewCarAdvert](contentAsJson(addCarAdvert(payload1))).get
      val ad2 = Json.fromJson[UsedCarAdvert](contentAsJson(addCarAdvert(payload2))).get

      val request = FakeRequest(GET, "/v1/caradverts").withHeaders(HOST -> "localhost:9000")
      val response: Future[Result] = route(app, request).get

      status(response) should equal(Status.OK)

      val ads: List[CarAdvert] = Json.fromJson[List[CarAdvert]](contentAsJson(response)).get
      val sortedAds = if(ad1.id.compareTo(ad2.id) <0)
          List[CarAdvert](ad1, ad2)
      else
        List[CarAdvert](ad2, ad1)

      ads should equal(sortedAds)
    }

    "return all CarAdverts sorted by `orderBy`" in {
      resetTable()

      val payload1 = NewCarAdvert("", "z title", Fuel.Gasoline, 300)
      val payload2 = UsedCarAdvert("","a title", Fuel.Diesel, 300, 500 ,utils.Date.strToDate("2019-04-22").get)


      val ad1 = Json.fromJson[NewCarAdvert](contentAsJson(addCarAdvert(payload1))).get
      val ad2 = Json.fromJson[UsedCarAdvert](contentAsJson(addCarAdvert(payload2))).get

      val request = FakeRequest(GET, "/v1/caradverts").withHeaders(HOST -> "localhost:9000")
      val response: Future[Result] = route(app, request).get

      status(response) should equal(Status.OK)

      val ads: List[CarAdvert] = Json.fromJson[List[CarAdvert]](contentAsJson(response)).get

      ads should equal(List(ad2, ad1))
    }
  }


}