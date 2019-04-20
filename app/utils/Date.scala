package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.libs.json.{Format, JsResult, JsValue, Json}

import scala.util.Try

object Date {
  val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  def dateToStr(date: LocalDate): String =
    date.format(formatter)


  def strToDate(str: String): Option[LocalDate] = Try{
    LocalDate.parse(str, formatter)
  }.toOption


  implicit val localDateFormat = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      json.validate[String].map(LocalDate.parse(_,  formatter))

    override def writes(o: LocalDate): JsValue = Json.toJson(o.format(formatter))
  }

}
