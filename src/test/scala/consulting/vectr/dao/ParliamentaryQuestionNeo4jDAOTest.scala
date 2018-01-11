package consulting.vectr.dao

import consulting.vectr.model._
import org.neo4j.driver.v1._
import org.neo4j.harness.{ServerControls, TestServerBuilders}
import org.scalatest.tagobjects.Slow
import org.scalatest.{Filter => _, _}

class ParliamentaryQuestionNeo4jDAOTest extends FlatSpec with Matchers with BeforeAndAfterAll with BeforeAndAfterEach {
  import scala.collection.JavaConverters._

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
    sub_department_name_nl = Some("Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken"),
    sub_department_name_fr = Some("Affaires étrangères, Commerce extérieur et Affaires européennes"),
    department_name_nl = "Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken",
    department_name_fr = "Vice-premier ministre et ministre des Affaires étrangères, du Commerce extérieur et des Affaires européennes",
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

  override def afterEach: Unit = {
    cleanNeo(driver)
  }

  override def afterAll: Unit = {
    neo4jControls.close()
  }

  behavior of "storePQuestionWeb"
  it should "store an object" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)

    val session2 = driver.session()
    val result = session2.run("MATCH (q:question) RETURN q.id as question")
    session2.close()
    result.peek().get("question").asString() should not be empty
  }

  behavior of "getAllPQuestions"
  it should "return no object" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    val result = neodao.allPQuestions("nl")
    result shouldBe empty
  }
  it should "return an object for nl" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val result = neodao.allPQuestions("nl")
    result should not be empty
  }
  it should "return an object for fr" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val result = neodao.allPQuestions("fr")
    result should not be empty
  }

  behavior of "getAllSDOCNAMES"
  it should "return an empty list when there are no questions" taggedAs Slow in {
    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    val sdocnames = neodao.allSDOCNAMES
    sdocnames shouldBe empty
  }
  it should "return a non-empty list when there is a questions" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val sdocnames = neodao.allSDOCNAMES
    sdocnames should not be empty
  }

  behavior of "storeFilters"
  it should "store new filters into the neo4j db" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    neodao.storeFilters(entities, "nl")
    val session = driver.session()
    var result = session.run("MATCH (f:filter) RETURN f.count AS c").peek()
    info("first time a filter is stored the count should be 1")
    result.get("c").asInt shouldBe 1

    neodao.storeFilters(entities, "nl")
    result = session.run("MATCH (f:filter) RETURN f.count AS c").peek()
    info("second time a filter is stored the count should be 2")
    result.get("c").asInt shouldBe 2
    session.close()
  }
  it should "log the error when neo4j data is wrong" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)
    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val session = driver.session()
    session.run("MATCH (p:party {name:'CD&V'}) CREATE (f:filter) MERGE (f)-[:W_PARTY]->(p) ")
    session.run("MATCH (p:party {name:'CD&V'}) CREATE (f:filter) MERGE (f)-[:W_PARTY]->(p) ")
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"))
    neodao.storeFilters(entities, "nl")
    val result = session.run("MATCH (f:filter), (x_0:party {name:'CD&V'})<-[:W_PARTY]-(f) OPTIONAL MATCH (f)-[r]-() WITH f, count(r) as rels WHERE rels = 1 RETURN ID(f) as id").list()
    result.size shouldBe 2
    session.close()
  }

  behavior of "updateFilter"
  it should "not fail if there is no filter for the given id" in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name_nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    neodao.updateFilter(900, public = true)
  }
  it should "update the public field of the filter" in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities2, "nl")

    val session2 = driver.session()
    val result = session2.run("MATCH (f:filter) RETURN ID(f) as id").asScala.toList
    val ids = result.map(record => record.get("id").asInt())
    session2.close()

    ids.foreach(id => neodao.updateFilter(id, public = true))

    val session3 = driver.session()
    val result2 = session3.run("MATCH (f:filter) RETURN f.public as public").asScala.toList
    val publics = result2.map(record => record.get("public").asBoolean())
    session3.close()

    publics should not be empty
    publics.size should equal(2)
    publics.head shouldBe true
    publics.tail.head shouldBe true
  }

  behavior of "removeFilter"
  it should "not fail if there is no filter for the given id" in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name_nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    neodao.removeFilter(900)
  }
  it should "remove a filter" in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities2, "nl")

    val session2 = driver.session()
    val result = session2.run("MATCH (f:filter) RETURN ID(f) as id").asScala.toList
    val ids = result.map(record => record.get("id").asInt())
    session2.close()

    neodao.allFilters("nl").size should equal(2)

    neodao.removeFilter(ids.head)

    neodao.allFilters("nl").size should equal(1)

    neodao.removeFilter(ids.tail.head)

    neodao.allFilters("nl") shouldBe empty
  }

  behavior of "getTopFilters"
  it should "return an empty list when there are no filters" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name_nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)

    neodao.topFilters(2, "nl") shouldBe empty
  }
  it should "return an empty list when there are no public filters" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name_nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities2, "nl")

    neodao.topFilters(2, "nl") shouldBe empty
  }
  it should "return the most used filters but only those that are public" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")

    val session2 = driver.session()
    session2.run("MATCH (f:filter) SET f.public = true")
    session2.close()

    neodao.storeFilters(entities2, "nl")

    val expected: List[Filter] = List(
      Filter(50, true, 2, "", List(FilterEntity("author", "Nahima Lanjri"), FilterEntity("party", "CD&V")))
    )
    val result: List[Filter] = neodao.topFilters(2, "nl")
    result should not be empty
    result.size should equal(1)
    result.head.count should equal(2)
    result.head.public should equal(true)
    result.head.question shouldBe empty
    result.head.entities should contain(FilterEntity("author", "Nahima Lanjri"))
    result.head.entities should contain(FilterEntity("party", "CD&V"))
  }
  it should "return all the public most used filters" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities2, "nl")

    val session2 = driver.session()
    session2.run("MATCH (f:filter) SET f.public = true")
    session2.close()

    val result: List[Filter] = neodao.topFilters(2, "nl")
    result should not be empty
    result.size should equal(2)
    result.head.public should equal(true)
    result.head.count should equal(2)
    result.head.question shouldBe empty
    result.head.entities should contain(FilterEntity("author", "Nahima Lanjri"))
    result.head.entities should contain(FilterEntity("party", "CD&V"))

    result.tail.head.public should equal(true)
    result.tail.head.count should equal(1)
    result.tail.head.question shouldBe empty
    result.tail.head.entities should contain(FilterEntity("party", "CD&V"))
  }

  behavior of "getFilters"
  it should "return an empty list when there are no filters" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name_nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)

    neodao.allFilters("nl") shouldBe empty
  }
  it should "return a non-empty list when there are only non-public filters" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities2, "nl")

    val result: List[Filter] = neodao.allFilters("nl")
    result should not be empty
    result.size should equal(2)
    result.head.public should equal(false)
    result.head.count should equal(2)
    result.head.question shouldBe empty
    result.head.entities should contain(FilterEntity("author", "Nahima Lanjri"))
    result.head.entities should contain(FilterEntity("party", "CD&V"))

    result.tail.head.public should equal(false)
    result.tail.head.count should equal(1)
    result.tail.head.question shouldBe empty
    result.tail.head.entities should contain(FilterEntity("party", "CD&V"))
  }
  it should "return the all the filters public and non-public" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")

    val session2 = driver.session()
    session2.run("MATCH (f:filter) SET f.public = true")
    session2.close()

    neodao.storeFilters(entities2, "nl")

    val result: List[Filter] = neodao.allFilters("nl")
    result should not be empty
    result.size should equal(2)
    result.head.count should equal(2)
    result.head.public should equal(true)
    result.head.question shouldBe empty
    result.head.entities should contain(FilterEntity("author", "Nahima Lanjri"))
    result.head.entities should contain(FilterEntity("party", "CD&V"))

    result.tail.head.public should equal(false)
    result.tail.head.count should equal(1)
    result.tail.head.question shouldBe empty
    result.tail.head.entities should contain(FilterEntity("party", "CD&V"))
  }
  it should "return all the filters" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao = new ParliamentaryQuestionNeo4jDAO(driver)

    //load some data first
    neodao.storePQuestionWeb(qrva_item_parsed)
    val entities = Map[String, Set[String]]("party" -> Set("CD&V"), "author" -> Set("Nahima Lanjri"))
    val entities2 = Map[String, Set[String]]("party" -> Set("CD&V"))

    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities, "nl")
    neodao.storeFilters(entities2, "nl")

    val session2 = driver.session()
    session2.run("MATCH (f:filter) SET f.public = true")
    session2.close()

    val result: List[Filter] = neodao.topFilters(2, "nl")
    result should not be empty
    result.size should equal(2)
    result.head.public should equal(true)
    result.head.count should equal(2)
    result.head.question shouldBe empty
    result.head.entities should contain(FilterEntity("author", "Nahima Lanjri"))
    result.head.entities should contain(FilterEntity("party", "CD&V"))

    result.tail.head.public should equal(true)
    result.tail.head.count should equal(1)
    result.tail.head.question shouldBe empty
    result.tail.head.entities should contain(FilterEntity("party", "CD&V"))
  }

  behavior of "getAuthors"
  it should "return an empty list when there are no authors" taggedAs Slow in {
    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    val authors: Entity = neodao.authors
    authors.entries shouldBe empty
  }
  it should "return a non-empty list when there are authors" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\"})")
    session1.close()

    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val authors: Entity = neodao.authors

    val expected = Entity("author", List(EntityValue("Nahima Lanjri", List("Nahima Lanjri", "nahima lanjri", "Lanjri"))))
    authors.entries should not be empty
    authors should equal(expected)
  }

  behavior of "getDepartments"
  it should "return an empty list when there are no departments" taggedAs Slow in {
    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    val departments_nl: Entity = neodao.departments("nl")
    departments_nl.entries shouldBe empty

    val departments_fr: Entity = neodao.departments("fr")
    departments_fr.entries shouldBe empty
  }
  it should "return a non-empty list when there are departments" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\", name__fr:\"Vice-premier ministre et ministre des Affaires étrangères et européennes, chargé de Beliris et des Institutions culturelles fédérales\", name_nl:\"Didier Reynders\", name_fr:\"Didier Reynders\" })")
    session1.close()

    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)

    val session = driver.session()
    session.run("CREATE (:department{name__nl:\"eerste minister\", name__fr:\"Premier ministre\", name_nl:\"Charles Michel\", name_fr:\"Charles Michel\"})")
    session.run("CREATE (:department{name__nl:\"Staatssecretaris voor Ambtenarenzaken en Modernisering van de Openbare Diensten,toegevoegd aan de minister van Financiën, belast met Ambtenarenzaken\", name__fr:\"Secrétaire d'Etat à la Lutte contre la fraude sociale, à la Protection de la vie privée et à la Mer du Nord, adjoint à la ministre des Affaires sociales et de la Santé publique\", name_nl:\"Jan Janssens\", name_fr:\"Jan Janssens\"})")
    session.close()

    val departments_nl: Entity = neodao.departments("nl")
    val expected_nl = Entity(
      "department",
      List(
        EntityValue(
          "Didier Reynders",
          List(
            "vice-eersteminister",
            "minister van buitenlandse zaken",
            "minister voor buitenlandse zaken",
            "minister van buitenlandse handel",
            "minister voor buitenlandse handel",
            "minister van europese zaken",
            "minister voor europese zaken",
            "didier reynders"
          )
        ),
        EntityValue(
          "Charles Michel",
          List(
            "eerste minister",
            "premier",
            "charles michel"
          )
        ),
        EntityValue("Jan Janssens",
          List(
            "staatssecretaris van ambtenarenzaken",
            "staatssecretaris voor ambtenarenzaken",
            "staatssecretaris van modernisering van de openbare diensten",
            "staatssecretaris voor modernisering van de openbare diensten",
            "jan janssens"
          )
        )
      )
    )
    departments_nl.entries should not be empty
    departments_nl should equal(expected_nl)

    val departments_fr: Entity = neodao.departments("fr")
    val expected_fr = Entity(
      "department",
      List(
        EntityValue(
          "Didier Reynders",
          List(
            "vice-premier ministre",
            "ministre des affaires étrangères et européennes",
            "ministre chargé de beliris",
            "ministre des institutions culturelles fédérales",
            "didier reynders"
          )
        ),
        EntityValue(
          "Charles Michel",
          List(
            "premier ministre",
            "premier",
            "charles michel"
          )
        ),
        EntityValue(
          "Jan Janssens",
          List(
            "secrétaire d'etat à la lutte contre la fraude sociale",
            "secrétaire d'etat à la protection de la vie privée et à la mer du nord",
            "secrétaire d'etat adjoint à la ministre des affaires sociales",
            "secrétaire d'etat de la santé publique",
            "jan janssens"
          )
        )
      )
    )
    departments_fr.entries should not be empty
    departments_fr should equal(expected_fr)
  }

  behavior of "getTopics"
  it should "return an empty list when there are no topics" taggedAs Slow in {
    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    val topics_nl: Entity = neodao.topics("nl")
    topics_nl.entries shouldBe empty

    val topics_fr: Entity = neodao.topics("fr")
    topics_fr.entries shouldBe empty
  }
  it should "return a non-empty list when there are topics" taggedAs Slow in {
    val session1 = driver.session()
    session1.run("CREATE (:department {name__nl:\"Vice-eersteminister en minister van Buitenlandse Zaken, Buitenlandse Handel en Europese Zaken\",name_nl:\"Didier Reynders\" })")
    session1.close()

    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    neodao.storePQuestionWeb(qrva_item_parsed)
    val topics_nl: Entity = neodao.topics("nl")
    val expected_nl = Entity(
      "topic",
      List(
        EntityValue(
          "Buitenlandse beleid",
          List(
            "buitenlandse beleid"
          )
        )
      )
    )
    topics_nl.entries should not be empty
    topics_nl should equal(expected_nl)

    val topics_fr: Entity = neodao.topics("fr")
    val expected_fr = Entity(
      "topic",
      List(
        EntityValue(
          "Politique exterieure",
          List(
            "politique exterieure"
          )
        )
      )
    )
    topics_fr.entries should not be empty
    topics_fr should equal(expected_fr)
  }

  behavior of "getParties"
  it should "return an empty list when there are no parties" taggedAs Slow in {
    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    val parties: Entity = neodao.parties
    parties.entries shouldBe empty
  }
  it should "return a non-empty list when there are parties" taggedAs Slow in {
    val neodao: ParliamentaryQuestionNeo4jDAO = new ParliamentaryQuestionNeo4jDAO(driver)
    val parties_query = List(
      "CREATE (:party {name: \"N-VA\", name_nl: \"N-VA\", name_fr: \"N-VA\", color: \"gold\", synonyms: \"n-va,nva,nv a,nieuw-vlaamse alliantie,nieuw vlaamse alliantie\"})",
      "CREATE (:party {name: \"VB\", name_nl: \"VB\", name_fr: \"VB\", color: \"brown\", synonyms: \"vb,vlaams belang, vlaams-belang\"})",
      "CREATE (:party {name: \"Open Vld\", name_nl: \"Open Vld\", name_fr: \"Open Vld\", color: \"blue\", synonyms: \"open vld,open-vld,open vlaamse liberalen en democraten\"})",
      "CREATE (:party {name: \"SP.A\", name_nl: \"SP.A\", name_fr: \"SP.A\", color: \"red\", synonyms: \"sp.a,spa,sp-a,sp a,socialistische partij anders\"})",
      "CREATE (:party {name: \"Vuye&Wouters\", name_nl: \"Vuye&Wouters\", name_nl: \"Vuye&Wouters\", color: \"DimGray\", synonyms: \"vuye&wouters,vuye en wouters,vuye wouters,v&w,vuwo\"})",
      "CREATE (:party {name: \"CD&V\", name_nl: \"CD&V\", name_fr: \"CD&V\", color: \"orange\", synonyms: \"cd&v,cdnv,cd v,christen-democratisch en vlaams\"})",
      "CREATE (:party {name: \"Ecolo-Groen\", name_nl: \"Ecolo-Groen\", name_fr: \"Ecolo-Groen\", color: \"green\", synonyms: \"ecolo-groen,groen,ecolo,ecolo groen,ecolo&groen\"})",
      "CREATE (:party {name: \"MR\", name_nl: \"MR\", name_fr: \"MR\", color: \"blue\", synonyms: \"mr,mouvement reformateur\"})",
      "CREATE (:party {name: \"PS\", name_nl: \"PS\", name_fr: \"PS\", color: \"red\", synonyms: \"ps,parti socialiste\"})",
      "CREATE (:party {name: \"CDH\", name_nl: \"CDH\", name_fr: \"CDH\", color: \"orange\", synonyms: \"cdh,centre democrate humaniste\"})",
      "CREATE (:party {name: \"FDF\", name_nl: \"FDF\", name_fr: \"FDF\", color: \"pink\", synonyms: \"fdf,defi,federalistes democrates francophones\"})",
      "CREATE (:party {name: \"PTB-GO!\", name_nl: \"PTB-GO!\", name_nl: \"PTB-GO!\", color: \"red\", synonyms:\"ptb-go\"})",
      "CREATE (:party {name: \"DEFI\", name_nl: \"DEFI\", name_fr: \"DEFI\", color: \"GreenYellow\", synonyms: \"fdf,defi,federalistes democrates francophones\"})",
      "CREATE (:party {name: \"VUWO\", name_nl: \"VUWO\", name_fr: \"VUWO\", color: \"lightcyan\", synonyms: \"vuwo,vuye&wouters,vuye en wouters,vuye wouters, v&w\"})",
      "CREATE (:party {name: \"UNKN\", name_nl: \"UNKN\", name_fr: \"UNKN\", color: \"moccasin\", synonyms: \"unkn,unknown,onbekend\"})",
      "CREATE (:party {name: \"default\", name_nl: \"default\", name_fr: \"default\", color: \"pink\", synonyms: \"default\"})"
    )
    val session = driver.session()
    parties_query.foreach(party => {
      try {
        session.run(party)
      } catch {
        case x: Throwable =>
          x.printStackTrace()
      }
    })
    session.close()

    val parties: Entity = neodao.parties
    val expected = Entity(
      "party",
      List(
        EntityValue("N-VA", List("n-va", "nva", "nv a", "nieuw-vlaamse alliantie", "nieuw vlaamse alliantie")),
        EntityValue("VB", List("vb", "vlaams belang", "vlaams-belang")),
        EntityValue("Open Vld", List("open vld", "open-vld", "open vlaamse liberalen en democraten")),
        EntityValue("SP.A", List("sp.a", "spa", "sp-a", "sp a", "socialistische partij anders")),
        EntityValue("Vuye&Wouters", List("vuye&wouters", "vuye en wouters", "vuye wouters", "v&w", "vuwo")),
        EntityValue("CD&V", List("cd&v", "cdnv", "cd v", "christen-democratisch en vlaams")),
        EntityValue("Ecolo-Groen", List("ecolo-groen", "groen", "ecolo", "ecolo groen", "ecolo&groen")),
        EntityValue("MR", List("mr", "mouvement reformateur")),
        EntityValue("PS", List("ps", "parti socialiste")),
        EntityValue("CDH", List("cdh", "centre democrate humaniste")),
        EntityValue("FDF", List("fdf", "defi", "federalistes democrates francophones")),
        EntityValue("PTB-GO!", List("ptb-go")),
        EntityValue("DEFI", List("fdf", "defi", "federalistes democrates francophones")),
        EntityValue("VUWO", List("vuwo", "vuye&wouters", "vuye en wouters", "vuye wouters", "v&w")),
        EntityValue("UNKN", List("unkn", "unknown", "onbekend")),
        EntityValue("default", List("default"))
      )
    )
    parties.entries should not be empty
    parties should equal(expected)
  }

  private def cleanNeo(driver: Driver): Unit = {
    val session = driver.session()
    session.run("MATCH (n) detach delete n").consume()
    session.close()
  }
}
