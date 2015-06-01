package models.services

import models.User
import com.mohiva.play.silhouette.api.services.{ AuthInfo, IdentityService }
import com.mohiva.play.silhouette.impl.providers.CommonSocialProfile
import models.User

import scala.concurrent.Future


trait UserService extends IdentityService[User] {


  def save(user: User): Future[User]

  def save[A <: AuthInfo](profile: CommonSocialProfile): Future[User]
}