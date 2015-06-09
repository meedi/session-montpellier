package models

import java.util.{UUID, Date}

import play.api.libs.json.Json

/**
 * Created by mehdi on 6/3/15.
 */
case class Subscribe(
     id: UUID,
     AuthorId: UUID,
     SessionId: UUID,
     SubscribeDate: Date
)

object Subscribe {
  implicit val subscribeJsonFormat = Json.format[Subscribe]
}