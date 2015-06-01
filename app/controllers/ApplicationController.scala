package controllers

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.User
import play.api.libs.json.Json

import scala.concurrent.Future


class ApplicationController @Inject() (implicit val env: Environment[User, JWTAuthenticator])
  extends Silhouette[User, JWTAuthenticator] {


  def user = SecuredAction.async { implicit request =>
    Future.successful(Ok(Json.toJson(request.identity)))
  }

  def signOut = SecuredAction.async { implicit request =>
    env.eventBus.publish(LogoutEvent(request.identity, request, request2lang))
    request.authenticator.discard(Future.successful(Ok))
  }


  def view(template: String) = UserAwareAction { implicit request =>

    template match {
      case "home" => Ok(views.html.home.render())
      case "signin" => Ok(views.html.signin.render())
      case "layout" => Ok(views.html.layout.render())
      case _ => NotFound
    }

  }
}
