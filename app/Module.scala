import com.google.inject.AbstractModule
import domain.repository.PostedDataRepository
import infrastructure.repository.PostedDataRepositoryImpl
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure() = {
    bind(classOf[PostedDataRepository]).to(classOf[PostedDataRepositoryImpl])
  }
}
