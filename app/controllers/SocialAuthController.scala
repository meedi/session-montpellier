package controllers

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
import play.api.libs.json.Json
import play.api.mvc.Action

import scala.concurrent.Future

 */
class SocialAuthController @Inject() (
                                       val env: Environment[User, JWTAuthenticator],
                                       val userService: UserService,
                                       val authInfoService: AuthInfoService)
  extends Silhouette[User, JWTAuthenticator] {


  def authenticate() = Action.async { implicit request =>
    (env.providers.get("facebook") match {
      case Some(p: SocialProvider with CommonSocialProfileBuilder) =>
        p.authenticate().flatMap {
          case Left(result) => Future.successful(result)
          case Right(authInfo) => for {
            profile <- p.retrieveProfile(authInfo)
            user <- userService.save(profile)
            authInfo <- authInfoService.save(profile.loginInfo, authInfo)
            authenticator <- env.authenticatorService.create(user.loginInfo)
            token <- env.authenticatorService.init(authenticator)
          } yield {
              env.eventBus.publish(LoginEvent(user, request, request2lang))
              Ok(Json.obj("token" -> token))
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