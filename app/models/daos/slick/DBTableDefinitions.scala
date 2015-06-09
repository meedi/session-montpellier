package models.daos.slick

import utils.MyPostgresDriver.simple._
import models.{Session, Subscribe}
import models.MessageModels._
import java.util.{UUID, Date}
import java.sql.Timestamp

import DBAuthTableDefinitions._

object DBTableDefinitions {
  implicit  val JavaUtilDateMapper =
    MappedColumnType.base[Date, Timestamp] (
      d => new Timestamp(d.getTime),
        d => new Date(d.getTime)
    )

  class Sessions(tag: Tag) extends Table[Session](tag, "session") {
    def id = column[UUID]("sessionId", O.PrimaryKey)
    def title = column[String]("title")
    def creationDate = column[Date]("creation_date")
    def description = column[String]("description")
    def authorId = column[UUID]("author_id")
    def author = foreignKey("AUTHOR_FK", authorId, slickUsers)(_.id)

    def * = (id.?, title, description, authorId.?, creationDate.?) <> ((Session.apply _).tupled, Session.unapply)
  }

  class Messages(tag: Tag) extends Table[Message](tag, "message") {
    def id = column[UUID]("messageId", O.PrimaryKey)
    def content = column[String]("content")
    def authorId = column[UUID]("author_id")
    def sessionId = column[UUID]("session_id")
    def creationDate = column[Date]("creation_date")

    def author = foreignKey("AUTHOR_MESSAGE_FK", authorId, slickUsers)(_.id)
    def session = foreignKey("SESSION_MESSAGE_FK", sessionId, slickSessions)(_.id)

    def * = (id, content, creationDate, authorId, sessionId) <> ((Message.apply _).tupled, Message.unapply)
  }


  class Subscribes(tag: Tag) extends Table[Subscribe](tag, "subscribe") {
    def id = column[UUID]("id", O.PrimaryKey)
    def userId = column[UUID]("user_id")
    def sessionId = column[UUID]("session_id")
    def subscribeDate = column[Date]("creation_date")

    def user = foreignKey("SUBSCRIBE_USER_FK", userId, slickUsers)(_.id)
    def session = foreignKey("SUBSCRIBE_SESSION_FK", sessionId, slickSessions)(_.id)

    def * = (id, userId, sessionId, subscribeDate) <> ((Subscribe.apply _).tupled, Subscribe.unapply)
  }

  val slickSessions = TableQuery[Sessions]
  val slickMessages = TableQuery[Messages]
  val slickSubscribes = TableQuery[Subscribes]
}
