package Repositories

import models.WeatherModel
import cats.effect.Sync

//Интерфейс репозитория для работы в БД
trait WeatherDataRepository[F[_]]{
    def getWeatherByLocation(location: String):F[Seq[WeatherModel]]     //Получение данных о погоде по локации
    def getAllWeather():F[Seq[WeatherModel]]                           //Получение всех данных о погоде
    //Получение последних данных о погоде
    def getLatestWeather(locations: List[String], services: List[String]): F[Seq[WeatherModel]]  
    def insert(weather: WeatherModel):F[WeatherModel]                   //Вставка в БД
    def delete(id: Int): F[Unit]                                        //Удаление
}
