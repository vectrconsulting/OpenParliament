package consulting.vectr.service

import javax.inject.Inject

import com.twitter.concurrent.AsyncStream
import com.twitter.finatra.utils.FuturePools
import com.twitter.inject.Logging
import consulting.vectr.dao._
import consulting.vectr.model.{Filter, ParliamentaryQuestionSmallSummary, ParliamentaryQuestionSummary, ParliamentaryQuestionWeb}

class DataService @Inject()(neodao: ParliamentaryQuestionNeo4jDAO,
                            webdao: ParliamentaryQuestionWebDAO,
                            filedao: ParliamentaryQuestionFileDAO,
                            nlpServiceFactory: NLPServiceFactory
                           ) extends Logging {

  private val futurePool = FuturePools.unboundedPool("CallbackConverter")
  private val nlpServiceNL = nlpServiceFactory.NLPService("nl")
  private val nlpServiceFR = nlpServiceFactory.NLPService("fr")

  def dataFromFilesAndInsertInNeo4j(): Unit = {
    val sdocnames: Set[String] = neodao.allSDOCNAMES
    filedao.loadPQuestions()
      .filter(pq => !sdocnames.contains(pq.sdocname))
      .foreach {
        pq => neodao.storePQuestionWeb(pq)
      }
  }

  def dataFromWebAndInsertInNeo4j(): Unit = {
    val sdocnames: Set[String] = neodao.allSDOCNAMES
    val pq_stream: AsyncStream[String] = webdao.getAllIDsAsStream
    val pq_stream_filtered: AsyncStream[String] = if (sdocnames.nonEmpty) {
      pq_stream.filter(id => !sdocnames.contains(id))
    } else {
      pq_stream
    }

    val pq_stream_retrieved: AsyncStream[(String, ParliamentaryQuestionWeb)] =
      pq_stream_filtered.flatMap(id => AsyncStream.fromOption(webdao.getObject(id)))

    pq_stream_retrieved.foreach { pq => {
      filedao.writePQuestion(pq._2.sdocname, pq._1)
      neodao.storePQuestionWeb(pq._2)
    }
    }.onSuccess { _ => {
      nlpServiceNL.reloadResources()
      nlpServiceFR.reloadResources()
      info("Done loading pq's from web")
    }
    }
  }

  def allParliamentaryQuestions(lang: String): List[ParliamentaryQuestionSummary] = {
    neodao.allPQuestions(lang)
  }

  def AllParliamentaryPaths(lang: String): List[ParliamentaryQuestionSmallSummary] = {
    neodao.allPaths(lang)
  }

  def resolvedEntitiesAndSaveToNeo4j(query: String, lang: String): Map[String, Set[String]] = {
    val entities: Map[String, Set[String]] = lang match {
      case "nl" => nlpServiceNL.entitiesFromSentence(query)
      case "fr" => nlpServiceFR.entitiesFromSentence(query)
    }
    futurePool {
      neodao.storeFilters(entities, lang)
    }
    entities
  }

  def topQuestionsFromNeo4j(top: Int, lang: String): List[Filter] = lang match {
    case "nl" => nlpServiceNL.topQuestions(top)
    case "fr" => nlpServiceFR.topQuestions(top)
  }

  def allQuestionFromNeo4j(lang: String): List[Filter] = lang match {
    case "nl" => nlpServiceNL.allQuestions
    case "fr" => nlpServiceFR.allQuestions
  }

  def updateFilterInNeo4j(id: Int, public: Boolean): Unit = neodao.updateFilter(id, public)

  def removeFilterInNeo4j(id: Int): Unit = neodao.removeFilter(id)
}