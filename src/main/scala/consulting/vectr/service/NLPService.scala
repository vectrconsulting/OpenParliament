package consulting.vectr.service

import com.twitter.inject.Logging
import consulting.vectr.model.{Entity, Filter, FilterEntity}
import javax.inject.Inject

import scala.io.Source
import io.circe.parser._

import scala.util.Random
import java.text.Collator

import consulting.vectr.dao.ParliamentaryQuestionNeo4jDAO

class NLPService(language: String, neo4jDAO: ParliamentaryQuestionNeo4jDAO) extends Logging {
  private var authors = neo4jDAO.authors
  private var parties = neo4jDAO.parties
  private var departments = neo4jDAO.departments(language)
  private var topics = neo4jDAO.topics(language)

  private val questions = Source.fromURL(getClass.getResource(s"/questions/$language.json")).getLines.mkString

  private val rand = new Random(System.currentTimeMillis())

  private[service] def randStr(filterType: String, languageFile: Map[String, List[String]]): String = {
    rand.shuffle(languageFile(filterType)).head
  }

  private[service] def build(languageFile: Map[String, List[String]], filters: List[FilterEntity]): String = {
    var question = randStr("start", languageFile)
    var party = false
    var department = false
    var topic = false
    var author = false
    randStr("start", languageFile) ++ filters.map { filter => {
      filter.`type` match {
        case "party" =>
          if (party) randStr("and", languageFile) + filter.value
          else {
            party = true
            randStr(filter.`type`, languageFile) + filter.value
          }
        case "department" =>
          if (department) randStr("and", languageFile) + filter.value
          else {
            department = true
            randStr(filter.`type`, languageFile) + filter.value
          }
        case "topic" =>
          if (topic) randStr("and", languageFile) + filter.value
          else {
            topic = true
            randStr(filter.`type`, languageFile) + filter.value
          }
        case "author" =>
          if (author) randStr("and", languageFile) + filter.value
          else {
            author = true
            randStr(filter.`type`, languageFile) + filter.value
          }
      }
    }
    }.mkString("")
  }

  def buildQuestion(filters: List[FilterEntity]): String =
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) =>
        error(error_msg)
        ""
      case Right(languageFile) => build(languageFile, filters.sortWith(_.`type` < _.`type`))
    }

  private[service] def containsSubstrIgnoreAccents(sentence: String, value: String): Boolean = {
    val instance: Collator = Collator.getInstance()
    instance.setStrength(Collator.NO_DECOMPOSITION)
    if (sentence.length >= value.length) {
      val sentence_split = sentence.split("")
        .iterator.sliding(value.length)
        .map(_.mkString(""))
      sentence_split.map((substr) => instance.compare(value.replace("-", "_"), substr.replace("-", "_"))).contains(0)
    }
    else
      false
  }

  private[service] def findEntities(sentence: String, entity: Entity): Set[String] =
    entity.entries.filter(_.synonyms.nonEmpty).flatMap { entry =>
      val contains_entity: Boolean = entry.synonyms.map {
        synonym => containsSubstrIgnoreAccents(sentence.toLowerCase, synonym.toLowerCase)
      }.foldLeft(false)(_ || _)
      if (contains_entity) Some(entry.value) else None
    }.toSet

  def entitiesFromSentence(sentence: String): Map[String, Set[String]] = Map(
    "author" -> findEntities(sentence, authors),
    "department" -> findEntities(sentence, departments),
    "topic" -> findEntities(sentence, topics),
    "party" -> findEntities(sentence, parties)
  )

  def topQuestions(top: Int): List[Filter] = {
    val top_filters: List[Filter] = neo4jDAO.topFilters(top, language)
    top_filters.map(filter => Filter(0, filter.public, filter.count, buildQuestion(filter.entities), filter.entities))
  }

  def allQuestions: List[Filter] = {
    val filters: List[Filter] = neo4jDAO.allFilters(language)
    filters.map(filter => Filter(filter.id, filter.public, filter.count, buildQuestion(filter.entities), filter.entities))
  }

  def reloadResources(): Unit = {
    authors = neo4jDAO.authors
    parties = neo4jDAO.parties
    departments = neo4jDAO.departments(language)
    topics = neo4jDAO.topics(language)
  }

}

