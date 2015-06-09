package models.daos.slick

import javax.inject
import models.User
import models.MessageModels._

import models.daos.slick.DBTableDefinitions._
import play.api.db.slick._
import play.api.db.slick.Config.driver.simple._
import models.daos.slick.DBAuthTableDefinitions._
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import java.util.UUID
import play.Logger
import play.api.libs.json.Json
import models.daos.slick.DBAuthTableDefinitions.DBUser
import play.api.libs.json._
import play.api.libs.functional.syntax._
/**
 * Created by mehdi on 6/3/15.
 */

class MessagesDAO {
  import play.api.Play.current

  
  def all(sessionId: UUID, drop: Int, take: Int) = {
    DB.withSession { implicit s =>
      val q = for {
        m <- slickMessages if m.sessionId === sessionId
        u <- slickUsers if m.authorId === u.id
      } yield (m.id, m.content, m.creationDate, u)

      q.drop(drop).take(take).list.map(m =>
        MessageWithAuthor(m._1, m._2, m._3, m._4)
      )

    }
  }

  def find(messageId: UUID) = {
    DB.withSession { implicit s =>
      slickSessions.filter(_.id === messageId).firstOption
    }
  }

}