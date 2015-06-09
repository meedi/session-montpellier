package models.daos.slick

import java.io.{FileOutputStream, File}

import models.User
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import models.daos.slick.DBAuthTableDefinitions._
import com.mohiva.play.silhouette.api.LoginInfo
import play.api.libs.iteratee.{Iteratee, Enumerator}
import play.api.libs.ws.{WSResponseHeaders, WS, WSResponse}
import scala.concurrent.Future
import java.util.UUID
import play.Logger
import models.daos.UserDAO
import scala.concurrent.ExecutionContext.Implicits.global

class UserDAOSlick extends UserDAO {

  import play.api.Play.current


  def find(loginInfo: LoginInfo) = {
    DB withSession { implicit session =>
      Future.successful {
        slickLoginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).firstOption match {
          case Some(info) =>
            slickUserLoginInfos.filter(_.loginInfoId === info.id).firstOption match {
              case Some(userLoginInfo) =>
                slickUsers.filter(_.id === userLoginInfo.userID).firstOption match {
                  case Some(user) =>
                    Some(User(user.userID, loginInfo, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL))
                  case None => None
                }
              case None => None
            }
          case None => None
        }
      }
    }
  }


  def find(userID: UUID) = {
    DB withSession { implicit session =>
      Future.successful {
        slickUsers.filter(
          _.id === userID
        ).firstOption match {
          case Some(user) =>
            slickUserLoginInfos.filter(_.userID === user.userID).firstOption match {
              case Some(info) =>
                slickLoginInfos.filter(_.id === info.loginInfoId).firstOption match {
                  case Some(loginInfo) =>
                    Some(User(user.userID, LoginInfo(loginInfo.providerID, loginInfo.providerKey), user.firstName, user.lastName, user.fullName, user.email, user.avatarURL))
                  case None => None
                }
              case None => None
            }
          case None => None
        }
      }
    }
  }

  /**
   * Saves a user.
   *
   * @param user The user to save.
   * @return The saved user.
   */
  def save(user: User) = {
    DB withSession { implicit session =>
      Future.successful {
        val dbUser = DBUser(user.userID, user.firstName, user.lastName, user.fullName, user.email, user.avatarURL)
        slickUsers.filter(_.id === dbUser.userID).firstOption match {
          case Some(userFound) => slickUsers.filter(_.id === dbUser.userID).update(dbUser)
          case None => slickUsers.insert(dbUser)
        }
        var dbLoginInfo = DBLoginInfo(None, user.loginInfo.providerID, user.loginInfo.providerKey)
        // Insert if it does not exist yet
        slickLoginInfos.filter(info => info.providerID === dbLoginInfo.providerID && info.providerKey === dbLoginInfo.providerKey).firstOption match {
          case None => slickLoginInfos.insert(dbLoginInfo)
          case Some(info) => Logger.debug("Nothing to insert since info already exists: " + info)
        }
        dbLoginInfo = slickLoginInfos.filter(info => info.providerID === dbLoginInfo.providerID && info.providerKey === dbLoginInfo.providerKey).first
        // Now make sure they are connected
        slickUserLoginInfos.filter(info => info.userID === dbUser.userID && info.loginInfoId === dbLoginInfo.id).firstOption match {
          case Some(info) =>
            // They are connected already, we could as well omit this case ;)
          case None =>
            slickUserLoginInfos += DBUserLoginInfo(dbUser.userID, dbLoginInfo.id.get)
        }
        user // We do not change the user => return it
      }
    }
  }

  def createProfilePicture(user: User, a_info: com.mohiva.play.silhouette.impl.providers.OAuth2Info) : Future[User] = {
    DB withSession { implicit session =>
        val id = user.userID
        for {
          f <- dlProfilePicture(a_info.accessToken)
         } yield  {
            slickUsers.filter(_.id === id).map(x => x.avatarURL).update(Some(f.getPath))
            user.copy(avatarURL = Some(f.getPath))
        }
    }
  }

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
}
