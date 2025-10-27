package models

import play.api.libs.json._
import java.time.LocalDateTime

//Модель данных о погоде
case class WeatherModel(
    id: Int,               
    serviceName: String,
    location: String,
    temperature: Double,
    metcast: String,
    dateAndTime: LocalDateTime    
)

//Сериализация и десериализация json 
object WeatherModel {
  implicit val format: OFormat[WeatherModel] = Json.format[WeatherModel]
}
