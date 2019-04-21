import javax.inject._
import com.google.inject.AbstractModule
import infra.repositories.{CarAdvertsRepository, DynamodbCarAdvertsRepository, InMemoryCarAdvertsRepository}
import net.codingwell.scalaguice.ScalaModule
import play.api.{Configuration, Environment}

/**
  * Sets up custom components for Play.
  *
  * https://www.playframework.com/documentation/latest/ScalaDependencyInjection
  */
class Module(environment: Environment, configuration: Configuration)
    extends AbstractModule
    with ScalaModule {

  override def configure() = {
//    bind[CarAdvertsRepository].to[InMemoryCarAdvertsRepository].in[Singleton]
    val accessKey = configuration.get[String]("aws.key")
    val secret = configuration.get[String]("aws.secret")
    val region = configuration.get[String]("aws.region")
    val dynamoEndpoint = configuration.get[String]("aws.dynamoUrl")
    val tableName = configuration.get[String]("tables.car-adverts")
    bind[CarAdvertsRepository].toInstance(new DynamodbCarAdvertsRepository(region,dynamoEndpoint, accessKey, secret, tableName))
  }
}
