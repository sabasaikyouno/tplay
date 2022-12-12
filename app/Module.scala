import com.google.inject.AbstractModule
import domain.repository.{PostedDataRepository, RoomDataRepository}
import infrastructure.repository.{PostedDataRepositoryImpl, RoomDataRepositoryImpl}
import play.api.{Configuration, Environment}

class Module(environment: Environment, configuration: Configuration) extends AbstractModule {
  override def configure() = {
    bind(classOf[PostedDataRepository]).to(classOf[PostedDataRepositoryImpl])
    bind(classOf[RoomDataRepository]).to(classOf[RoomDataRepositoryImpl])
  }
}
