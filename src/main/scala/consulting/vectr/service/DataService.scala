package consulting.vectr.service

import javax.inject.Inject

import com.twitter.concurrent.AsyncStream
import com.twitter.finatra.utils.FuturePools
import com.twitter.inject.Logging
import consulting.vectr.dao._
import consulting.vectr.model.{ParliamentaryQuestionSummary, WitAIEntity, WitAIResponse}

import io.circe.syntax._

class DataService @Inject()(neodao: ParliamentaryQuestionNeo4jDAO,
                            webdao: ParliamentaryQuestionWebDAO,
                            filedao: ParliamentaryQuestionFileDAO,
                            nlpservice: NLPService
                           ) extends Logging {

  private val futurePool = FuturePools.unboundedPool("CallbackConverter")

  def getDataFromFilesAndInsertInNeo4j(): Unit = {
    filedao.loadPQuestions()
      .filter(pq => !neodao.hasSDOCNAME(pq.sdocname))
      .foreach {
        pq => neodao.storePQuestionWeb(pq)
      }
  }

  def getDataFromWebAndInsertInNeo4j(): Unit = {
    val sdocnames = neodao.getAllSDOCNAMES
    val pq_stream = webdao.getAllIDsAsStream
      .filter(id => !sdocnames.contains(id))
      .flatMap(id => AsyncStream.fromOption(webdao.getObject(id)))

    pq_stream.foreach { pq => {
      filedao.writePQuestion(pq._2.sdocname, pq._1)
      neodao.storePQuestionWeb(pq._2)
    }
    }.onSuccess { _ => info("Done loading pq's from web") }
  }

  def getAllParliamentaryQuestions(lang: String = "nl"): List[ParliamentaryQuestionSummary] = {
    neodao.getAllPQuestions(lang)
  }

  def getResolvedEntitiesAndSaveToNeo4j(query: String): String = {
    val entities = nlpservice.getEntitiesFromSentence(query)

    futurePool {
      neodao.storeFilters(entities)
    }

    entities.asJson.toString
  }

  def getTopQuestionsFromNeo4j(top: Int, lang: String): String = {
    val filters = neodao.getTopFilters(top)
    val title = nlpservice.title

    var json =
      s"""
          {
            "sidebar_title" : "$title",
            "lang": "$lang",
            "questions" : [
      """
    filters.foreach(filter => {
      val count = filter._1
      val entities = filter._2
      val question = nlpservice.buildQuestion(entities)

      var entities_json = "["

      entities.foreach((entity) => {
        val e_type = entity._1
        val e_value = entity._2
        entities_json +=
          s"""
            {
              "type": "$e_type",
              "value": "$e_value"
            },"""
      })
      entities_json = entities_json.dropRight(1) + "]"
      json +=
        s"""
           {
              "count": $count,
              "question": "$question",
              "entities": $entities_json
           },"""
    })

    json.dropRight(1) + "\n]}"
  }
}