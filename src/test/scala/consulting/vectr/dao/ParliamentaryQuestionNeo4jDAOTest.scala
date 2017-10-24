package consulting.vectr.dao

import consulting.vectr.model.{ParliamentaryQuestionWeb, WitAIEntity}
import org.neo4j.driver.v1.{AuthTokens, Config, Driver, GraphDatabase}
import org.neo4j.harness.{ServerControls, TestServerBuilders}
import org.scalatest.tagobjects.Slow
import org.scalatest.{BeforeAndAfterAll, FlatSpec, Matchers}

class ParliamentaryQuestionNeo4jDAOTest extends FlatSpec with Matchers with BeforeAndAfterAll {
  val neo4jControls: ServerControls = TestServerBuilders.newInProcessBuilder.newServer
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
    subject_nl = Some("Buitenlandse beleid"),
    subject_fr = Some("Politique exterieure"),
    sub_subject_nl = List[String]("Ongehuwd samenleven", "Buitenlands beleid", "Toelating van vreemdelingen", "Administratieve formaliteit", "Ambassade", "Schijnhuwelijk"),
    sub_subject_fr = List[String]("Union libre", "Politique exterieure", "Admission des etrangers", "Formalite administrative", "Ambassade", "Marriage de complaisance")
  )

  val driver: Driver = GraphDatabase.driver(
    neo4jControls.boltURI,
    AuthTokens.basic("neo4j", "neo4j"),
    Config.build.withEncryptionLevel(Config.EncryptionLevel.NONE).toConfig)

  override def afterAll: Unit = {
    neo4jControls.close()
  }

  behavior of "storePQuestionWeb"
  it should "store an object" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val session = driver.session()
    val result = session.run("MATCH (q:question) RETURN q.id as question")
    session.close()
    result.peek().get("question").asString() should not be empty
    cleanNeo(driver)
  }

  behavior of "getAllPQuestions"
  it should "return no object" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    val result = neodao.getAllPQuestions()

    result shouldBe empty
    cleanNeo(driver)
  }
  it should "return an object" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val result = neodao.getAllPQuestions()

    result should not be empty
    cleanNeo(driver)
  }
  it should "return an object for 'fr'" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val result = neodao.getAllPQuestions("fr")

    result should not be empty
    cleanNeo(driver)
  }
  it should "return an object for 'nl'" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val result = neodao.getAllPQuestions("nl")

    result should not be empty
    cleanNeo(driver)
  }

  behavior of "hasSDOCNAME"
  it should "return true if the sdocname is present" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)

    neodao.hasSDOCNAME("54-B001-3-0001-0000201400004") shouldBe true
    cleanNeo(driver)
  }
  it should "return false if the sdocname isn't present" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    neodao.hasSDOCNAME("54-B001-3-0001-0000201400004") shouldBe false
    cleanNeo(driver)
  }

  behavior of "storeFilters"
  it should "store new filters into the neo4j db" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)

    val entities = Map[String, Set[String]]("party"-> Set("CD&V"), "author"->Set("Nahima Lanjri"))

    neodao.storeFilters(entities)
    val session = driver.session()
    var result = session.run("MATCH (f:filter) RETURN f.count AS c").list().get(0)

    result.get("c").asInt shouldBe 1

    neodao.storeFilters(entities)

    result = session.run("MATCH (f:filter) RETURN f.count AS c").list().get(0)

    result.get("c").asInt shouldBe 2

    session.close()

    cleanNeo(driver)
  }
  it should "return an object" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val result = neodao.getAllPQuestions()
  }

  it should "log the error when neo4j data is wrong" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)

    val session = driver.session()
    session.run("MATCH (p:party {name:'CD&V'}) CREATE (f:filter) MERGE (f)-[:W_PARTY]->(p) ")
    session.run("MATCH (p:party {name:'CD&V'}) CREATE (f:filter) MERGE (f)-[:W_PARTY]->(p) ")


    val entities = Map[String, Set[String]]("party"-> Set("CD&V"))

    neodao.storeFilters(entities)

    val result = session.run("MATCH (f:filter), (x_0:party {name:'CD&V'})<-[:W_PARTY]-(f) OPTIONAL MATCH (f)-[r]-() WITH f, count(r) as rels WHERE rels = 1 RETURN ID(f) as id").list()

    result.size shouldBe 2
    session.close()

    cleanNeo(driver)
  }

  behavior of "getTopFilters"
  it should "return the most used filters" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)

    val entities = Map[String, Set[String]]("party"-> Set("CD&V"), "author"->Set("Nahima Lanjri"))

    neodao.storeFilters(entities)
    neodao.storeFilters(entities)

    val entities2 = Map[String, Set[String]]("party"-> Set("CD&V"))

    neodao.storeFilters(entities2)

    val expected = List(Tuple2(2,List(Tuple2("author","Nahima Lanjri"),Tuple2("party","CD&V"))), Tuple2(1,List(Tuple2("party","CD&V"))))

    neodao.getTopFilters(5) shouldBe expected
  }



  private def cleanNeo(driver: Driver): Unit = {
    val session = driver.session()
    session.run("MATCH (n) detach delete n").consume()
    session.close()
  }


}
