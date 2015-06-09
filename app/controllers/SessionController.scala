package controllers

import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api.{Silhouette, Environment}
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.Session
import models.User
import models.daos.slick.SessionsDAO
import play.api.libs.json._
import play.api.mvc.BodyParsers

import scala.concurrent.Future

class SessionController @Inject() (implicit val env: Environment[User, JWTAuthenticator],
                                                sessionsDAO: SessionsDAO)
  extends Silhouette[User, JWTAuthenticator] {
        val sessionsToLoad = 10

        def get(uuid: String) = SecuredAction.async {
          Future.successful(Ok(Json.toJson(sessionsDAO.find(UUID.fromString(uuid)))))
        }

        def all(page: Option[Int]) = SecuredAction.async {
          Future.successful(Ok(Json.toJson(sessionsDAO.all((page.getOrElse(1)-1) * sessionsToLoad, sessionsToLoad))))
        }
        def allSubscribedSessions(page: Option[Int]) = SecuredAction.async { implicit request =>
          Future.successful(Ok(Json.toJson(sessionsDAO.allSubscribedByUserId(request.identity.userID))))
        }
        def search(query: String, page: Option[Int]) = SecuredAction.async {
          val sessionsToSeek = 5
          Future.successful(Ok(Json.toJson(sessionsDAO.search(query, (page.getOrElse(1)-1) * sessionsToSeek, sessionsToSeek ))))
        }

        def save() =  SecuredAction(BodyParsers.parse.json) { implicit request =>
          println("save session")
          val sessionResult = request.body.validate[Session]
          sessionResult.fold(
            e => {
              BadRequest(Json.obj("status" -> "KO", "message" -> JsError.toFlatJson(e)))
            },
            s => {
              val result = sessionsDAO.save(s.copy(authorId = Some(request.identity.userID)))
              Ok(Json.toJson(result))
            }
          )
        }

        def subscribe(id: String) = SecuredAction.async { implicit request =>
            Future.successful {
              sessionsDAO.subscribe(UUID.fromString(id), request.identity.userID)
              Ok("ok")
            }
        }
}
