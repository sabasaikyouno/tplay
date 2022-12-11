import com.google.inject.AbstractModule
import domain.repository.{ImageDataRepository, TextDataRepository}
import infrastructure.repository.{ImageDataRepositoryImpl, TextDataRepositoryImpl}
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure() = {
    bind(classOf[TextDataRepository]).to(classOf[TextDataRepositoryImpl])
    bind(classOf[ImageDataRepository]).to(classOf[ImageDataRepositoryImpl])
  }
}
