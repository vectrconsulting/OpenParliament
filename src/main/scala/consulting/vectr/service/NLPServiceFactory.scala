package consulting.vectr.service

import javax.inject.Inject

import com.twitter.inject.Logging
import consulting.vectr.dao.ParliamentaryQuestionNeo4jDAO

class NLPServiceFactory @Inject()(neo4jdao: ParliamentaryQuestionNeo4jDAO) extends Logging {
  def NLPService(lang: String): NLPService = {
    new NLPService(lang, neo4jdao)
  }
}
