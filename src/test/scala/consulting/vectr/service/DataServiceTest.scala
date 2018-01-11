package consulting.vectr.service

import ammonite.ops.{ls, mkdir, pwd, rm}
import com.twitter.concurrent.AsyncStream
import com.typesafe.config.ConfigFactory
import consulting.vectr.dao._
import consulting.vectr.model.{Entity, ParliamentaryQuestionWeb, Filter, FilterEntity}
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import io.circe.syntax._

import scala.io.Source

class DataServiceTest extends FlatSpec with Matchers with MockFactory {
  val qrva_item: String = Source.fromInputStream(getClass.getResourceAsStream("/qrva_item.json")).mkString
  val qrva_item_parsed: ParliamentaryQuestionWeb = ParliamentaryQuestionWeb(
    link = "http://data.dekamer.be/v0/qrva/54-B001-3-0001-0000201400004",
    id = 146303,
    status = "answerReceived",
    legislation = 54,
    sdocname = "54-B001-3-0001-0000201400004",
    document_id = "0000201400004",
    document_date = Some("20140718"),
    author = "Nahima Lanjri",
    author_party = "CD&V",
    language = "N",
    department_number = 822,
    department_name_nl = "Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken",
    department_name_fr = "Affaires étrangères, Commerce extérieur et Affaires européennes",
    sub_department_name_nl = Some("Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken"),
    sub_department_name_fr = Some("Vice-premier ministre et ministre des Affaires étrangères, du Commerce extérieur et des Affaires européennes"),
    question_number = 1,
    title_nl = "Onderzoek naar schijnhuwelijken en schijn-wettelijke samenwoonst. - Ambassades. - Instructies.",
    title_fr = "Enquête relative aux mariages de complaisance et aux cohabitations légales de complaisance. - Ambassades. - Instructions.",
    question_text_nl = Some("In het kader van de strijd tegen de schijnhuwelijken en de schijn-wettelijke samenwoonst wordt ook door onze ambassades in het buitenland een onderzoek gevoerd naar mogelijk misbruik van de migratiewet door huwelijksmigratie.In vele landen moeten mensen echter een zeer lange afstand afleggen om de Belgische ambassade te bezoeken in het kader van dit onderzoek. Problematisch daarbij is vooral dat zij niet of nauwelijks ingelicht worden over de nodige documenten of de inhoud van het interview dat zal plaatshebben. Het komt dan ook vaak voor dat de mensen onvoldoende documenten of bewijsmiddelen mee hebben en hun reis dus opnieuw moeten ondernemen om nieuwe bewijsstukken aan te leveren.1. Welke instructies worden aan de ambassades gegeven in het kader van het onderzoek naar schijnhuwelijken en schijn-wettelijke samenwoonst?2. Zijn de ambassades verplicht de mensen op voorhand in te lichten over welke documenten en bewijsmiddelen zij dienen te presenteren in het kader van hun interview?3. Indien niet, overweegt u hierover duidelijke richtlijnen op te stellen zodat de betrokken personen niet onverricht ter zake moeten terugkeren bij gebrek aan onvoldoende bewijsmiddelen of documenten?"),
    question_text_fr = Some("Dans le cadre de la lutte contre les mariages de complaisance et les cohabitations légales de complaisance, nos ambassades à l'étranger mènent également l'enquête sur les abus éventuels commis dans le cadre de la loi sur la migration au travers de la migration matrimoniale.Dans de nombreux pays, les personnes concernées par l'enquête doivent toutefois parcourir de très longues distances pour se rendre à l'ambassade belge. Le problème réside surtout dans le fait qu'elles ne sont pas ou qu'elles sont mal informées sur les documents dont elles doivent disposer ou du contenu de la future interview. Il arrive dès lors souvent que les personnes ne disposent pas de tous les documents ou preuves nécessaires et doivent ainsi refaire le voyage pour apporter les pièces justificatives manquantes.1. Quelles instructions sont communiquées aux ambassades dans le cadre de l'enquête relative aux mariages de complaisance et aux cohabitations légales de complaisance?2. Les ambassades sont-elles tenues d'informer préalablement les personnes sur les documents et les preuves à présenter dans le cadre de leur interview?3. Dans la négative, envisagez-vous de rédiger des directives claires à ce sujet de sorte que les personnes concernées ne rentrent pas bredouilles de leur visite à l'ambassade pour cause de preuves ou de documents manquants?"),
    answer_text_nl = Some("Ik wil er eerst op wijzen dat een bezoek aan de consulaire post niet altijd noodzakelijk is. De aanvraag voor een attest geen huwelijksbeletsel kan schriftelijk gebeuren. Tenzij er elementen zouden zijn die kunnen wijzen op een mogelijk schijnhuwelijk, wordt het attest naar de aanvrager opgezonden. Indien bij de aanvraag niet alle nodige bewijsstukken aanwezig waren, wordt gevraagd die alsnog op te zenden.Als een interview op de consulaire post nodig blijkt, wordt - meestal telefonisch - een afspraak geregeld; bij deze gelegenheid wordt aan de betrokkenen meegedeeld welke documenten ze moeten meebrengen.Een nutteloze verplaatsing naar het consulaat is dus zo goed als uitgesloten."),
    answer_text_fr = Some("J'attire votre attention sur le fait qu'un déplacement vers le poste consulaire n'est pas toujours nécessaire. La demande d'un certificat de non-empêchement à mariage peut se faire par écrit. Sauf dans les cas où des éléments peuvent faire croire à  l'éventualité d'un mariage blanc, l'attestation est envoyée au demandeur.Si lors de la demande, des documents manquent, le poste demande de les envoyer.Si une interview au poste consulaire s'impose, un rendez-vous est fixé  - dans la plupart des cas par téléphone. À cette occasion le demandeur est informé des documents qu'il devra présenter.Un déplacement inutile vers le consulat est donc pratiquement exclu."),
    subject_nl = Some(""),
    subject_fr = Some(""),
    sub_subject_nl = List[String](),
    sub_subject_fr = List[String]()
  )

