import models.cardadvert.CarAdvert
import org.scalatestplus.play._
import org.scalatestplus.play.guice._
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{RequestHeader, Result}
import play.api.test._
import play.api.test.Helpers._
import play.api.test.CSRFTokenHelper._
import org.scalatest.Matchers._
import org.scalactic.Explicitly._
import scala.concurrent.Future

class CarAdvertsRouterSpec extends PlaySpec with GuiceOneAppPerTest {

  "PostRouter" should {

    "render the list of posts" in {
      val request = FakeRequest(GET, "/v1/caradverts").withHeaders(HOST -> "localhost:9000")
      val home:Future[Result] = route(app, request).get

      val cars: Seq[CarAdvert] = Json.fromJson[Seq[CarAdvert]](contentAsJson(home)).get
//      posts.filter(_.id == "1").head mustBe (PostResource("1","/v1/posts/1", "title 1", "blog post 1" ))
      cars.length should be > 0
    }


  }

}