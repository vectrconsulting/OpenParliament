package consulting.vectr.dao

import java.net.SocketTimeoutException

import com.twitter.concurrent.AsyncStream
import com.twitter.util.Await
import consulting.vectr.model.{DekamerQRVAIdResponseItem, DekamerQRVAIdResponseItemText, DekamerQRVAResponseItemsLink, ParliamentaryQuestionWeb}
import consulting.vectr.service.HttpService
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source
import scalaj.http.HttpResponse


class ParliamentaryQuestionWebDAOTest extends FlatSpec with Matchers with MockFactory {
  val qrva: String = Source.fromInputStream(getClass.getResourceAsStream("/qrva.json")).mkString
  val qrva_item: String = Source.fromInputStream(getClass.getResourceAsStream("/qrva_item.json")).mkString
  val qrva_item_empty: String = Source.fromInputStream(getClass.getResourceAsStream("/qrva_item_empty.json")).mkString
  val qrva_item_parsed = DekamerQRVAIdResponseItem(
    link = new DekamerQRVAResponseItemsLink(
      href = "http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004",
      rel = "self"),
    ID = 146303,
    STATUSQ = Some("answerReceived"),
    LEGISL = 54,
    DOCNAME = Some("0000201400004"),
    DEPOTDAT = Some("20140718"),
    AUT = Some("Nahima\n      Lanjri,\n      CD&V"),
    LANG = Some("N"),
    DEPTPRES = 3,
    DEPTNUM = 822,
    SUBDEPTN = Some("Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken"),
    SUBDEPTF = Some("Affaires étrangères, Commerce extérieur et Affaires européennes"),
    DEPTN = Some("Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken"),
    DEPTF = Some("Vice-premier ministre et ministre des Affaires étrangères, du Commerce extérieur et des Affaires européennes"),
    QUESTNUM = 1,
    DELAIDAT = Some("20190527"),
    TITN = Some("Onderzoek naar schijnhuwelijken en schijn-wettelijke samenwoonst. - Ambassades. - Instructies."),
    TITF = Some("Enquête relative aux mariages de complaisance et aux cohabitations légales de complaisance. - Ambassades. - Instructions."),
    STATUS_OL = None,
    TEXTQN = Some(new DekamerQRVAIdResponseItemText(
      br = List(
        "In het kader van de strijd tegen de schijnhuwelijken en de schijn-wettelijke samenwoonst wordt ook door onze ambassades in het buitenland een onderzoek gevoerd naar mogelijk misbruik van de migratiewet door huwelijksmigratie.In vele landen moeten mensen echter een zeer lange afstand afleggen om de Belgische ambassade te bezoeken in het kader van dit onderzoek. Problematisch daarbij is vooral dat zij niet of nauwelijks ingelicht worden over de nodige documenten of de inhoud van het interview dat zal plaatshebben. Het komt dan ook vaak voor dat de mensen onvoldoende documenten of bewijsmiddelen mee hebben en hun reis dus opnieuw moeten ondernemen om nieuwe bewijsstukken aan te leveren.1. Welke instructies worden aan de ambassades gegeven in het kader van het onderzoek naar schijnhuwelijken en schijn-wettelijke samenwoonst?2. Zijn de ambassades verplicht de mensen op voorhand in te lichten over welke documenten en bewijsmiddelen zij dienen te presenteren in het kader van hun interview?3. Indien niet, overweegt u hierover duidelijke richtlijnen op te stellen zodat de betrokken personen niet onverricht ter zake moeten terugkeren bij gebrek aan onvoldoende bewijsmiddelen of documenten?"
      ))),
    STATUS_SL = None,
    TEXTQF = Some(new DekamerQRVAIdResponseItemText(
      br = List(
        "Dans le cadre de la lutte contre les mariages de complaisance et les cohabitations légales de complaisance, nos ambassades à l'étranger mènent également l'enquête sur les abus éventuels commis dans le cadre de la loi sur la migration au travers de la migration matrimoniale.Dans de nombreux pays, les personnes concernées par l'enquête doivent toutefois parcourir de très longues distances pour se rendre à l'ambassade belge. Le problème réside surtout dans le fait qu'elles ne sont pas ou qu'elles sont mal informées sur les documents dont elles doivent disposer ou du contenu de la future interview. Il arrive dès lors souvent que les personnes ne disposent pas de tous les documents ou preuves nécessaires et doivent ainsi refaire le voyage pour apporter les pièces justificatives manquantes.1. Quelles instructions sont communiquées aux ambassades dans le cadre de l'enquête relative aux mariages de complaisance et aux cohabitations légales de complaisance?2. Les ambassades sont-elles tenues d'informer préalablement les personnes sur les documents et les preuves à présenter dans le cadre de leur interview?3. Dans la négative, envisagez-vous de rédiger des directives claires à ce sujet de sorte que les personnes concernées ne rentrent pas bredouilles de leur visite à l'ambassade pour cause de preuves ou de documents manquants?"
      ))),
    PUBLICA1 = Some("B001,\n      ,\n      10/10/2014,\n      00002014"),
    STATUSA1 = Some("publicated"),
    NUMA1 = 1,
    CASA1 = Some("1\n      réponse normale -\n      normaal antwoord"),
    TEXTA1F = Some(new DekamerQRVAIdResponseItemText(
      br = List(
        "J'attire votre attention sur le fait qu'un déplacement vers le poste consulaire n'est pas toujours nécessaire. La demande d'un certificat de non-empêchement à mariage peut se faire par écrit. Sauf dans les cas où des éléments peuvent faire croire à  l'éventualité d'un mariage blanc, l'attestation est envoyée au demandeur.Si lors de la demande, des documents manquent, le poste demande de les envoyer.Si une interview au poste consulaire s'impose, un rendez-vous est fixé  - dans la plupart des cas par téléphone. À cette occasion le demandeur est informé des documents qu'il devra présenter.Un déplacement inutile vers le consulat est donc pratiquement exclu."
      ))),
    TEXTA1N = Some(new DekamerQRVAIdResponseItemText(
      br = List(
        "Ik wil er eerst op wijzen dat een bezoek aan de consulaire post niet altijd noodzakelijk is. De aanvraag voor een attest geen huwelijksbeletsel kan schriftelijk gebeuren. Tenzij er elementen zouden zijn die kunnen wijzen op een mogelijk schijnhuwelijk, wordt het attest naar de aanvrager opgezonden. Indien bij de aanvraag niet alle nodige bewijsstukken aanwezig waren, wordt gevraagd die alsnog op te zenden.Als een interview op de consulaire post nodig blijkt, wordt - meestal telefonisch - een afspraak geregeld; bij deze gelegenheid wordt aan de betrokkenen meegedeeld welke documenten ze moeten meebrengen.Een nutteloze verplaatsing naar het consulaat is dus zo goed als uitgesloten."
      ))),
    PUBLICA2 = None,
    STATUSA2 = None,
    NUMA2 = 0,
    CASA2 = None,
    TEXTA2F = None,
    TEXTA2N = None,
    PUBLICA3 = None,
    STATUSA3 = None,
    NUMA3 = 0,
    CASA3 = None,
    TEXTA3F = None,
    TEXTA3N = None,
    PUBLICA4 = None,
    STATUSA4 = None,
    NUMA4 = 0,
    CASA4 = None,
    TEXTA4F = None,
    TEXTA4N = None,
    MAIN_THESAF = Some(" POLITIQUE EXTERIEURE "),
    MAIN_THESAN = Some(" BUITENLANDS BELEID "),
    MAIN_THESAD = None,
    THESAF = List(" UNION LIBRE  |  POLITIQUE EXTERIEURE  |  ADMISSION DES ETRANGERS  |  FORMALITE ADMINISTRATIVE  |  AMBASSADE  |  MARIAGE DE COMPLAISANCE "),
    THESAN = List(" ONGEHUWD SAMENLEVEN  |  BUITENLANDS BELEID  |  TOELATING VAN VREEMDELINGEN  |  ADMINISTRATIEVE FORMALITEIT  |  AMBASSADE  |  SCHIJNHUWELIJK "),
    THESAD = List(""),
    DESCF = None,
    DESCN = None,
    DESCD = None,
    FREEF = None,
    FREEN = None,
    FREED = None
  )

