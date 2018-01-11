package consulting.vectr.service

import ammonite.ops.pwd
import com.twitter.concurrent.AsyncStream
import com.typesafe.config.ConfigFactory
import consulting.vectr.dao.{ParliamentaryQuestionFileDAO, ParliamentaryQuestionNeo4jDAO, ParliamentaryQuestionWebDAO}
import consulting.vectr.model.Entity
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class ScheduledLoaderServiceTest extends FlatSpec with Matchers with MockFactory {
  behavior of "start"
  it should "start a recurring service retrieving all data from the web" in {
    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    // getDataFromWebAndInsertInNeo4j
    (neo4jdao.allSDOCNAMES _).expects().returns(Set()).noMoreThanOnce()
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List())).noMoreThanOnce()
    (webdao.getObject _).expects(*).returns(None).never()
    (neo4jdao.storePQuestionWeb _).expects(*).never()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)

    val scheduledLoaderService = new ScheduledLoaderService(dataService)
    scheduledLoaderService.running should not be true
    scheduledLoaderService.start()
    scheduledLoaderService.running shouldBe true
    scheduledLoaderService.start()
    scheduledLoaderService.running shouldBe true
  }
}
