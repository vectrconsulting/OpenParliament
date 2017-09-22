package consulting.vectr.dao

import consulting.vectr.model.ParliamentaryQuestion
import javax.inject.Inject
import com.typesafe.config.Config
import java.io.File
import ammonite.ops.ls
import ammonite.ops.Path
import java.nio.file.Paths
import ammonite.ops.read
import scala.xml.XML
import org.parboiled.common.StringUtils
import com.twitter.inject.Logging

class ParliamentaryQuestionFileDAO @Inject() (config: Config) extends Logging {

  val directory = Path(config.getString("cachedirectory"))

  def getAllPQuestions(): List[ParliamentaryQuestion] = {
    getListOfXMLFiles().filter(x=> StringUtils.isNotEmpty((x \\ "SDOCNAME").text)).
      map { x =>
        info("loading question:" + (x \\ "SDOCNAME").text)
        val pq = ParliamentaryQuestion(
          author = getAuthor((x \\ "AUT").text),
          party = getParty((x \\ "AUT").text),
          status = (x \\ "STATUSQ").text,
          topicNL = getTopic((x \\ "MAIN_THESAN").text),
          subTopicNL = (x \\ "THESAN").text.split('|').map(_.trim().replaceAll(" ", "")).toSet,
          department = (x \\ "DEPTN").text,
          questionId = (x \\ "SDOCNAME").text)
        info(pq)
        pq
      }.filter(_.status == "answerReceived")
  }

  private def getListOfXMLFiles() = {
    (ls ! directory).toList.map(x => XML.loadFile(x.toString))
  }

  private def getAuthor(mixed: String): String = {
    mixed.replace("\n", "").split(',')(0).trim().replaceAll(" +", " ")
  }

  private def getParty(mixed: String): String = {
    mixed.replace("\n", "").split(',')(1).trim().replaceAll(" +", " ")
  }

  private def getTopic(mixed: String): Option[String] = {
    if (StringUtils.isEmpty(mixed)) Some(mixed) else None
  }
}