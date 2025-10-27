package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import models._
import Services._
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import play.api.libs.json._

@Singleton
class WeatherController @Inject()(val controllerComponents: ControllerComponents, weatherService: WeatherDataService[Future])(implicit ec: ExecutionContext) extends BaseController {


  weatherService.dataCollection()

  //Главная страница
  def index() = Action { implicit request: Request[AnyContent] =>
    Ok(views.html.index())
  }

  //Последние данные о погоде для всех городов
  def latestCitiesWeather() = Action.async { implicit request =>
    weatherService.getLatestWeather().map { data =>
      if (data.isEmpty) {
        Redirect(routes.WeatherController.index())
      } 
      else {
        Ok(views.html.latestWeather(data))
      }
    }
  }

  //Получение погоды по локации
  def getWeatherByLocation(location: String) = Action.async{ 
    weatherService.getWeatherByLocation(location).map{data =>
      if (data.isEmpty) NotFound(Json.obj("error" -> s"Данные о погоде для местоположения не найдены: $location"))
      else Ok(Json.toJson(data))
    }
  }

  //Получение всех данных о погоде
  def getAllWeather() = Action.async {
    weatherService.getAllWeather().map { data =>
      Ok(Json.toJson(data))
    }
  }

  //Добавление данных 
  def insert(): Action[JsValue] = Action(parse.json).async{request =>
    request.body.validate[WeatherModel].fold(
      errors => {
        Future.successful(BadRequest(Json.obj("error" -> JsError.toJson(errors))))
      },
      weatherData => {
        weatherService.insert(weatherData).map{ saved =>
          Created(Json.toJson(saved))
        }
      } 
    )
  }

  //Удаление по id
  def delete(id: Int): Action[AnyContent] = Action.async{
    weatherService.delete(id).map{_=>
      NoContent
    }
  }

  //Получение данных о погоде из всех источников
  // def aggregateWeather(location: String) = Action.async {
  //   weatherService.fetchFromAllServices(location).map { data =>
  //     if (data.isEmpty) {
  //       NotFound(s"Не удалось получить данные о погоде для $location")
  //     } 
  //     else {
  //       Ok(views.html.aggregatedWeather(location, data))
  //     }
  //   }.recover {
  //     case e: Exception => 
  //       InternalServerError(s"Ошибка при получении данных о погоде: ${e.getMessage}")
  //   }
  // }
  
  // Обработка запроса с параметрами формы
  // def aggregateWeatherFromForm() = Action.async { implicit request =>
  //   val locationOpt = request.getQueryString("location")
  //   locationOpt match {
  //     case Some(location) if location.trim.nonEmpty => 
  //       // Перенаправление на URL с параметром пути
  //       Future.successful(Redirect(routes.WeatherController.aggregateWeather(location)))
  //     case _ => 
  //       Future.successful(BadRequest("Название города не указано"))
  //   }
  // }
  
  // def aggregateWeatherApi(location: String): Action[AnyContent] = Action.async {
  //   weatherService.fetchFromAllServices(location).map { data =>
  //     if (data.isEmpty) {
  //       NotFound(Json.obj("error" -> s"Не удалось получить данные о погоде для $location"))
  //     } else {
  //       Ok(Json.toJson(data))
  //     }
  //   }.recover {
  //     case e: Exception => 
  //       InternalServerError(Json.obj("error" -> s"Ошибка при получении данных о погоде: ${e.getMessage}"))
  //   }
  // }
}
