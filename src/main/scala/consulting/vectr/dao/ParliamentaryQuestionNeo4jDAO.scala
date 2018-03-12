package consulting.vectr.dao

import java.util.UUID
import javax.inject.Inject

import com.twitter.inject.Logging
import consulting.vectr.model._
import org.neo4j.driver.v1.exceptions.ClientException
import org.neo4j.driver.v1.{Driver, Record, Values}

class ParliamentaryQuestionNeo4jDAO @Inject()(driver: Driver) extends Logging {

  import scala.collection.JavaConverters._

  def storePQuestionWeb(pq: ParliamentaryQuestionWeb): Unit = {
    val session = driver.session()
    try {
      val cypher =
        s"""
        MATCH (d:department) WHERE LOWER(d.name__nl) = ${'"'}${pq.department_name_nl.toLowerCase}${'"'}
        MERGE (p:party {name:{parname}, name_nl:{parname}, name_fr:{parname}})
        MERGE (a:author {name:{name}, name_nl:{name}, name_fr:{name}})
        MERGE (q:question {id:{questionid}, status:{status},
        title_nl:{title_nl}, title_fr:{title_fr}, original_lang:{original_lang}, sdocname:{sdocname}})

        MERGE (a)-[:IS_MEMBER_OF]->(p)
        MERGE (a)-[:ASKED]->(q)
        MERGE (q)-[:ASKED_TO]->(d)
        MERGE (q)-[:ASKED_FOR]->(p)
        return *
        """

      session.run(cypher,
        Map[String, Object](
          "name" -> pq.author,
          "dep_name_nl" -> pq.department_name_nl,
          "parname" -> pq.author_party,
          "questionid" -> pq.document_id.toString,
          "status" -> pq.status,
          "title_nl" -> pq.title_nl,
          "title_fr" -> pq.title_fr,
          "original_lang" -> pq.language,
          "sdocname" -> pq.sdocname
        ).asJava)

      pq.document_date.foreach { date =>
        val date_cypher =
          """
            MATCH(q:question {id:{question_id}})
            SET q.date = {date}
          """
        session.run(date_cypher,
          Map[String, Object](
            "question_id" -> pq.document_id,
            "date" -> (date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8))
          ).asJava)
      }
      pq.question_text_nl.foreach { question_nl =>
        val title_nl_cypher =
          """
            MATCH(q:question {id:{question_id}})
            SET q.question_nl = {question_nl}
          """
        session.run(title_nl_cypher,
          Map[String, Object](
            "question_id" -> pq.document_id,
            "question_nl" -> question_nl.trim
          ).asJava)
      }
      pq.question_text_fr.foreach { question_fr =>
        val title_fr_cypher =
          """
            MATCH(q:question {id:{question_id}})
            SET q.question_fr = {question_fr}
          """
        session.run(title_fr_cypher,
          Map[String, Object](
            "question_id" -> pq.document_id,
            "question_fr" -> question_fr.trim
          ).asJava)
      }
      pq.answer_text_nl.foreach { answer_nl =>
        val title_fr_cypher =
          """
            MATCH(q:question {id:{question_id}})
            SET q.answer_nl = {answer_nl}
          """
        session.run(title_fr_cypher,
          Map[String, Object](
            "question_id" -> pq.document_id,
            "answer_nl" -> answer_nl.trim
          ).asJava)
      }
      pq.answer_text_fr.foreach { answer_fr =>
        val title_fr_cypher =
          """
            MATCH(q:question {id:{question_id}})
            SET q.answer_fr = {answer_fr}
          """
        session.run(title_fr_cypher,
          Map[String, Object](
            "question_id" -> pq.document_id,
            "answer_fr" -> answer_fr.trim
          ).asJava)
      }
      pq.subject_nl.foreach { topic =>
        val topicCypher =
          """
        MATCH (q:question {id:{questionid}})
        MERGE (t:topic {name_nl:{topicname_nl}, name_fr:{topicname_fr}})
        MERGE (q)-[:IS_ABOUT]->(t)
        return *
        """
        session.run(topicCypher,
          Map[String, Object](
            "questionid" -> pq.document_id.toString,
            "topicname_nl" -> topic.trim.toLowerCase.capitalize,
            "topicname_fr" -> pq.subject_fr.getOrElse("").trim.toLowerCase.capitalize
          ).asJava)

      }
    } catch {
      case client: ClientException =>
        client.printStackTrace()
        throw client
      case x: Throwable =>
        x.printStackTrace()
        throw x
    } finally {
      session.close()
    }
  }

  def allPQuestions(lang: String): List[ParliamentaryQuestionSummary] = {
    val session = driver.session()
    try {
      val cypher =
        s"""
        MATCH (p:party)<-[:IS_MEMBER_OF]-(a:author)-[:ASKED]->(q:question)-[:IS_ABOUT]->(t:topic),
        (q)-[:ASKED_TO]->(d:department),(q)-[:ASKED_FOR]->(p)
        WHERE q.status = "answerReceived" AND t.name_$lang <> ""
        RETURN a.name as author,
        p.name as party,
        t.name_$lang as topic,
        d.name__$lang as department_long,
        d.name_$lang as department,
        q.date as date,
        q.title_$lang as title,
        q.question_$lang as question,
        q.answer_$lang as answer
        """
      session.run(cypher)
        .asScala
        .toList
        .map(rec => ParliamentaryQuestionSummary(
          rec.get("author").asString(),
          rec.get("party").asString(),
          rec.get("topic").asString(),
          rec.get("department_long").asString(),
          rec.get("department").asString(),
          rec.get("date").asString(),
          rec.get("title").asString(),
          rec.get("question").asString(),
          rec.get("answer").asString(),
          1)
        )
    } catch {
      case client: ClientException =>
        client.printStackTrace()
        throw client
      case x: Throwable =>
        x.printStackTrace()
        throw x
    } finally {
      session.close()
    }

  }

  def allPaths(lang: String): List[ParliamentaryQuestionSmallSummary] = {
    val session = driver.session()
    try {
      val cypher =
        s"""
           MATCH (p:party)<-[:IS_MEMBER_OF]-(a:author)-[:ASKED]->(q:question)-[:IS_ABOUT]->(t:topic),
           (q)-[:ASKED_TO]->(d:department),(q)-[:ASKED_FOR]->(p)
           WHERE q.status = "answerReceived" AND t.name_$lang <> ""
           RETURN a.name as author,
           p.name as party,
           t.name_$lang as topic,
           d.name__$lang as department_long,
           d.name_$lang as department,
           q.date as date
         """

      session.run(cypher)
        .asScala
        .toList
        .map(rec => ParliamentaryQuestionSmallSummary(
          rec.get("author").asString(),
          rec.get("party").asString(),
          rec.get("topic").asString(),
          rec.get("department_long").asString(),
          rec.get("department").asString(),
          rec.get("date").asString(),
          1
        ))

    } catch {
      case client: ClientException =>
        client.printStackTrace()
        throw client
      case x: Throwable =>
        x.printStackTrace()
        throw x
    } finally {
      session.close()
    }
  }

  def allSDOCNAMES: Set[String] = {
    val cypher =
      """
        MATCH (q:question) RETURN q.sdocname as sdocname
      """

    val session = driver.session()
    val result = session.run(cypher).asScala.toList
    session.close()
    result.map { element => element.get("sdocname").asString() }.toSet
  }

  def storeFilters(entities: Map[String, Set[String]], lang: String): Unit = {
    val session = driver.session()
    try {
      val attributes: Map[String, String] = Map(//some nodes have name, some have name_nl/name_fr
        "author" -> "name",
        "department" -> s"name_$lang",
        "party" -> "name",
        "topic" -> s"name_$lang"
      )

      val nodes: Seq[(String, String)] = entities.toList.flatMap {
        case (key, set) => set.map { entity => (key, entity) }
      }

      if (nodes.nonEmpty) {
        val pattern = nodes.map {
          case (key, entity) => s"(:$key{${attributes(key)}:${'"'}$entity${'"'}})<-[:W_${key.toUpperCase}]-(f)"
        }.mkString(", ")

        val cypher = // match against all relations in the pattern but no more than listed
          s"""
                MATCH (f:filter), $pattern
                OPTIONAL MATCH (f)-[r]-()
                WITH f, count(r) as rels
                WHERE rels = ${nodes.size.toString} RETURN ID(f) as id
            """
        val result = session.run(cypher)
          .asScala
          .toList

        if (result.size == 1) {
          val id = result.head.get("id").asInt()
          val increase_count = s"MATCH (f:filter) WHERE ID(f) = $id SET f.count = f.count + 1"
          session.run(increase_count)
        } else if (result.isEmpty) {
          val items = nodes.map {
            case (key, entity) =>
              val uuid = UUID.randomUUID().toString
              (s"(`$uuid`:$key{${attributes(key)}:${'"'}$entity${'"'}})", s"MERGE (`$uuid`)<-[:W_${key.toUpperCase}]-(f)")
          }

          val relations =
            s"""
               MATCH ${items.map { item => item._1 }.mkString(", ")}
               CREATE (f:filter {count:1, public:toBoolean(false)})
               ${items.map { item => item._2 }.mkString("\n")}
             """
          session.run(relations, Map[String, Object]("public" -> false.toString).asJava)
        } else {
          //more patterns should never be found
          error(result)
          error("Multiple patterns should never be found!")
        }
      }
    } catch {
      case x: Throwable =>
        error(x.getMessage)
        throw x
    }
    finally {
      session.close()
    }

  }

  def updateFilter(id: Int, public: Boolean): Unit = {
    info(s"updating filter with id $id to $public")
    val session = driver.session()
    val cypher = s"MATCH (f:filter) WHERE ID(f) = $id SET f.public = toBoolean($public)"
    session.run(cypher)
    session.close()
  }

  def removeFilter(id: Int): Unit = {
    val session = driver.session()
    val cypher = s"MATCH (f:filter) WHERE ID(f) = $id DETACH DELETE f"
    session.run(cypher)
    session.close()
  }

  def topFilters(top: Int, lang: String): List[Filter] = {
    val session = driver.session()
    val cypher =
      s"""
        MATCH (f:filter {public:toBoolean(true)})--(x)
        WITH f , collect(DISTINCT x) AS entities
        WHERE NOT f.count IS NULL
        WITH ID(f) as id, f.public AS public, f.count AS c, EXTRACT(x in entities| x.name_$lang) as entity_names, EXTRACT(x IN entities | labels(x)[0]) AS node_labels
        RETURN id, public, c, node_labels, entity_names
        ORDER BY c DESC LIMIT $top
       """
    val result: List[Record] = session.run(cypher).asScala.toList
    val mostUsedFilters: List[Filter] = result.map(record => {
      val id = record.get("id").asInt()
      val public = record.get("public").asBoolean()
      val count = record.get("c").asInt()
      val labels: List[String] = record.get("node_labels").asList(Values.ofString).asScala.toList
      val entities: List[String] = record.get("entity_names").asList(Values.ofString).asScala.toList
      val zipped = labels.zip(entities).map(d => FilterEntity(d._1, d._2))
      Filter(id, public, count, "", zipped)
    })
    session.close()
    mostUsedFilters
  }

  def allFilters(lang: String): List[Filter] = {
    val session = driver.session()
    val cypher =
      s"""
        MATCH (f:filter)--(x)
        WITH f , collect(DISTINCT x) AS entities
        WHERE NOT f.count IS NULL
        WITH ID(f) as id, toBoolean(f.public) AS public, f.count AS c, EXTRACT(x in entities| x.name_$lang) as entity_names, EXTRACT(x IN entities | labels(x)[0]) AS node_labels
        RETURN id, public, c, node_labels, entity_names
        ORDER BY c DESC
      """
    val result: List[Record] = session.run(cypher).asScala.toList
    val filters: List[Filter] = result.map(record => {
      val id = record.get("id").asInt()
      val public = record.get("public").asBoolean()
      val count = record.get("c").asInt()
      val labels: List[String] = record.get("node_labels").asList(Values.ofString).asScala.toList
      val entities: List[String] = record.get("entity_names").asList(Values.ofString).asScala.toList
      val zipped = labels.zip(entities).map(d => FilterEntity(d._1, d._2))
      Filter(id, public, count, "", zipped)
    })
    session.close()
    filters
  }

  def authors: Entity = {
    val cypher = "MATCH (q:author) RETURN q.name as name"
    val session = driver.session()
    val result = session.run(cypher).asScala.toList
    session.close()
    val names = result.map(element => element.get("name").asString())
    val entityValues: List[EntityValue] = names.map(name => EntityValue(
      value = name,
      synonyms = List(name, name.toLowerCase, name.split(' ').tail.mkString(" "))
    ))
    Entity("author", entityValues)
  }

  def departments(lang: String): Entity = {
    val cypher = s"MATCH (q:department) RETURN q.name__$lang as name, q.name_$lang as person"
    val session = driver.session()
    val result = session.run(cypher).asScala.toList
    session.close()
    val names = result.map(element => (element.get("name").asString(), element.get("person").asString()))

    def getSynonymsDutch(name: String): List[String] = name match {
      case "eerste minister" => List(name, "premier")
      case _ => name.split(' ').head match {
        case "vice-eersteminister" =>
          "vice-eersteminister" :: getSynonymsDutchInner(name.replace("vice-eersteminister en ", ""))
        case _ =>
          getSynonymsDutchInner(name)

      }
    }

    def getSynonymsDutchInner(name: String): List[String] = {
      val title = name.split(' ').head
      val without_toegevoegd = name.split(",toegevoegd").head
      // remove title, 'van'/'voor' and split on ,
      val powers = without_toegevoegd.replace(title, "").trim.split(' ').tail.mkString(" ").split(", ")
      // split last 2 on 'en'
      val powers_split_on_and: List[String] = powers.dropRight(1).toList ++ powers.takeRight(1).head.split(" en ").toList
      powers_split_on_and.flatMap(power => List(
        title.toLowerCase.trim + " van " + power.toLowerCase.trim,
        title.toLowerCase.trim + " voor " + power.toLowerCase.trim
      ))
    }

    def getSynonymsFrench(name: String): List[String] = name match {
      case "premier ministre" => List(name, "premier")
      case _ => name.split(' ').head match {
        case "vice-premier" =>
          "vice-premier ministre" :: getSynonymsFrenchInner(name.replace("vice-premier ministre et ", ""))
        case _ =>
          getSynonymsFrenchInner(name)

      }
    }

    def getSynonymsFrenchInner(name: String): List[String] = {
      val title = if (name.split(' ').head == "ministre") "ministre" else "secrétaire d'etat"
      val powers = name.replace(title, "").trim.split(", ")
      val powers_split_on_and: List[String] = powers.dropRight(1).toList ++ powers.takeRight(1).head.split("et ").toList
      powers_split_on_and.map(power => title.toLowerCase.trim + " " + power.toLowerCase.trim)
    }

    val entityValues: List[EntityValue] = names.map(name => EntityValue(
      value = name._2,
      synonyms = lang match {
        case "nl" => getSynonymsDutch(name._1.toLowerCase) ++ List(name._2.toLowerCase())
        case "fr" => getSynonymsFrench(name._1.toLowerCase) ++ List(name._2.toLowerCase())
      }
    ))

    Entity("department", entityValues)
  }

  def topics(lang: String): Entity = {
    val cypher = s"MATCH (q:topic) WHERE q.name_$lang <> ${'"'}${'"'} RETURN q.name_$lang as name"
    val session = driver.session()
    val result = session.run(cypher).asScala.toList
    session.close()
    val names = result.map(element => element.get("name").asString())
    Entity("topic", names.map(name => EntityValue(name, List(name.toLowerCase))))
  }

  def parties: Entity = {
    val cypher = "MATCH (q:party) RETURN q.name as name, q.synonyms as synonyms"
    val session = driver.session()
    val result = session.run(cypher).asScala.toList
    session.close()
    val entityValues = result.map(element => EntityValue(
      element.get("name").asString(),
      element.get("synonyms").asString().split(',').toList.map(_.trim)
    ))
    Entity("party", entityValues)
  }
}