  behavior of "dataFromWebAndInsertInNeo4j"
  it should "not call neodao or filedoa if no items are retrieved" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ

    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).twice()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).twice()
    // getDataFromWebAndInsertInNeo4j
    (neo4jdao.allSDOCNAMES _).expects().returns(Set()).once()
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List())).once()
    (webdao.getObject _).expects(*).returns(None).never()
    (neo4jdao.storePQuestionWeb _).expects(*).never()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList shouldBe empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "not call neodao or filedoa if no new items are retrieved" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ

    val webdao = mock[ParliamentaryQuestionWebDAO]

    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).twice()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).twice()
    // getDataFromWebAndInsertInNeo4j
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List("test"))).once
    (neo4jdao.allSDOCNAMES _).expects().returns(Set("test")).once
    (webdao.getObject _).expects(*).returns(None).never()
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList shouldBe empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "not call neodao or filedoa if the items retrieval fails" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).twice()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).twice()
    // getDataFromWebAndInsertInNeo4j
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List("test"))).once
    (neo4jdao.allSDOCNAMES _).expects().returns(Set()).once
    (webdao.getObject _).expects("test").returns(None).once
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList shouldBe empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "call neodao and filedoa" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ

    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).twice()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).twice()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).twice()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).twice()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).twice()
    // getDataFromWebAndInsertInNeo4j
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List("test"))).once
    (neo4jdao.allSDOCNAMES _).expects().returns(Set()).once
    (webdao.getObject _).expects("test").returns(Some((qrva_item, qrva_item_parsed))).once
    (neo4jdao.storePQuestionWeb _).expects(*).once()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList should not be empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }

  behavior of "dataFromFilesAndInsertInNeo4j"
  it should "load no files if the directory is empty" in {
    rm ! pwd / 'src / 'test / 'resources / 'emptyPQ
    mkdir ! pwd / 'src / 'test / 'resources / 'emptyPQ

    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'emptyPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getDataFromFilesAndInsertInNeo4j
    (neo4jdao.allSDOCNAMES _).expects().returns(Set()).once
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromFilesAndInsertInNeo4j()
  }
  it should "load no files if they are already present in neo4j" in {
    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'loadPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getDataFromFilesAndInsertInNeo4j
    (neo4jdao.allSDOCNAMES _).expects().returns(Set("54-B001-3-0001-0000201400004")).once
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromFilesAndInsertInNeo4j()
  }
  it should "load files if the directory isn't empty" in {
    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'loadPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getDataFromFilesAndInsertInNeo4j
    (neo4jdao.allSDOCNAMES _).expects().returns(Set()).once
    (neo4jdao.storePQuestionWeb _).expects(*).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.dataFromFilesAndInsertInNeo4j()
  }

  behavior of "allParliamentaryQuestions"
  it should "call neo4jdao to retrieve all questions for nl language" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getAllParliamentaryQuestions
    (neo4jdao.allPQuestions _).expects("nl").once()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.allParliamentaryQuestions("nl")
  }
  it should "call neo4jdao to retrieve all questions for fr language" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getAllParliamentaryQuestions
    (neo4jdao.allPQuestions _).expects("fr").once()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.allParliamentaryQuestions("fr")
  }

  behavior of "resolvedEntitiesAndSaveToNeo4j"
  it should "resolve entities with NL nlp service" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getResolvedEntitiesAndSaveToNeo4j
    (neo4jdao.storePQuestionWeb _).expects(*).noMoreThanOnce()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.resolvedEntitiesAndSaveToNeo4j("Some query", "nl")
    val expectedMap: Map[String, Set[String]] =
      Map("author" -> Set(), "department" -> Set(), "topic" -> Set(), "party" -> Set())
    result should equal(expectedMap)
  }
  it should "resolve entities with FR nlp service" in {

    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val queryString = "Some query"

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getResolvedEntitiesAndSaveToNeo4j
    (neo4jdao.storePQuestionWeb _).expects(*).noMoreThanOnce()

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.resolvedEntitiesAndSaveToNeo4j("Some query", "fr")
    val expectedMap: Map[String, Set[String]] =
      Map("author" -> Set(), "department" -> Set(), "topic" -> Set(), "party" -> Set())
    result should equal(expectedMap)
  }

  behavior of "topQuestionsFromNeo4j"
  it should "return an empty json object if there are no filters" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.topFilters _).expects(2, "nl").returns(List()).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.topQuestionsFromNeo4j(2, "nl")
    result shouldBe empty
  }
  it should "get the most used filters/questions in nl from nlpservice as json" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.topFilters _).expects(2, "nl").returns(List(Filter(1, public = true, 1, "", List(FilterEntity("author", "test"))))).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.topQuestionsFromNeo4j(2, "nl")
    val questions = List(
      "Geef mij alle vragen gesteld door test",
      "Geef mij alle vragen van test",
      "Alle vragen gesteld door test",
      "Alle vragen van test"
    )
    val expected_result = questions.map {
      question => List(Filter(0, public = true, 1, question, List(FilterEntity("author", "test"))))
    }
    result should not be empty
    expected_result should contain(result)
  }
  it should "get the most used filters/questions in fr from nlservice as json" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.topFilters _).expects(2, "fr").returns(List(Filter(1, public = true, 1, "", List(FilterEntity("author", "test"))))).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.topQuestionsFromNeo4j(2, "fr")
    val questions = List(
      "Donne moi toutes les questions posées par test",
      "Donne moi toutes les questions de test",
      "Toutes les questions posées par test",
      "Toutes les questions de test"
    )
    val expected_result = questions.map {
      question => List(Filter(0, public = true, 1, question, List(FilterEntity("author", "test"))))
    }
    result should not be empty
    expected_result should contain(result)
  }

  behavior of "allQuestionFromNeo4j"
  it should "return an empty json object if there are no filters" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.allFilters _).expects("nl").returns(List()).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.allQuestionFromNeo4j("nl")
    result shouldBe empty
  }
  it should "get the most used filters/questions in nl from nlpservice as json" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.allFilters _).expects("nl").returns(List(Filter(1, public = true, 1, "", List(FilterEntity("author", "test"))))).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.allQuestionFromNeo4j("nl")
    val questions = List(
      "Geef mij alle vragen gesteld door test",
      "Geef mij alle vragen van test",
      "Alle vragen gesteld door test",
      "Alle vragen van test"
    )
    val expected_result = questions.map {
      question => List(Filter(1, public = true, 1, question, List(FilterEntity("author", "test"))))
    }
    result should not be empty
    expected_result should contain(result)
  }
  it should "get the most used filters/questions in fr from nlservice as json" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.allFilters _).expects("fr").returns(List(Filter(1, public = true, 1, "", List(FilterEntity("author", "test"))))).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    val result = dataService.allQuestionFromNeo4j("fr")
    val questions = List(
      "Donne moi toutes les questions posées par test",
      "Donne moi toutes les questions de test",
      "Toutes les questions posées par test",
      "Toutes les questions de test"
    )
    val expected_result = questions.map {
      question => List(Filter(1, public = true, 1, question, List(FilterEntity("author", "test"))))
    }
    result should not be empty
    expected_result should contain(result)
  }


  behavior of "updateFilterInNeo4j"
  it should "call neodao to update the filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.updateFilter _).expects(1, true).once
    (neo4jdao.updateFilter _).expects(1, false).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.updateFilterInNeo4j(1, public = true)
    dataService.updateFilterInNeo4j(1, public = false)
  }

  behavior of "removeFilterInNeo4j"
  it should "call neodao to remove the filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpServiceFactory = new NLPServiceFactory(neo4jdao)

    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    //NLP Service FR
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()
    //getTopQuestionsFromNeo4j
    (neo4jdao.removeFilter _).expects(1).once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpServiceFactory)
    dataService.removeFilterInNeo4j(1)
  }
}
