package consulting.vectr.service

import consulting.vectr.dao.ParliamentaryQuestionNeo4jDAO
import consulting.vectr.model.{Entity, EntityValue, Filter, FilterEntity}
import io.circe.parser._
import org.scalamock.scalatest.MockFactory
import org.scalatest.{FlatSpec, Matchers}

import scala.io.Source

class NLPServiceTest extends FlatSpec with Matchers with MockFactory {
  val party_entity = Entity(
    "party",
    List(
      EntityValue("N-VA", List("n-va", "nva", "nieuw-vlaamse alliantie", "nieuw vlaamse alliantie")),
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

  behavior of "randStr"
  it should "return random string for each filtertype in nl" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val start: String = nlpService.randStr("start", language)
        language("start") should contain(start)

        val author: String = nlpService.randStr("author", language)
        language("author") should contain(author)

        val party: String = nlpService.randStr("party", language)
        language("party") should contain(party)

        val or: String = nlpService.randStr("or", language)
        language("or") should contain(or)

        val and: String = nlpService.randStr("and", language)
        language("and") should contain(and)

        val topic: String = nlpService.randStr("topic", language)
        language("topic") should contain(topic)

        val department: String = nlpService.randStr("department", language)
        language("department") should contain(department)

        val title: String = nlpService.randStr("title", language)
        language("title") should contain(title)
    }
  }
  it should "return random string for each filtertype in fr" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    //NLP Service NL
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("fr").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("fr").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("fr", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/fr.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val start: String = nlpService.randStr("start", language)
        language("start") should contain(start)

        val author: String = nlpService.randStr("author", language)
        language("author") should contain(author)

        val party: String = nlpService.randStr("party", language)
        language("party") should contain(party)

        val or: String = nlpService.randStr("or", language)
        language("or") should contain(or)

        val and: String = nlpService.randStr("and", language)
        language("and") should contain(and)

        val topic: String = nlpService.randStr("topic", language)
        language("topic") should contain(topic)

        val department: String = nlpService.randStr("department", language)
        language("department") should contain(department)

        val title: String = nlpService.randStr("title", language)
        language("title") should contain(title)
    }
  }

  behavior of "build"
  it should "return a string given a language file and a party filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val party_filter: List[FilterEntity] = List(FilterEntity("party", "test"))
        val party_question: String = nlpService.build(language, party_filter)
        val expected_questions = List(
          "Geef mij alle vragen van partij test",
          "Geef mij alle vragen van test",
          "Alle vragen van partij test",
          "Alle vragen van test"
        )
        party_question should not be empty
        expected_questions should contain(party_question)
    }
  }
  it should "return a string given a language file and multiple party filters" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val party_filter: List[FilterEntity] = List(FilterEntity("party", "test"), FilterEntity("party", "tset"))
        val party_question: String = nlpService.build(language, party_filter)
        val expected_questions = List(
          "Geef mij alle vragen van partij test en tset",
          "Geef mij alle vragen van test en tset",
          "Alle vragen van partij test en tset",
          "Alle vragen van test en tset"
        )
        party_question should not be empty
        expected_questions should contain(party_question)
    }
  }
  it should "return a string given a language file and a department filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val department_filter: List[FilterEntity] = List(FilterEntity("department", "test"))
        val department_question: String = nlpService.build(language, department_filter)
        val expected_questions = List(
          "Geef mij alle vragen gesteld aan de test",
          "Geef mij alle vragen gericht aan de test",
          "Geef mij alle vragen voor de test",
          "Alle vragen gesteld aan de test",
          "Alle vragen gericht aan de test",
          "Alle vragen voor de test"
        )
        department_question should not be empty
        expected_questions should contain(department_question)
    }
  }
  it should "return a string given a language file and multiple department filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val department_filter: List[FilterEntity] = List(FilterEntity("department", "test"), FilterEntity("department", "tset"))
        val department_question: String = nlpService.build(language, department_filter)
        val expected_questions = List(
          "Geef mij alle vragen gesteld aan de test en tset",
          "Geef mij alle vragen gericht aan de test en tset",
          "Geef mij alle vragen voor de test en tset",
          "Alle vragen gesteld aan de test en tset",
          "Alle vragen gericht aan de test en tset",
          "Alle vragen voor de test en tset"
        )
        department_question should not be empty
        expected_questions should contain(department_question)
    }
  }
  it should "return a string given a language file and a topic filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val topic_filter: List[FilterEntity] = List(FilterEntity("topic", "test"))
        val topic_question: String = nlpService.build(language, topic_filter)
        val expected_questions = List(
          "Geef mij alle vragen over test",
          "Geef mij alle vragen met als onderwerp test",
          "Geef mij alle vragen die gaan over test",
          "Alle vragen over test",
          "Alle vragen met als onderwerp test",
          "Alle vragen die gaan over test"
        )
        topic_question should not be empty
        expected_questions should contain(topic_question)
    }
  }
  it should "return a string given a language file and multiple topic filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val topic_filter: List[FilterEntity] = List(FilterEntity("topic", "test"), FilterEntity("topic", "tset"))
        val topic_question: String = nlpService.build(language, topic_filter)
        val expected_questions = List(
          "Geef mij alle vragen over test en tset",
          "Geef mij alle vragen met als onderwerp test en tset",
          "Geef mij alle vragen die gaan over test en tset",
          "Alle vragen over test en tset",
          "Alle vragen met als onderwerp test en tset",
          "Alle vragen die gaan over test en tset"
        )
        topic_question should not be empty
        expected_questions should contain(topic_question)
    }
  }
  it should "return a string given a language file and a author filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val author_filter: List[FilterEntity] = List(FilterEntity("author", "test"))
        val author_question: String = nlpService.build(language, author_filter)
        val expected_questions = List(
          "Geef mij alle vragen gesteld door test",
          "Geef mij alle vragen van test",
          "Alle vragen gesteld door test",
          "Alle vragen van test"
        )
        author_question should not be empty
        expected_questions should contain(author_question)
    }
  }
  it should "return a string given a language file and mulitple author filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val author_filter: List[FilterEntity] = List(FilterEntity("author", "test"), FilterEntity("author", "tset"))
        val author_question: String = nlpService.build(language, author_filter)
        val expected_questions = List(
          "Geef mij alle vragen gesteld door test en tset",
          "Geef mij alle vragen van test en tset",
          "Alle vragen gesteld door test en tset",
          "Alle vragen van test en tset"
        )
        author_question should not be empty
        expected_questions should contain(author_question)
    }
  }
  it should "return a string given a language file and multiple filters" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService = new NLPService("nl", neo4jdao)
    val questions = Source.fromURL(getClass.getResource(s"/questions/nl.json")).getLines.mkString
    decode[Map[String, List[String]]](questions) match {
      case Left(error_msg) => fail(error_msg)
      case Right(language) =>
        val author_filter: List[FilterEntity] = List(
          FilterEntity("party", "partij"),
          FilterEntity("department", "departement"),
          FilterEntity("topic", "onderwerp"),
          FilterEntity("author", "auteur")
        )
        val author_question: String = nlpService.build(language, author_filter)
        val expected_questions = List(
          "Geef mij alle vragen van partij partij gesteld aan de departement over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij gesteld aan de departement over onderwerp van auteur",
          "Geef mij alle vragen van partij partij gesteld aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij gesteld aan de departement met als onderwerp onderwerp van auteur",
          "Geef mij alle vragen van partij partij gesteld aan de departement die gaan over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij gesteld aan de departement die gaan over onderwerp van auteur",
          "Geef mij alle vragen van partij partij gericht aan de departement over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij gericht aan de departement over onderwerp van auteur",
          "Geef mij alle vragen van partij partij gericht aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij gericht aan de departement met als onderwerp onderwerp van auteur",
          "Geef mij alle vragen van partij partij gericht aan de departement die gaan over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij gericht aan de departement die gaan over onderwerp van auteur",
          "Geef mij alle vragen van partij partij voor de departement over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij voor de departement over onderwerp van auteur",
          "Geef mij alle vragen van partij partij voor de departement met als onderwerp onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij voor de departement met als onderwerp onderwerp van auteur",
          "Geef mij alle vragen van partij partij voor de departement die gaan over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij partij voor de departement die gaan over onderwerp van auteur",
          "Geef mij alle vragen van partij gesteld aan de departement over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij gesteld aan de departement over onderwerp van auteur",
          "Geef mij alle vragen van partij gesteld aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij gesteld aan de departement met als onderwerp onderwerp van auteur",
          "Geef mij alle vragen van partij gesteld aan de departement die gaan over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij gesteld aan de departement die gaan over onderwerp van auteur",
          "Geef mij alle vragen van partij gericht aan de departement over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij gericht aan de departement over onderwerp van auteur",
          "Geef mij alle vragen van partij gericht aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij gericht aan de departement met als onderwerp onderwerp van auteur",
          "Geef mij alle vragen van partij gericht aan de departement die gaan over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij gericht aan de departement die gaan over onderwerp van auteur",
          "Geef mij alle vragen van partij voor de departement over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij voor de departement over onderwerp van auteur",
          "Geef mij alle vragen van partij voor de departement met als onderwerp onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij voor de departement met als onderwerp onderwerp van auteur",
          "Geef mij alle vragen van partij voor de departement die gaan over onderwerp gesteld door auteur",
          "Geef mij alle vragen van partij voor de departement die gaan over onderwerp van auteur",
          "Alle vragen van partij partij gesteld aan de departement over onderwerp gesteld door auteur",
          "Alle vragen van partij partij gesteld aan de departement over onderwerp van auteur",
          "Alle vragen van partij partij gesteld aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Alle vragen van partij partij gesteld aan de departement met als onderwerp onderwerp van auteur",
          "Alle vragen van partij partij gesteld aan de departement die gaan over onderwerp gesteld door auteur",
          "Alle vragen van partij partij gesteld aan de departement die gaan over onderwerp van auteur",
          "Alle vragen van partij partij gericht aan de departement over onderwerp gesteld door auteur",
          "Alle vragen van partij partij gericht aan de departement over onderwerp van auteur",
          "Alle vragen van partij partij gericht aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Alle vragen van partij partij gericht aan de departement met als onderwerp onderwerp van auteur",
          "Alle vragen van partij partij gericht aan de departement die gaan over onderwerp gesteld door auteur",
          "Alle vragen van partij partij gericht aan de departement die gaan over onderwerp van auteur",
          "Alle vragen van partij partij voor de departement over onderwerp gesteld door auteur",
          "Alle vragen van partij partij voor de departement over onderwerp van auteur",
          "Alle vragen van partij partij voor de departement met als onderwerp onderwerp gesteld door auteur",
          "Alle vragen van partij partij voor de departement met als onderwerp onderwerp van auteur",
          "Alle vragen van partij partij voor de departement die gaan over onderwerp gesteld door auteur",
          "Alle vragen van partij partij voor de departement die gaan over onderwerp van auteur",
          "Alle vragen van partij gesteld aan de departement over onderwerp gesteld door auteur",
          "Alle vragen van partij gesteld aan de departement over onderwerp van auteur",
          "Alle vragen van partij gesteld aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Alle vragen van partij gesteld aan de departement met als onderwerp onderwerp van auteur",
          "Alle vragen van partij gesteld aan de departement die gaan over onderwerp gesteld door auteur",
          "Alle vragen van partij gesteld aan de departement die gaan over onderwerp van auteur",
          "Alle vragen van partij gericht aan de departement over onderwerp gesteld door auteur",
          "Alle vragen van partij gericht aan de departement over onderwerp van auteur",
          "Alle vragen van partij gericht aan de departement met als onderwerp onderwerp gesteld door auteur",
          "Alle vragen van partij gericht aan de departement met als onderwerp onderwerp van auteur",
          "Alle vragen van partij gericht aan de departement die gaan over onderwerp gesteld door auteur",
          "Alle vragen van partij gericht aan de departement die gaan over onderwerp van auteur",
          "Alle vragen van partij voor de departement over onderwerp gesteld door auteur",
          "Alle vragen van partij voor de departement over onderwerp van auteur",
          "Alle vragen van partij voor de departement met als onderwerp onderwerp gesteld door auteur",
          "Alle vragen van partij voor de departement met als onderwerp onderwerp van auteur",
          "Alle vragen van partij voor de departement die gaan over onderwerp gesteld door auteur",
          "Alle vragen van partij voor de departement die gaan over onderwerp van auteur"
        )
        author_question should not be empty
        expected_questions should contain(author_question)
    }
  }

  behavior of "buildQuestion"
  it should "return an empty string if the language file is invalid" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("invalid").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("invalid").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("invalid", neo4jdao)
    val filters: List[FilterEntity] = List(FilterEntity("author", "test"))
    val result: String = nlpService.buildQuestion(filters)
    result shouldBe empty
  }
  it should "sort the filters based on filter type" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val filters: List[FilterEntity] = List(
      FilterEntity("author", "1"),
      FilterEntity("department", "3"),
      FilterEntity("author", "2")
    )
    val result = nlpService.buildQuestion(filters)
    result should not be empty
    val split_1: Array[String] = result.split('1')
    split_1.head should not be empty
    val split_2: Array[String] = split_1.tail.mkString("").split('2')
    split_2.head should not be empty
    val split_3: Array[String] = split_2.tail.mkString("").split('3')
    split_3.head should not be empty
    split_3.tail.mkString("") shouldBe empty
  }
  it should "return a string given a party filter" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val filters: List[FilterEntity] = List(FilterEntity("party", "test"))
    val result: String = nlpService.buildQuestion(filters)
    val expected_questions: List[String] = List(
      "Geef mij alle vragen van partij test",
      "Geef mij alle vragen van test",
      "Alle vragen van partij test",
      "Alle vragen van test"
    )
    result should not be empty
    expected_questions should contain(result)
  }

  behavior of "containsSubstrIgnoreAccents"
  it should "return false if the value is longer as the string" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    nlpService.containsSubstrIgnoreAccents("test", "longer as test") should not be true
  }
  it should "return false if the value is not found in the string" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    nlpService.containsSubstrIgnoreAccents("this sentence does not contain the value", "test") should not be true
  }
  it should "return true if the value is found in the string" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    nlpService.containsSubstrIgnoreAccents("this sentence does contain test", "test") shouldBe true
    nlpService.containsSubstrIgnoreAccents("this test does contain test", "test") shouldBe true
    nlpService.containsSubstrIgnoreAccents("this sentestnce does contain the value", "test") shouldBe true
    nlpService.containsSubstrIgnoreAccents("Geef mij alle vragen van MR en groen", "mr") shouldBe true
  }
  it should "return true if the value is found in the string disregarding accents" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    nlpService.containsSubstrIgnoreAccents("this sentence does contain tèst", "test") shouldBe true
    nlpService.containsSubstrIgnoreAccents("this sentence does contain tēst", "test") shouldBe true
    nlpService.containsSubstrIgnoreAccents("this sentence does contain tęst", "test") shouldBe true
    nlpService.containsSubstrIgnoreAccents("this sentence does contain tęst", "test") shouldBe true
  }
  it should "return false for when comparing dashes and spaces" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    nlpService.containsSubstrIgnoreAccents("this sentence does contain n-va", "n-va") shouldBe true
    nlpService.containsSubstrIgnoreAccents("this sentence does contain n va", "n-va") shouldBe false
    nlpService.containsSubstrIgnoreAccents("this sentence does contain n-va", "n va") shouldBe false
  }

  behavior of "findEntities"
  it should "return an empty set if the entity set was empty" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result = nlpService.findEntities("these are not the values you're looking for", Entity("authors", List()))
    result shouldBe empty
  }
  it should "return an empty set if the sentence does not contain any value" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result = nlpService.findEntities("these are not the values you're looking for", party_entity)
    result shouldBe empty
  }
  it should "return a non-empty set if the sentence does contain values" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result = nlpService.findEntities("Geef mij alle vragen van MR", party_entity)
    result should not be empty
    result.size shouldBe 1
    result should contain("MR")

    val result_multiple = nlpService.findEntities("Geef mij alle vragen van MR en groen", party_entity)
    result_multiple should not be empty
    result_multiple.size shouldBe 2
    result_multiple should contain("MR")
    result_multiple should contain("Ecolo-Groen")
  }

  behavior of "entitiesFromSentence"
  it should "return an empty set if the all the entity sets were empty" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result = nlpService.entitiesFromSentence("these are not the values you're looking for")
    result("author") shouldBe empty
    result("department") shouldBe empty
    result("topic") shouldBe empty
    result("party") shouldBe empty
  }
  it should "return an empty set if the sentence does not contain any values" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(party_entity).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result = nlpService.entitiesFromSentence("these are not the values you're looking for")
    result("author") shouldBe empty
    result("department") shouldBe empty
    result("topic") shouldBe empty
    result("party") shouldBe empty
  }
  it should "return a non-empty set if the sentence does contain values" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(party_entity).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result = nlpService.entitiesFromSentence("Geef mij alle vragen van MR")
    result("author") shouldBe empty
    result("department") shouldBe empty
    result("topic") shouldBe empty
    result("party") should not be empty
    result("party") should contain("MR")
    result("party").size should be(1)

    val result_multiple = nlpService.entitiesFromSentence("Geef mij alle vragen van mr en groen")
    result_multiple("author") shouldBe empty
    result_multiple("department") shouldBe empty
    result_multiple("topic") shouldBe empty
    result_multiple("party") should not be empty
    result_multiple("party") should contain("MR")
    result_multiple("party") should contain("Ecolo-Groen")
    result_multiple("party").size shouldBe 2

  }

  behavior of "topQuestions"
  it should "return an empty list if there are no filters present in the database" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    (neo4jdao.topFilters _).expects(10, "nl").returns(List()).once

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result: List[Filter] = nlpService.topQuestions(10)
    result shouldBe empty
  }
  it should "return a single filter if there is only one filter in the database" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    val top_filters = List(
      Filter(1, public = true, 1, "", List(FilterEntity("author", "test")))
    )
    (neo4jdao.topFilters _).expects(10, "nl").returns(top_filters).once

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result: List[Filter] = nlpService.topQuestions(10)

    result should not be empty
    result.size should equal(1)
    result.head.count should equal(1)
    result.head.question should not be empty
    result.head.entities should contain(FilterEntity("author", "test"))
  }
  it should "return multiple filters when there are multiple filters in the database" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    val top_filters = List(
      Filter(1, public = true, 1, "", List(FilterEntity("author", "auteur"))),
      Filter(1, public = true, 2, "", List(FilterEntity("author", "auteur"), FilterEntity("department", "departement"))),
      Filter(1, public = true, 3, "", List(FilterEntity("author", "auteur"), FilterEntity("topic", "onderwerp"), FilterEntity("party", "partij")))
    )
    (neo4jdao.topFilters _).expects(10, "nl").returns(top_filters).once

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result: List[Filter] = nlpService.topQuestions(10)
    result should not be empty
    result.size should equal(3)

    result.head.count should equal(1)
    result.tail.head.count should equal(2)
    result.takeRight(1).head.count should equal(3)

    result.head.question should not be empty
    result.tail.head.question should not be empty
    result.takeRight(1).head.question should not be empty

    result.head.entities should equal(List(FilterEntity("author", "auteur")))
    result.tail.head.entities should equal(List(
      FilterEntity("author", "auteur"),
      FilterEntity("department", "departement")
    ))
    result.takeRight(1).head.entities should equal(List(
      FilterEntity("author", "auteur"),
      FilterEntity("topic", "onderwerp"),
      FilterEntity("party", "partij")
    ))
  }

  behavior of "allQuestions"
  it should "return an empty list if there are no filters present in the database" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    (neo4jdao.allFilters _).expects("nl").returns(List()).once

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result: List[Filter] = nlpService.allQuestions
    result shouldBe empty
  }
  it should "return a single filter if there is only one filter in the database" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    val top_filters = List(
      Filter(1, public = true, 1, "", List(FilterEntity("author", "test")))
    )
    (neo4jdao.allFilters _).expects("nl").returns(top_filters).once

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result: List[Filter] = nlpService.allQuestions

    result should not be empty
    result.size should equal(1)
    result.head.count should equal(1)
    result.head.question should not be empty
    result.head.entities should contain(FilterEntity("author", "test"))
  }
  it should "return multiple filters when there are multiple filters in the database" in {
    val neo4jdao = mock[ParliamentaryQuestionNeo4jDAO]
    (neo4jdao.authors _).expects().returns(Entity("authors", List())).once()
    (neo4jdao.parties _).expects().returns(Entity("parties", List())).once()
    (neo4jdao.departments _).expects("nl").returns(Entity("departments", List())).once()
    (neo4jdao.topics _).expects("nl").returns(Entity("topics", List())).once()
    val top_filters = List(
      Filter(1, public = true, 1, "", List(FilterEntity("author", "auteur"))),
      Filter(1, public = true, 2, "", List(FilterEntity("author", "auteur"), FilterEntity("department", "departement"))),
      Filter(1, public = true, 3, "", List(FilterEntity("author", "auteur"), FilterEntity("topic", "onderwerp"), FilterEntity("party", "partij")))
    )
    (neo4jdao.allFilters _).expects("nl").returns(top_filters).once

    val nlpService: NLPService = new NLPService("nl", neo4jdao)
    val result: List[Filter] = nlpService.allQuestions
    result should not be empty
    result.size should equal(3)

    result.head.count should equal(1)
    result.tail.head.count should equal(2)
    result.takeRight(1).head.count should equal(3)

    result.head.question should not be empty
    result.tail.head.question should not be empty
    result.takeRight(1).head.question should not be empty

    result.head.entities should equal(List(FilterEntity("author", "auteur")))
    result.tail.head.entities should equal(List(
      FilterEntity("author", "auteur"),
      FilterEntity("department", "departement")
    ))
    result.takeRight(1).head.entities should equal(List(
      FilterEntity("author", "auteur"),
      FilterEntity("topic", "onderwerp"),
      FilterEntity("party", "partij")
    ))
  }
}
