package models.daos.slick

import javax.inject
import models.Session
import models.Subscribe
import models.daos.slick.DBTableDefinitions._
import play.api.db.slick._
import utils.MyPostgresDriver.simple._
import models.daos.slick.DBAuthTableDefinitions._
import com.mohiva.play.silhouette.api.LoginInfo
import scala.concurrent.Future
import java.util.{UUID, Date}
import play.Logger
import com.github.tminglei.slickpg.TsQuery


class SessionsDAO {
  import play.api.Play.current

  val plainToTsQuery= SimpleFunction.unary[String, TsQuery]("plainto_tsquery")

  def all(drop: Int, take: Int) = {
    DB.withSession { implicit s =>
      slickSessions.drop(drop).take(take).list
    }
  }

  def allSubscribedByUserId(uuid: UUID) = {
    DB.withSession { implicit s =>
      val q = for{
          sub <- slickSubscribes if sub.userId === uuid
          s <- slickSessions if sub.sessionId === sub.sessionId
        } yield (s.id, s.title, s.description, s.authorId, s.creationDate)

      q.list.map(x => Session(Some(x._1), x._2, x._3, Some(x._4), Some(x._5)))
    }
  }

  def subscribe(userId: UUID, sessionId: UUID) = {
    DB.withSession { implicit s =>
      slickSubscribes += Subscribe(UUID.randomUUID(),sessionId,userId, new Date())
    }
  }
  def find(sessionId: UUID) = {
    DB.withSession { implicit s =>
      slickSessions.filter(_.id === sessionId).firstOption
    }
  }

  def search(query: String, drop: Int, take: Int) = {
    DB.withSession { implicit s =>
      val formatted_query = query.split("\\W+").map(x=>x.trim + ":*").mkString(" & ")
      println(formatted_query)
      val q1 = slickSessions
      .filter(x => {
        (toTsVector(x.title, Some("french")) @+ toTsVector(x.description, Some("french"))) @@ toTsQuery(formatted_query, Some("French")) /*toTsQuery(query.bind, Some("french")*/
      })
      .map(r => (r.id, r.title, r.description, r.authorId, r.creationDate, tsRank(toTsVector(r.title),toTsQuery(formatted_query, Some("French")))))/*toTsQuery(query.bind, Some("french"))*/
      .sortBy(_._6)
      .drop(drop)
      .take(take)
      q1.list
      .map(s => Session(Some(s._1), s._2, s._3, Some(s._4), Some(s._5)))

    }
  }

  def save(session: Session) = {
      DB.withSession { implicit s =>
        session.sessionID match {
          case Some(uuid) => {
            println(uuid)
            session
          }
          case None => {
            val ses = session.copy(sessionID = Some(UUID.randomUUID()), creationDate = Some(new Date()))
            slickSessions.insert(ses)
            ses
          }
        }
      }
  }

  //def findWithMessages(sessionId: UUID)
}