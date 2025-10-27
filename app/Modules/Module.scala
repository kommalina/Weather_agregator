package Modules

import Services._
import Repositories._
import com.google.inject.{AbstractModule, TypeLiteral}
import scala.concurrent.Future
import akka.stream.Materializer
import akka.actor.ActorSystem
import javax.inject.{Inject, Singleton, Provider}
import play.api.inject.ApplicationLifecycle
import utils.FlywayMigrator
import cats.effect.IO
import play.api.db.Database
import scala.concurrent.ExecutionContext
import play.api.Configuration
import play.api.libs.ws.WSClient


@Singleton
class MaterializerProviderImpl @Inject()(lifecycle: ApplicationLifecycle) extends Provider[Materializer] {
    private val actorSystem = ActorSystem("weather")

    lifecycle.addStopHook(() => {
        scala.concurrent.Future.successful(actorSystem.terminate())
    })
    
    override def get(): Materializer = Materializer(actorSystem)
}

@Singleton
class WeatherDataRepositoryProvider @Inject()(db: Database)(implicit ec: ExecutionContext) extends Provider[WeatherDataRepository[IO]] {
    override def get(): WeatherDataRepository[IO] = new WeatherDataRepositoryImpl[IO](db)
}

@Singleton
class WeatherDataServiceProvider @Inject()(
    repository: WeatherDataRepository[IO],
    config: Configuration,
    ws: WSClient
)(implicit ec: ExecutionContext, materializer: Materializer) extends Provider[WeatherDataService[IO]] {
    override def get(): WeatherDataService[IO] = new WeatherDataServiceImpl[IO](repository, config, ws)
}

class Module extends AbstractModule {
    override def configure(): Unit = {
        //Миграция 
        //FlywayMigrator.migrate()
        
        //Привязка репозитория
        bind(new TypeLiteral[WeatherDataRepository[IO]](){}).toProvider(new TypeLiteral[WeatherDataRepositoryProvider](){})

        //Привязка сервиса
        bind(new TypeLiteral[WeatherDataService[IO]](){}).toProvider(new TypeLiteral[WeatherDataServiceProvider](){})
        
        //Адаптер для контроллера(Future)
        bind(new TypeLiteral[WeatherDataService[Future]](){}).to(classOf[IOToFutureAdapter])
        
        //Materializer для WSClient
        bind(classOf[Materializer]).toProvider(classOf[MaterializerProviderImpl]).asEagerSingleton()
    }
}
