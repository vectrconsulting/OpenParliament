package consulting.vectr.modules

import com.google.inject.Provides
import com.twitter.inject.TwitterModule
import javax.inject.Singleton
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.harness.TestServerBuilders
import org.neo4j.driver.v1.Config
import org.neo4j.driver.v1.Driver
import scala.io.Source
import com.twitter.inject.Logging

/**
 * @author tommichiels
 */

object EmbeddedNeo4jModule extends TwitterModule with Logging {

  val neo4jControls = {
    val control = TestServerBuilders.
      newInProcessBuilder().
      newServer()
    val driver: Driver = GraphDatabase.driver(control.boltURI(),
      Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());
    val session = driver.session()
    val index = Source.fromInputStream(this.getClass.getResourceAsStream("/Indexes.cql"))
    index.getLines().filter(_.contains("CREATE")).foreach(session.run(_))
    session.close()
    index.close()
    info(s"neo4j url: ${control.httpURI()}")
    control
  }

  @Singleton @Provides
  def provideDatabase(): Driver = GraphDatabase.driver(
    neo4jControls.boltURI(),
    AuthTokens.basic("neo4j", "security"),
    Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());

}
