package utils.di

import com.google.inject.{ AbstractModule, Provides }
import com.mohiva.play.silhouette.api.services._
import com.mohiva.play.silhouette.api.util._
import com.mohiva.play.silhouette.api.{ Environment, EventBus }
import com.mohiva.play.silhouette.impl.authenticators._
import com.mohiva.play.silhouette.impl.daos.{ CacheAuthenticatorDAO, DelegableAuthInfoDAO }
import com.mohiva.play.silhouette.impl.providers._
import com.mohiva.play.silhouette.impl.providers.oauth1._
import com.mohiva.play.silhouette.impl.providers.oauth1.secrets.{ CookieSecretProvider, CookieSecretSettings }
import com.mohiva.play.silhouette.impl.providers.oauth1.services.PlayOAuth1Service
import com.mohiva.play.silhouette.impl.providers.oauth2._
import com.mohiva.play.silhouette.impl.providers.oauth2.state.DummyStateProvider
import com.mohiva.play.silhouette.impl.services._
import com.mohiva.play.silhouette.impl.util._



import models.services.{UserService, UserServiceImpl}
import models.daos._
import models.daos.slick._
import models.User

import net.codingwell.scalaguice.ScalaModule
import play.api.Play
import play.api.Play.current

import scala.collection.immutable.ListMap

/**
 * The Guice module which wires all Silhouette dependencies.
 */
class SilhouetteModule extends AbstractModule with ScalaModule {

  /**
   * Configures the module.
   */
  def configure() {
    bind[UserService].to[UserServiceImpl]
    bind[UserDAO].to[UserDAOSlick]
    bind[DelegableAuthInfoDAO[OAuth2Info]].to[OAuth2InfoDAOSlick]

    bind[CacheLayer].to[PlayCacheLayer]
    bind[HTTPLayer].to[PlayHTTPLayer]
    bind[OAuth2StateProvider].to[DummyStateProvider]
    bind[IDGenerator].toInstance(new SecureRandomIDGenerator())
    bind[PasswordHasher].toInstance(new BCryptPasswordHasher)
    bind[FingerprintGenerator].toInstance(new DefaultFingerprintGenerator(false))
    bind[EventBus].toInstance(EventBus())
  }

  /**
   * Provides the Silhouette environment.
   *
   * @param userService The user service implementation.
   * @param authenticatorService The authentication service implementation.
   * @param eventBus The event bus instance.
   * @param credentialsProvider The credentials provider implementation.
   * @param facebookProvider The Facebook provider implementation.
   * @param googleProvider The Google provider implementation.
   * @param twitterProvider The Twitter provider implementation.
   * @return The Silhouette environment.
   */
  @Provides
  def provideEnvironment(
                          userService: UserService,
                          authenticatorService: AuthenticatorService[JWTAuthenticator],
                          eventBus: EventBus,
                          facebookProvider: FacebookProvider
  ): Environment[User, JWTAuthenticator] = {

    Environment[User, JWTAuthenticator](
      userService,
      authenticatorService,
      ListMap(
        facebookProvider.id -> facebookProvider
      ),
      eventBus
    )
  }

  /**
   * Provides the authenticator service.
   *
   * @param cacheLayer The cache layer implementation.
   * @param idGenerator The ID generator used to create the authenticator ID.
   * @return The authenticator service.
   */
  @Provides
  def provideAuthenticatorService(
                                   cacheLayer: CacheLayer,
                                   idGenerator: IDGenerator,
                                   fingerprintGenerator: FingerprintGenerator): AuthenticatorService[JWTAuthenticator] = {

    new JWTAuthenticatorService(JWTAuthenticatorSettings(
      headerName = Play.configuration.getString("silhouette.authenticator.headerName").get,
      issuerClaim = Play.configuration.getString("silhouette.authenticator.issuerClaim").get,
      encryptSubject = Play.configuration.getBoolean("silhouette.authenticator.encryptSubject").get,
      authenticatorExpiry = Play.configuration.getInt("silhouette.authenticator.authenticatorExpiry").get,
      sharedSecret = Play.configuration.getString("application.secret").get
    ), Some(new CacheAuthenticatorDAO[JWTAuthenticator](cacheLayer)), idGenerator, Clock())
  }

  /**
   * Provides the auth info service.
   *
   * @param oauth2InfoDAO The implementation of the delegable OAuth2 auth info DAO.
   * @return The auth info service instance.
   */
  @Provides
  def provideAuthInfoService(
                              oauth2InfoDAO: DelegableAuthInfoDAO[OAuth2Info]): AuthInfoService = {

    new DelegableAuthInfoService(oauth2InfoDAO)
  }

  /**
   * Provides the avatar service.
   *
   * @param httpLayer The HTTP layer implementation.
   * @return The avatar service implementation.
   */
  @Provides
  def provideAvatarService(httpLayer: HTTPLayer): AvatarService = new GravatarService(httpLayer)


  /**
   * Provides the Facebook provider.
   *
   * @param httpLayer The HTTP layer implementation.
   * @param stateProvider The OAuth2 state provider implementation.
   * @return The Facebook provider.
   */
  @Provides
  def provideFacebookProvider(httpLayer: HTTPLayer, stateProvider: OAuth2StateProvider): FacebookProvider = {
    FacebookProvider(httpLayer, stateProvider, OAuth2Settings(
      accessTokenURL = Play.configuration.getString("silhouette.facebook.accessTokenURL").get,
      redirectURL = Play.configuration.getString("silhouette.facebook.redirectURL").get,
      clientID = Play.configuration.getString("silhouette.facebook.clientID").getOrElse(""),
      clientSecret = Play.configuration.getString("silhouette.facebook.clientSecret").getOrElse(""),
      scope = Play.configuration.getString("silhouette.facebook.scope")))
  }
}

