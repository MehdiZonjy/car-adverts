package utils

import java.time.LocalDate
import java.time.format.DateTimeFormatter

import play.api.libs.json.{Format, JsResult, JsValue, Json}

object Date {
  val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

  def dateToStr(date: LocalDate): String =
    date.format(formatter)


  def strToDate(str: String): LocalDate =
    LocalDate.parse(str, formatter)


  implicit val localDateFormat = new Format[LocalDate] {
    override def reads(json: JsValue): JsResult[LocalDate] =
      json.validate[String].map(LocalDate.parse(_,  formatter))

    override def writes(o: LocalDate): JsValue = Json.toJson(o.format(formatter))
  }

}
