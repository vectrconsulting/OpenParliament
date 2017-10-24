package consulting.vectr.service

import consulting.vectr.dao._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}
import ammonite.ops.{ls, mkdir, pwd, rm}
import com.twitter.concurrent.AsyncStream
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import consulting.vectr.model.{ParliamentaryQuestionWeb, WitAIEntity}

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
    answer_text_nl =  Some("Ik wil er eerst op wijzen dat een bezoek aan de consulaire post niet altijd noodzakelijk is. De aanvraag voor een attest geen huwelijksbeletsel kan schriftelijk gebeuren. Tenzij er elementen zouden zijn die kunnen wijzen op een mogelijk schijnhuwelijk, wordt het attest naar de aanvrager opgezonden. Indien bij de aanvraag niet alle nodige bewijsstukken aanwezig waren, wordt gevraagd die alsnog op te zenden.Als een interview op de consulaire post nodig blijkt, wordt - meestal telefonisch - een afspraak geregeld; bij deze gelegenheid wordt aan de betrokkenen meegedeeld welke documenten ze moeten meebrengen.Een nutteloze verplaatsing naar het consulaat is dus zo goed als uitgesloten."),
    answer_text_fr = Some("J'attire votre attention sur le fait qu'un déplacement vers le poste consulaire n'est pas toujours nécessaire. La demande d'un certificat de non-empêchement à mariage peut se faire par écrit. Sauf dans les cas où des éléments peuvent faire croire à  l'éventualité d'un mariage blanc, l'attestation est envoyée au demandeur.Si lors de la demande, des documents manquent, le poste demande de les envoyer.Si une interview au poste consulaire s'impose, un rendez-vous est fixé  - dans la plupart des cas par téléphone. À cette occasion le demandeur est informé des documents qu'il devra présenter.Un déplacement inutile vers le consulat est donc pratiquement exclu."),
    subject_nl = Some(""),
    subject_fr = Some(""),
    sub_subject_nl = List[String](),
    sub_subject_fr = List[String]()
  )

  behavior of "getDataFromWebAndInsertInNeo4j"
  it should "not call neodao or filedoa if no items are retrieved" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
    val webdao = mock[ParliamentaryQuestionWebDAO]
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List())).once()
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.storePQuestionWeb _).expects(*).never()

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)
    dataService.getDataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList shouldBe empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "not call neodao or filedoa if no new items are retrieved" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
    val webdao = mock[ParliamentaryQuestionWebDAO]
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List("test"))).once
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.hasSDOCNAME _).expects("test").returns(true).once
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)
    dataService.getDataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList shouldBe empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "not call neodao or filedoa if the items retrieval fails" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
    val webdao = mock[ParliamentaryQuestionWebDAO]
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List("test"))).once
    (webdao.getObject _).expects("test").returns(None).once
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.hasSDOCNAME _).expects("test").returns(false).once
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)
    dataService.getDataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList shouldBe empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "call neodao and filedoa" in {
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
    val webdao = mock[ParliamentaryQuestionWebDAO]
    (webdao.getAllIDsAsStream _).expects().returns(AsyncStream.fromSeq(List("test"))).once
    (webdao.getObject _).expects("test").returns(Some((qrva_item, qrva_item_parsed))).once
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.hasSDOCNAME _).expects("test").returns(false).once
    (neo4jdao.storePQuestionWeb _).expects(*).once()

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)
    dataService.getDataFromWebAndInsertInNeo4j()

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList should not be empty
    rm ! pwd / 'src / 'test / 'resources / 'writePQ
    mkdir ! pwd / 'src / 'test / 'resources / 'writePQ
  }

  behavior of "getDataFromFilesAndInsertInNeo4j"
  it should "load no files if the directory is empty" in {
    rm !  pwd / 'src / 'test / 'resources / 'emptyPQ
    mkdir ! pwd / 'src / 'test / 'resources / 'emptyPQ

    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.hasSDOCNAME _).expects(*).never
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'emptyPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)

    dataService.getDataFromFilesAndInsertInNeo4j()
  }
  it should "load no files if they are already present in neo4j" in {
    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.hasSDOCNAME _).expects("54-B001-3-0001-0000201400004").returns(true).once
    (neo4jdao.storePQuestionWeb _).expects(*).never

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'loadPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)

    dataService.getDataFromFilesAndInsertInNeo4j()
  }
  it should "load files if the directory isn't empty" in {
    val webdao = mock[ParliamentaryQuestionWebDAO]
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.hasSDOCNAME _).expects("54-B001-3-0001-0000201400004").returns(false).once
    (neo4jdao.storePQuestionWeb _).expects(*).once

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'loadPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)

    dataService.getDataFromFilesAndInsertInNeo4j()
  }

  behavior of "getAllParliamentaryQuestions"
  it should "call neo4jdao to retrieve all questions" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.getAllPQuestions _).expects(*).once()

    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)

    dataService.getAllParliamentaryQuestions("nl")
  }

  behavior of "getResolvedEntitiesAndSaveToNeo4j"
  it should "call resolve entities and async call neo4jdao to store questions" in {

    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val queryString = "Some query"

    val nlpservice = mock[NLPService]

    val returnmap = Map[String,Set[String]]()

    inAnyOrder {
      (nlpservice.getEntitiesFromSentence _).expects(queryString).returns(returnmap).once()
      (neo4jdao.storeFilters _).expects(*).noMoreThanOnce()
    }

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)

    dataService.getResolvedEntitiesAndSaveToNeo4j(queryString)
  }

  behavior of "getTopQuestionsFromNeo4j"
  it should "get the most used filters/questions from neo4j and return it as json" in {

    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]

    val webdao = mock[ParliamentaryQuestionWebDAO]

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=${cacheFolder}")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val nlpservice = mock[NLPService]

    val filters = List(Tuple2(2,List(Tuple2("author","Nahima Lanjri"),Tuple2("party","CD&V"))), Tuple2(1,List(Tuple2("party","CD&V"))))

    (neo4jdao.getTopFilters _).expects(2).returns(filters).once
    (nlpservice.title _).expects().returns("title").once
    (nlpservice.buildQuestion _).expects(List(Tuple2("author","Nahima Lanjri"),Tuple2("party","CD&V"))).returns("question1").once
    (nlpservice.buildQuestion _).expects(List(Tuple2("party","CD&V"))).returns("question2").once

    val dataService = new DataService(neo4jdao, webdao, filedao, nlpservice)

    val expected =
      """
        {
          "sidebar_title" : "title",
          "lang": "nl",
          "questions" : [
           {
               "count": 2,
               "question": "question1",
               "entities": [{"type":"author","value":"Nahima Lanjri"},{"type":"party","value":"CD&V"}]
            },
            {
              "count": 1,
              "question":"question2",
              "entities":[{"type":"party","value":"CD&V"}]
            }
          ]}
      """.replaceAll("\n| ","")

    dataService.getTopQuestionsFromNeo4j(2, "nl").replaceAll("\n| ","") shouldBe expected
  }

}
