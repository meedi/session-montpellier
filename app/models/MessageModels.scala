package models

import java.util.{Date, UUID}
import play.api.libs.functional.syntax._

import play.api.libs.json.{JsPath, Writes, Json}
import models.daos.slick.DBAuthTableDefinitions.DBUser

object MessageModels {

  case class Message(
                      messageID: UUID,
                      content: String,
                      creationDate: Date,
                      authorId: UUID,
                      sessionId: UUID
                      )

  case class MessageWithAuthor(
                                messageID: UUID,
                                content: String,
                                creationDate: Date,
                                author: DBUser
                                )

  object Message {
    implicit val messageJsonFormat = Json.format[Message]

  }

  object MessageWithAuthor {
    implicit val messageWithAuthorWrites: Writes[MessageWithAuthor] = (
        (JsPath \ "id").write[UUID] and
        (JsPath \ "content").write[String] and
        (JsPath \ "creationDate").write[Date] and
        (JsPath \ "author").write[DBUser]
      )(unlift(MessageWithAuthor.unapply))
  }

}

