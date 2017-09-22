package consulting.vectr.modules

import javax.inject.Singleton

import com.google.inject.Provides
import com.twitter.inject.{ Logging, TwitterModule }
import com.typesafe.config.{ Config, ConfigFactory }

/**
 * @author tommichiels
 */

object TypesafeConfigModule extends TwitterModule with Logging {

  val mode = flag("mode", "dev", "application run mode [dev:default, alpha, sandbox, beta, real]")

  val configPath = ""

  private lazy val config = {
    logger info s"LOADING CONFIG FROM: ${mode()}"
    ConfigFactory load ()
  }

  @Provides @Singleton
  def provideConfig(): Config = config
}
