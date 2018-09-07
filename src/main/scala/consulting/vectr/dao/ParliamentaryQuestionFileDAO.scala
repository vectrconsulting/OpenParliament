package consulting.vectr.dao

import java.io._
import javax.inject.Inject

import ammonite.ops.{Path, ls, pwd, rm}
import com.twitter.concurrent.AsyncStream
import com.twitter.inject.Logging
import com.typesafe.config.Config
import consulting.vectr.model.{DekamerQRVAIdResponse, DekamerQRVAIdResponseItem, ParliamentaryQuestionWeb}
import io.circe.generic.auto._
import io.circe.parser._

import scala.io.Source

class ParliamentaryQuestionFileDAO @Inject()(config: Config) extends Logging {

  val directory = Path(config.getString("cachedirectory"))

  def writePQuestion(filename: String, content: String): Unit = {
    rm ! Path(s"${directory}/${filename}.json")
    val file = new File(s"${directory}/${filename}.json")
    try{
      debug(s"writing ${directory}/${filename}.json")
      file.createNewFile()
      val fw = new FileWriter(file)
      val bw = new BufferedWriter(fw)
      bw.write(content)
      bw.close()
    }catch {
      case x: Throwable =>
        error(x.getMessage)
        x.getStackTrace.foreach(ste => error(ste.toString))
        throw x
    } finally {
    }
  }

  def loadPQuestions(): AsyncStream[ParliamentaryQuestionWeb] = {
    AsyncStream.fromSeq((ls ! directory).toList)
      .filter(p => p.toString.split('.').last == "json")
      .map(p => Source.fromFile(p.toString()).getLines().mkString(""))
      .flatMap(str => AsyncStream.fromOption(decode[DekamerQRVAIdResponse](str) match {
        case Left(error_msg) => None
        case Right(qRVAIdResponse) =>
          qRVAIdResponse.items.headOption match {
            case None => None
            case Some(head) => Some(createParliamentaryQuestionWeb(head))
          }
      }))
  }

  private def createParliamentaryQuestionWeb(item: DekamerQRVAIdResponseItem): ParliamentaryQuestionWeb = ParliamentaryQuestionWeb(
    link = item.link.href.toString,
    id = item.ID,
    status = item.STATUSQ.getOrElse("unknown"),
    legislation = item.LEGISL,
    sdocname = item.link.href.split('/').takeRight(1).head,
    document_id = item.DOCNAME.getOrElse(""),
    document_date = item.DEPOTDAT,
    author = item.AUT.getOrElse("").split("\n").dropRight(1).map(_.replace(',', ' ').trim).mkString(" "),
    author_party = item.AUT.getOrElse("").split("\n").takeRight(1).map(_.trim).mkString(" "),
    language = item.LANG.getOrElse("unknown"),
    department_number = item.DEPTNUM,
    department_name_nl = item.DEPTN.getOrElse("unknown"),
    department_name_fr = item.DEPTF.getOrElse("unknown"),
    sub_department_name_nl = item.SUBDEPTN,
    sub_department_name_fr = item.SUBDEPTF,
    question_number = item.QUESTNUM,
    title_nl = item.TITN.getOrElse("unknown"),
    title_fr = item.TITF.getOrElse("unknown"),
    question_text_nl = item.TEXTQN.map(_.br.head),
    question_text_fr = item.TEXTQF.map(_.br.head),
    answer_text_nl = item.TEXTA1N.map(_.br.head),
    answer_text_fr = item.TEXTA1F.map(_.br.head),
    subject_nl = item.MAIN_THESAN,
    subject_fr = item.MAIN_THESAF,
    sub_subject_nl = item.THESAN.head.split('|').map(_.trim.toLowerCase.capitalize).toList,
    sub_subject_fr = item.THESAF.head.split('|').map(_.trim.toLowerCase.capitalize).toList
  )
}