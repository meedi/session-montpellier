package controllers

import java.io.{FileOutputStream, File}
import java.util.UUID
import javax.inject.Inject

import com.mohiva.play.silhouette.api._
import com.mohiva.play.silhouette.api.exceptions.ProviderException
import com.mohiva.play.silhouette.api.services.AuthInfoService
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import com.mohiva.play.silhouette.impl.providers._
import models.User
import models.services.UserService
import play.api.i18n.Messages
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.json.Json
import play.api.libs.ws.{WSResponseHeaders, WS, WSResponse}
import play.api.mvc.Action
import models.daos.slick.UserDAOSlick
import com.mohiva.play.silhouette.impl.providers.OAuth2Provider
import scala.concurrent.Future
import scala.util.Success

class SocialAuthController @Inject() (
                                       val env: Environment[User, JWTAuthenticator],
                                       val userService: UserService,
                                       val authInfoService: AuthInfoService,
                                       val usersDAO: UserDAOSlick
                                       )
  extends Silhouette[User, JWTAuthenticator] {


  def authenticate() = Action.async { implicit request =>
    (env.providers.get("facebook") match {
      case Some(p: OAuth2Provider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoService.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(user.loginInfo)
            token <- env.authenticatorService.init(authenticator)
            user_updated <- usersDAO.createProfilePicture(user, authInfo)
          } yield {
                  env.eventBus.publish(LoginEvent(user, request, request2lang))
                  Ok(Json.obj("token" -> token, "user" -> user_updated))
              }
        }
      case _ => Future.failed(new ProviderException(s"Cannot authenticate with facebook"))
    }).recover {
      case e: ProviderException =>
        logger.error("Unexpected provider error", e)
        Unauthorized(Json.obj("message" -> Messages("could.not.authenticate")))
    }
  }
}