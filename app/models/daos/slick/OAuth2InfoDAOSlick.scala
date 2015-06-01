package models.daos.slick

import com.mohiva.play.silhouette.api.LoginInfo
import com.mohiva.play.silhouette.impl.daos.DelegableAuthInfoDAO
import com.mohiva.play.silhouette.impl.providers.OAuth2Info
import play.api.db.slick._
import scala.concurrent.Future
import models.daos.slick.DBAuthTableDefinitions._
import play.api.db.slick.Config.driver.simple._



class OAuth2InfoDAOSlick extends DelegableAuthInfoDAO[OAuth2Info] {

  import play.api.Play.current


  def save(loginInfo: LoginInfo, authInfo: OAuth2Info): Future[OAuth2Info] = {
    Future.successful(
      DB withSession { implicit session =>
        val infoId = slickLoginInfos.filter(
          x => x.providerID === loginInfo.providerID && x.providerKey === loginInfo.providerKey
        ).first.id.get
        slickOAuth2Infos.filter(_.loginInfoId === infoId).firstOption match {
          case Some(info) =>
            slickOAuth2Infos update DBOAuth2Info(info.id, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId)
          case None => slickOAuth2Infos insert DBOAuth2Info(None, authInfo.accessToken, authInfo.tokenType, authInfo.expiresIn, authInfo.refreshToken, infoId) 
        }
        authInfo
      }
    )
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
