package consulting.vectr.modules

import com.google.inject.Provides
import com.twitter.inject.TwitterModule
import javax.inject.Singleton

import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.harness.{ServerControls, TestServerBuilders}
import org.neo4j.driver.v1.Config
import org.neo4j.driver.v1.Driver

import scala.io.Source
import com.twitter.inject.Logging
import org.neo4j.graphdb.factory.GraphDatabaseSettings.pagecache_memory
import org.neo4j.dbms.DatabaseManagementSystemSettings.data_directory
import java.io.File
import org.neo4j.kernel.configuration.HttpConnector
import org.neo4j.kernel.configuration.HttpConnector.Encryption

/**
 * @author tommichiels
 */

object EmbeddedNeo4jModule extends TwitterModule with Logging {
  
  val neo4jPath: String = System.getProperty("user.home") + File.separator + "OpenData" + File.separator + "neo4j"
  val dataDir: File = new File(neo4jPath)

  val neo4jControls: ServerControls = {
    val http1 = new HttpConnector("http", Encryption.NONE)

    val control = TestServerBuilders.
      newInProcessBuilder().
      withConfig(http1.`type`, "HTTP").
      withConfig(http1.encryption, Encryption.NONE.name()).
      withConfig(http1.enabled, "true").
      withConfig(http1.address, "0.0.0.0:7474").
      withConfig(pagecache_memory, "2g").
      withConfig(data_directory.name(), dataDir.getAbsolutePath).
      newServer()

    val driver: Driver = GraphDatabase.driver(control.boltURI(),
      Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig)

    val session = driver.session()
    val index = Source.fromInputStream(this.getClass.getResourceAsStream("/Indexes.cql"))

    index.getLines().filter(line => line.contains("CREATE") || line.contains("MERGE")).foreach(session.run)
    session.close()
    index.close()
    info(s"neo4j url: ${control.httpURI()}")
    control
  }

  @Singleton @Provides
  def provideDatabase(): Driver = GraphDatabase.driver(
    neo4jControls.boltURI(),
    AuthTokens.basic("neo4j", "security"),
    Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig
  )
}
