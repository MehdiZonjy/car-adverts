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
    bind[CarAdvertsRepository].toInstance(new DynamodbCarAdvertsRepository("us-east-1","http://localhost:8000","key","secret"))
  }
}
