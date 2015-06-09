package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.Session
import models.User
import models.daos.slick.SessionsDAO
import models.MessageModels._
import play.api.libs.json.Json
import models.daos.slick.MessagesDAO
import models.daos.slick.DBAuthTableDefinitions._
import play.api.libs.json._
import play.api.libs.functional.syntax._
import scala.concurrent.Future

class MessageController @Inject() (implicit val env: Environment[User, JWTAuthenticator],
                                   messagesDAO: MessagesDAO)
  extends Silhouette[User, JWTAuthenticator] {

  val messagesPerPage = 10

  implicit val userJsonFormat = Json.format[DBUser]

  def all(sessionId: String, page: Int = 1) = SecuredAction.async {
    Future.successful(Ok(Json.toJson(messagesDAO.all(UUID.fromString(sessionId), (page - 1) * messagesPerPage, messagesPerPage ))))
  }
}

/*
  implicit val messageWithUser : Writes[(Message, DBUser)] = (
      (JSPath \ _).write[Message] and
      (JSPath \ author).write[DBUser]
    )(unlift(Message, DBUser).unapply)*/