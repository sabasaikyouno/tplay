import com.google.inject.AbstractModule
import domain.repository.TextDataRepository
import infrastructure.repository.TextDataRepositoryImpl
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure() = {
    bind(classOf[TextDataRepository]).to(classOf[TextDataRepositoryImpl])
  }
}
