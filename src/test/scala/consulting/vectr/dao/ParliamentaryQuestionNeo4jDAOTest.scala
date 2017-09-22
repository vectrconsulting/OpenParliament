package consulting.vectr.dao

import com.twitter.inject.Test
import org.neo4j.harness.TestServerBuilders
import org.neo4j.driver.v1.AuthTokens
import org.neo4j.driver.v1.GraphDatabase
import org.neo4j.driver.v1.Config
import org.scalatest.BeforeAndAfterAll
import consulting.vectr.model.ParliamentaryQuestion

class ParliamentaryQuestionNeo4jDAOTest extends Test with BeforeAndAfterAll {

  val neo4jControls = TestServerBuilders.newInProcessBuilder().
    //withConfig( httpConnector( "1" ).address, "0.0.0.0:7474").
    newServer()
  val driver = GraphDatabase.driver(neo4jControls.boltURI(),
    AuthTokens.basic("neo4j", "neo4j"),
    Config.build().withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig());

  override def afterAll() = neo4jControls.close()

  test("A parliamentary question needs to be stored in neo4j") {

    val repo = new ParliamentaryQuestionNeo4jDAO(driver)

    val list = List[ParliamentaryQuestion](ParliamentaryQuestion(
      "author",
      "party",
      "status",
      Some("topic"),
      Set(),
      "department",
      "questionId"))
    repo.storePQuestions(list)

    val session = driver.session()
    val result = session.run(
      """MATCH (:question {id:"questionId"})-[:ASKED]-(n:author {name:"author"})-[:IS_MEMBER_OF]-(p:party {name:"party"})
         WITH n
         MATCH (n)-[:ASKED_TO]-(d:department)
          return n.name as name,d.name as depname
        """).next()
    session.close()
    result.get("name").asString() === "author" && result.get("depname").asString() === "department"

  }

  test("A parliamentary question needs to be stored and summary retrieved in neo4j") {

    val repo = new ParliamentaryQuestionNeo4jDAO(driver)

    val list = List[ParliamentaryQuestion](ParliamentaryQuestion(
      "author",
      "party",
      "status",
      Some("topic"),
      Set(),
      "department",
      "questionId"))
    repo.storePQuestions(list)

    println(repo.getAllPQuestions().mkString("\n"))

  }
}