  behavior of "getOffsets"

  it should "return a list like [0..X] with equal steps, this test uses the opendata api" in {
    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(new HttpService)

    val offsets: scala.Range = parliamentaryQuestionWebDAO.getOffsets
    offsets should not be empty

    val distance: Int = offsets.tail.head - offsets.head
    offsets.exists(offset => offset % distance != 0) shouldBe false
  }

  it should "return a list like [0]" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54"), *)
      .returns(HttpResponse(body = qrva, code = 200, headers = Map()))
        .once

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val offsets: scala.Range = parliamentaryQuestionWebDAO.getOffsets

    offsets should not be empty
    offsets.length shouldBe 1
    offsets.head shouldBe 0
  }

  it should "return a empty list when json object is unparseable" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54"), *)
      .returns(HttpResponse(body = "", code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val offsets: scala.Range = parliamentaryQuestionWebDAO.getOffsets

    offsets shouldBe empty
  }

  it should "return an empty list when the http request failed " in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54"), *)
      .returns(HttpResponse(body = "", code = 404, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val offsets: scala.Range = parliamentaryQuestionWebDAO.getOffsets

    offsets shouldBe empty
  }

  behavior of "getIDs"

  it should "return a list of ids with length 10" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .returns(HttpResponse(body = qrva, code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val ids: List[String] = parliamentaryQuestionWebDAO.getIDs(0)

    ids should not be empty
    ids.length shouldBe 10
    ids.head shouldBe "54-B116-18-0496-2016201716126"
    ids.takeRight(1).head shouldBe "54-B116-18-0482-2016201715468"
  }

  it should "return an empty list if the http request fails" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .returns(HttpResponse(body = "", code = 404, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val ids: List[String] = parliamentaryQuestionWebDAO.getIDs(0)

    ids shouldBe empty
  }

  it should "return an empty list if the json object fails to parse" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .returns(HttpResponse(body = "", code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val ids: List[String] = parliamentaryQuestionWebDAO.getIDs(0)

    ids shouldBe empty
  }

  it should "return an empty list if it encounters a SocketTimeoutException" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .throws(new SocketTimeoutException())

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val ids: List[String] = parliamentaryQuestionWebDAO.getIDs(0)

    ids shouldBe empty
  }

  it should "throw an error if there it encounters an error other than SocketTimeoutException" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .throws(new Throwable())

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)

    assertThrows[Throwable] {
      parliamentaryQuestionWebDAO.getIDs(0)
    }
  }

  behavior of "getAllIDsAsStream"

  it should "return a non-empty AsyncStream" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54"), *)
      .returns(new HttpResponse(body = qrva, code = 200, headers = Map()))
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .returns(new HttpResponse(body = qrva, code = 200, headers = Map()))
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "10"), *)
      .returns(new HttpResponse(body = "", code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val stream: AsyncStream[String] = parliamentaryQuestionWebDAO.getAllIDsAsStream

    Await.result[Boolean](stream.isEmpty) shouldBe false
  }

  it should "return a empty AsyncStream when getOffsets returns no offsets" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54"), *)
      .returns(new HttpResponse(body = "", code = 404, headers = Map()))
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .returns(new HttpResponse(body = qrva, code = 200, headers = Map()))
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "10"), *)
      .returns(new HttpResponse(body = "", code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val stream: AsyncStream[String] = parliamentaryQuestionWebDAO.getAllIDsAsStream

    Await.result[Boolean](stream.isEmpty) shouldBe true
  }

  it should "return a empty AsyncStream when getIDS returns no ids" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54"), *)
      .returns(new HttpResponse(body = qrva, code = 200, headers = Map()))
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "0"), *)
      .returns(new HttpResponse(body = "", code = 404, headers = Map()))
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva", Map("leg" -> "54", "start" -> "10"), *)
      .returns(new HttpResponse(body = "", code = 404, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val stream: AsyncStream[String] = parliamentaryQuestionWebDAO.getAllIDsAsStream

    Await.result[Boolean](stream.isEmpty) shouldBe true
  }

  behavior of "getObject"

  it should "return some if the api call succeeds" in {

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(new HttpService)
    val response: Option[(String ,ParliamentaryQuestionWeb)] = parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")

    response should not be empty
  }

  it should "return some if the http request succeeds" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004", *, *)
      .returns(HttpResponse(body = qrva_item, code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val response: Option[(String ,ParliamentaryQuestionWeb)] = parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")

    response should not be empty
  }

  it should "return none if the http request fails" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004", *, *)
      .returns(HttpResponse(body = "", code = 404, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val response: Option[(String ,ParliamentaryQuestionWeb)] = parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")

    response shouldBe empty
  }

  it should "return none if the json is unparseable" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004", *, *)
      .returns(HttpResponse(body = "", code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val response: Option[(String, ParliamentaryQuestionWeb)] = parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")

    response shouldBe empty
  }

  it should "return none if the json doesn't contain items" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004", *, *)
      .returns(HttpResponse(body = qrva_item_empty, code = 200, headers = Map()))

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val response: Option[(String, ParliamentaryQuestionWeb)] = parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")

    response shouldBe empty
  }

  it should "return none if the http request timeouts" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004", *, *)
      .throws(new SocketTimeoutException())

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    val response: Option[(String, ParliamentaryQuestionWeb)] = parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")

    response shouldBe empty
  }

  it should "throw an error if an error other than timeout occurs" in {
    val httpServiceStub = stub[HttpService]
    (httpServiceStub.url _)
      .when("http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004", *, *)
      .throws(new Throwable())

    val parliamentaryQuestionWebDAO: ParliamentaryQuestionWebDAO =
      new ParliamentaryQuestionWebDAO(httpServiceStub)
    assertThrows[Throwable] {
      parliamentaryQuestionWebDAO.getObject("54-B001-3-0001-0000201400004")
    }
  }


}
