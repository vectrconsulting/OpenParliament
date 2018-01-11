package consulting.vectr.service

import consulting.vectr.dao.ParliamentaryQuestionNeo4jDAO
import consulting.vectr.model.Entity
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

class NLPServiceFactoryTest extends FlatSpec with Matchers with MockFactory {
  behavior of "NLPService"
  it should "return a new NLPService instance" in {
    val neo4jdao: ParliamentaryQuestionNeo4jDAO = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()

    val nlpServiceFactory: NLPServiceFactory = new NLPServiceFactory(neo4jdao)

    val nlpServiceNL: NLPService = nlpServiceFactory.NLPService("nl")
    val nlpServiceFR: NLPService = nlpServiceFactory.NLPService("fr")

    nlpServiceNL.getClass.toString should equal("class consulting.vectr.service.NLPService")
    nlpServiceFR.getClass.toString should equal("class consulting.vectr.service.NLPService")
  }
}
