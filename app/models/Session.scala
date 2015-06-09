package models

import java.util.UUID
import java.util.Date
import play.api.libs.json.Json


case class Session(
                 sessionID: Option[UUID],
                 title: String,
                 description: String,
                 authorId: Option[UUID],
                 creationDate: Option[Date]
)



object Session {

  implicit val sessionJsonFormat = Json.format[Session]
}


