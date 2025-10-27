package Services

import models.WeatherModel
import cats.effect.kernel.Async

//Интерфейс сервиса 
trait WeatherDataService[F[_]]{
    def getWeatherByLocation(location: String):F[Seq[WeatherModel]]
    def getAllWeather():F[Seq[WeatherModel]]
    def getLatestWeather(): F[Seq[WeatherModel]]
    def insert(weather: WeatherModel):F[WeatherModel]
    def delete(id: Int): F[Unit]
    def dataCollection(): F[Unit]
}

