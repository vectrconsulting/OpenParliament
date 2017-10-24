package consulting.vectr.service

import com.twitter.inject.Logging
import consulting.vectr.model.Entity

import scala.io.Source
import io.circe.generic.auto._
import io.circe.parser._

import scala.util.Random
import scala.math.{min, max}

class NLPService() extends Logging {

  private val language = "nl"

  private val questions = Source.fromURL(getClass.getResource(s"/questions/$language.json")).getLines.mkString
  private val authorsFile = Source.fromURL(getClass.getResource("/entities/author.json")).getLines.mkString
  private val departmentsFile = Source.fromURL(getClass.getResource("/entities/department.json")).getLines.mkString
  private val topicsFile = Source.fromURL(getClass.getResource("/entities/topic.json")).getLines.mkString
  private val partiesFile = Source.fromURL(getClass.getResource("/entities/party.json")).getLines.mkString

  private val rand = new Random(System.currentTimeMillis())

  private def randStr(filterType: String, languageFile: Map[String,List[String]]): String = {
    rand.shuffle(languageFile(filterType)).head
  }

  private def build(languageFile: Map[String,List[String]], filters: List[Tuple2[String, String]]): String = {
    var question = randStr("start", languageFile)
    var party = false
    var department = false
    var topic = false
    var author = false
    filters.foreach( filter => {
      val entity = filter._2
      val filterType = filter._1


      if (filterType == "party") {
        if (party)
          question += randStr("and", languageFile) + entity
        else
          question += randStr(filterType, languageFile) + entity
        party = true
      }
      if (filter._1 == "department") {
        if (department)
          question += randStr("and", languageFile) + entity
        else
          question += randStr(filterType, languageFile) + entity
        department = true
      }
      if (filter._1 == "topic") {
        if (topic)
          question += randStr("and", languageFile) + entity
        else
          question += randStr(filterType, languageFile) + entity
        topic = true
      }
      if (filter._1 == "author") {
        if (author)
          question += randStr("and", languageFile) + entity
        else
          question += randStr(filterType, languageFile) + entity
        author = true
      }
    })
    question
  }

  def buildQuestion(filters: List[Tuple2[String, String]]): String = {

    val sorted = filters.sortWith( (x:Tuple2[String, String], y:Tuple2[String, String]) => x._1 < y._1)

    var question = ""

    decode[Map[String,List[String]]](questions) match {
      case Left(error_msg) =>
        error(error_msg)
      case Right(languageFile) => {
        question = build(languageFile, sorted)
      }
    }
    question
  }

  def title(): String = {
    var title = ""

    decode[Map[String,List[String]]](questions) match {
      case Left(error_msg) =>
        error(error_msg)
      case Right(languageFile) => {
        title = randStr("title", languageFile)
      }
    }

    title
  }

  private def decodeEntityJson(fileContent: String): Entity = {
    var entity = Entity("",List())

    decode[Entity](fileContent) match {
      case Left(error_msg) =>
        error(error_msg)
      case Right(parsed) => {
        entity = parsed
      }
    }
    entity
  }


  private def findEntities(sentence: String, entity: Entity): Set[String] = {
    val lowered = sentence.toLowerCase
    var found = Set[String]()
    entity.entries.foreach( (entry) => {
      if (lowered.contains(entry.value.toLowerCase))
        found = found ++ Set(entry.value)
      entry.synonyms.foreach( (syn) =>
        if (lowered.contains(syn.toLowerCase))
          found = found ++ Set(entry.value)
      )
    })
    found
  }


  def getEntitiesFromSentence(sentence: String): Map[String, Set[String]] = {

    val authors = decodeEntityJson(authorsFile)
    val departments = decodeEntityJson(departmentsFile)
    val topics = decodeEntityJson(topicsFile)
    val parties = decodeEntityJson(partiesFile)

    val foundEntities = Map(
      "author"->findEntities(sentence, authors),
      "department"->findEntities(sentence, departments),
      "topic"->findEntities(sentence, topics),
      "party"->findEntities(sentence, parties)
    )

    foundEntities
  }

}
