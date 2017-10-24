package consulting.vectr.dao

import java.net.SocketTimeoutException
import javax.inject.Inject

import com.twitter.concurrent.AsyncStream
import com.twitter.inject.Logging
import consulting.vectr.model.{DekamerQRVAIdResponse, DekamerQRVAIdResponseItem, DekamerQRVAResponse, ParliamentaryQuestionWeb}
import consulting.vectr.service.HttpService

import scalaj.http.HttpResponse
import io.circe.generic.auto._
import io.circe.parser._


class ParliamentaryQuestionWebDAO @Inject()(http: HttpService) extends Logging {
  private val url = "http://data.dekamer.be/v0/qrva"
  private val legislation = 54

  private[dao] def getOffsets: Range = {
    val response: HttpResponse[String] =
      http.url(url, params = Map("leg" -> this.legislation.toString))
    if (response.code != 200) {
      0 until 0 by 1
    } else {
      decode[DekamerQRVAResponse](response.body) match {
        case Left(_) =>
          0 until 0 by 1
        case Right(qRVAResponse) =>
          qRVAResponse.start until qRVAResponse.total by qRVAResponse.items.length
      }
    }
  }

  private[dao] def getIDs(offset: Int): List[String] = {
    try {
      val response: HttpResponse[String] =
        http.url(url = url, params = Map("leg" -> this.legislation.toString, "start" -> offset.toString))
      if (response.code != 200) {
        return List()
      }
      decode[DekamerQRVAResponse](response.body) match {
        case Left(error_message) =>
          List()
        case Right(qRVAResponse) =>
          qRVAResponse.items.map(item => item.SDOCNAME)
      }
    }

    catch {
      case nwerror: SocketTimeoutException =>
        List()
      case othererror: Throwable =>
        throw othererror
    }
  }

  def getAllIDsAsStream: AsyncStream[String] =
    AsyncStream.fromSeq(getOffsets).flatMap {
      offset => AsyncStream.fromSeq(getIDs(offset))
    }

  def getObject(sdocname: String): Option[(String, ParliamentaryQuestionWeb)] = {
    try {
      val response: HttpResponse[String] =
        http.url("http://data.dekamer.be/v0/qrva/" + sdocname)
      if (response.code != 200) {
          None
      } else {
        decode[DekamerQRVAIdResponse](response.body) match {
          case Left(error_msg) =>
            None
          case Right(qRVAIdResponse) =>
            qRVAIdResponse.items.headOption match {
              case None =>
                None
              case Some(head) =>
                Some((response.body, createParliamentaryQuestionWeb(head)))
            }
        }
      }
    } catch {
      case nwerror: SocketTimeoutException =>
        None
      case othererror: Throwable =>
        throw othererror
    }
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
