package models.daos.slick

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import play.api.db.slick._
import play.api.libs.iteratee.Enumerator
import play.api.libs.ws._
import play.api
import scala.concurrent.Future
import models.daos.slick.DBAuthTableDefinitions._
import play.api.db.slick.Config.driver.simple._

import java.nio.file.{Paths, Files}
import java.io.{FileOutputStream, File}
import java.util.UUID
import scala.concurrent.ExecutionContext.Implicits.global

import play.api.libs.iteratee._
import play.api.libs.json._

import javax.inject.Inject

import com.mohiva.play.silhouette.api.{ Environment, LogoutEvent, Silhouette }
import com.mohiva.play.silhouette.impl.authenticators.JWTAuthenticator
import models.User

class OAuth2InfoDAOSlick @Inject() (implicit val env: Environment[User, JWTAuthenticator]) extends DelegableAuthInfoDAO[OAuth2Info] {

  import play.api.Play.current


  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = Future.successful(
    DB withSession { implicit session =>
      val infoId = slickLoginInfos.filter(
        x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
      ).first.id.get

      slickOAuth2Infos.filter(_.loginInfoId === infoId).firstOption match {
        case Some(info) =>
          slickOAuth2Infos update DBOAuth2Info(info.id, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId)
        case None => {
          slickOAuth2Infos insert DBOAuth2Info(None, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId)

        }
      }
      authInfo

    }
  )

  def dlProfilePicture(access_token: String): Future[File] = {
    //val avatar_url = "http://cdns2.freepik.com/photos-libre/_21253111.jpg"
    val avatar_url = "https://graph.facebook.com/v2.3/me/picture?width=9999&redirect=false&access_token="+access_token

    val futureResult: Future[WSResponse] = WS.url(avatar_url).get()

    val futureResponse : Future[(WSResponseHeaders, Enumerator[Array[Byte]])] =
      futureResult.flatMap {
        r => WS.url((r.json \ "data" \ "url").as[String]).getStream()
      }

    futureResponse.flatMap {
      case (headers, body) =>
        val newFileName: String = UUID.randomUUID().toString + ".jpg"
        val file = new File("public/uploads/"+newFileName)

        val outputStream = new FileOutputStream(file)

        // The iteratee that writes to the output stream
        val iteratee = Iteratee.foreach[Array[Byte]] { bytes =>
          outputStream.write(bytes)
        }

        // Feed the body into the iteratee
        (body |>>> iteratee).andThen {
          case result =>
            // Close the output stream whether there was an error or not
            outputStream.close()
            // Get the result or rethrow the error
            result.get
        }.map(_ => file)
    }

  }

  def find(loginInfo: LoginInfo): Future[Option[OAuth2Info]] = {
    Future.successful(
      DB withSession { implicit session =>
        slickLoginInfos.filter(info => info.providerID === loginInfo.providerID && info.providerKey === loginInfo.providerKey).firstOption match {
          case Some(info) =>
            val oAuth2Info = slickOAuth2Infos.filter(_.loginInfoId === info.id).first
            Some(OAuth2Info(oAuth2Info.accessToken, oAuth2Info.tokenType, oAuth2Info.expiresIn, oAuth2Info.refreshToken))
          case None => None
        }
      }
    )
  }
}
