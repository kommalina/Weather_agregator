package Repositories

import models.WeatherModel
import javax.inject._
import scala.concurrent.ExecutionContext
import play.api.db.Database
import java.time.LocalDateTime
import java.sql.ResultSet
import scala.collection.mutable.Buffer
import java.sql.Timestamp
import cats.effect.Sync
import cats.syntax.all._

//Реализация репозитория для работы в БД
@Singleton
class WeatherDataRepositoryImpl[F[_]: Sync] @Inject()(db: Database)(implicit ec: ExecutionContext) extends WeatherDataRepository[F] {

    //Получение данных о погоде по локации
    override def getWeatherByLocation(location: String): F[Seq[WeatherModel]] = Sync[F].blocking {
        db.withConnection{conn =>
            //Создание SQL-запроса с параметрами 
            val stmt = conn.prepareStatement("SELECT * FROM weather WHERE location = ?")    
            stmt.setString(1, location)
            //Выполнение SQL-запроса и возвращение объекта ResultSet
            val rs = stmt.executeQuery()
            val buffer = Buffer[WeatherModel]()    //Хранение элементов типа WeatherModel
            //Считывание строк из rs и конуструирование WeatherModel
            while(rs.next()){
                buffer.append(
                    WeatherModel(
                        rs.getInt("id"),
                        rs.getString("serviceName"),
                        rs.getString("location"),
                        rs.getBigDecimal("temperature").doubleValue(),
                        rs.getString("metcast"),
                        rs.getTimestamp("dateAndTime").toLocalDateTime()    
                    )
                )
            }
            rs.close()
            stmt.close()
            buffer.toSeq
        }
    }

    //Получение всех данных о погоде
    override def getAllWeather(): F[Seq[WeatherModel]] = Sync[F].blocking {
        db.withConnection{conn =>
            val stmt = conn.prepareStatement("SELECT * FROM weather ORDER BY dateAndTime DESC")
            val rs = stmt.executeQuery()
            val buffer = Buffer[WeatherModel]()
            while(rs.next()){
                buffer.append(
                    WeatherModel(
                        rs.getInt("id"),
                        rs.getString("serviceName"),
                        rs.getString("location"),
                        rs.getBigDecimal("temperature").doubleValue(),
                        rs.getString("metcast"),
                        rs.getTimestamp("dateAndTime").toLocalDateTime()    
                    )
                )
            }
            rs.close()
            stmt.close()
            buffer.toSeq
        }
    }

    //Получение последних данных о погоде со всех городов
    override def getLatestWeather(locations: List[String], services: List[String]): F[Seq[WeatherModel]] = Sync[F].blocking {
        db.withConnection { conn =>
            val buffer = Buffer[WeatherModel]()
            for {
                location <- locations
                service <- services
            } {
                val stmt = conn.prepareStatement( 
                    "SELECT * FROM weather WHERE location = ? AND serviceName = ? ORDER BY dateAndTime DESC LIMIT 1"
                )
                stmt.setString(1, location)
                stmt.setString(2, service)

                val rs = stmt.executeQuery()

                if (rs.next()){
                    buffer.append(
                        WeatherModel(
                            rs.getInt("id"),
                            rs.getString("serviceName"),
                            rs.getString("location"),
                            rs.getBigDecimal("temperature").doubleValue(),
                            rs.getString("metcast"),
                            rs.getTimestamp("dateAndTime").toLocalDateTime()    
                        )
                    )
                }
                rs.close()
                stmt.close()
            }
            buffer.toSeq
        }    
    }

    //Вставка записи о погоде в БД 
    override def insert(weather: WeatherModel): F[WeatherModel] = Sync[F].blocking {
        db.withConnection{conn =>
            val stmt = conn.prepareStatement(
                """ INSERT INTO weather (serviceName, location, temperature, metcast, dateAndTime)
                    VALUES (?, ?, ?, ?, ?)""",
                java.sql.Statement.RETURN_GENERATED_KEYS        //Получение id
            )
            //Установка параметров запроса
            stmt.setString(1, weather.serviceName)
            stmt.setString(2, weather.location)
            stmt.setBigDecimal(3, BigDecimal(weather.temperature).bigDecimal)
            stmt.setString(4, weather.metcast)
            stmt.setTimestamp(5, Timestamp.valueOf(weather.dateAndTime))
            stmt.executeUpdate()

            //Получение сгенерированного id 
            val rs = stmt.getGeneratedKeys
            val result = if (rs.next()) weather.copy(id = rs.getInt(1)) else weather
            rs.close()
            stmt.close()
            result
        }
    }

    //Удаление записи по id из БД
    override def delete(id: Int): F[Unit] = Sync[F].blocking {
        db.withConnection{conn =>
            val stmt = conn.prepareStatement("DELETE FROM weather WHERE id = ?")
            stmt.setInt(1, id)
            stmt.executeUpdate()
            stmt.close()
        }
    }
}
