package consulting.vectr.dao

import javax.inject.Inject

import com.twitter.inject.Logging
import consulting.vectr.model.{ParliamentaryQuestionSummary, ParliamentaryQuestionWeb, WitAIEntity}
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.exceptions.ClientException

class ParliamentaryQuestionNeo4jDAO @Inject()(driver: Driver) extends Logging{

  import scala.collection.JavaConverters._

  def storePQuestionWeb(pq: ParliamentaryQuestionWeb): Unit = {
    val session = driver.session()
    try {
      val cypher =
        """
        MERGE (a:author {name:{name}})
        MERGE (d:department {id:{dep_id}, name_nl:{dep_name_nl}, name_fr:{dep_name_fr}})
        MERGE (p:party {name:{parname}})
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
          "dep_id" -> pq.department_number.toString,
          "dep_name_nl" -> pq.department_name_nl,
          "dep_name_fr" -> pq.department_name_fr,
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
            "date" -> (date.substring(0,4)+"-"+date.substring(4,6)+"-"+date.substring(6,8))
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
            "question_nl" -> question_nl
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
            "question_fr" -> question_fr
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
            "answer_nl" -> answer_nl
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
            "answer_fr" -> answer_fr
          ).asJava)
      }

      pq.subject_nl.foreach { topic =>
        val topicCypher = """
        MERGE (q:question {id:{questionid}})
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

  def getAllPQuestions(lang: String="nl"): List[ParliamentaryQuestionSummary] = {
    val session = driver.session()
    try {
      val cypher_nl =
        """
        MATCH (p:party)<-[:IS_MEMBER_OF]-(a:author)-[:ASKED]->(q:question)-[:IS_ABOUT]->(t:topic),
        (q)-[:ASKED_TO]->(d:department),(q)-[:ASKED_FOR]->(p)
        WHERE q.status = "answerReceived" AND t.name_nl <> ""
        RETURN a.name as author, p.name as party, t.name_nl as topic, d.name_nl as department, q.date as date
        """
      val cypher_fr =
        """
        MATCH (p:party)<-[:IS_MEMBER_OF]-(a:author)-[:ASKED]->(q:question)-[:IS_ABOUT]->(t:topic),
        (q)-[:ASKED_TO]->(d:department), (q)-[:ASKED_FOR]->(p)
        WHERE q.status = "answerReceived" AND t.name_nl <> ""
        RETURN a.name as author, p.name as party, t.name_fr as topic, d.name_fr as department, q.date as date
        """


      session.run(if (lang == "nl")cypher_nl else cypher_fr)
        .asScala
        .toList
        .map(rec => ParliamentaryQuestionSummary(
          rec.get("author").asString(),
          rec.get("party").asString(),
          rec.get("topic").asString(),
          rec.get("department").asString(),
          rec.get("date").asString(),
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

  def hasSDOCNAME(sdocname: String): Boolean = {
    val session = driver.session()
    try {
      val cypher = """MATCH (q:question{sdocname:{id}}) RETURN count(q) as count"""
      val result = session.run(cypher, Map[String, Object](
        "id" -> sdocname
      ).asJava)
      result.single().get("count").asInt() > 0
    } finally {
      session.close()
    }
  }

  def storeFilters(entities: Map[String, Set[String]]) = {
    val lang = "nl"

    val name_w_language = "name_" + lang

    val session = driver.session()
    try {
      //some nodes have name, some have name_nl/name_fr
      val attributes: Map[String, String] = Map("author"->"name",
                                                "department"->name_w_language ,
                                                "party"->"name",
                                                "topic"->name_w_language)
      var nodes = List[Tuple3[Int, String, String]]()

      var i: Int = 0
      entities.keys.foreach(key =>
        entities(key).foreach(entity => {
          nodes = nodes ++ List(new Tuple3(i, key, entity))
          i+= 1
        })
      )

      // the pattern must have <matches> relationships, no more, no less
      val matches = nodes.size
      if (matches > 0) {
        var pattern = "(f:filter)"
        nodes.foreach( node => {
          val (i, key, entity) = node
          pattern += ", (x_" + i.toString() + ":" + key + " {" + attributes(key) + ":'" + entity + "'})<-[:W_" + key.toUpperCase() + "]-(f) "
        })

        var cypher = "MATCH " + pattern +
                      """
                       OPTIONAL MATCH (f)-[r]-()
                        WITH f, count(r) as rels
                      """
        cypher += "WHERE rels = " + matches.toString + " RETURN ID(f) as id"

        debug(cypher)
        //returns an id if the pattern is found, or nothing
        val result = session.run(cypher)
          .asScala
          .toList

        if (result.size == 1) {
          //if the pattern is found, set count +1
          val id = result.head.get("id")
          val increase_count = "MATCH (f:filter) WHERE ID(f) = " + id.toString + " SET f.count = f.count + 1"
          debug(increase_count)
          session.run(increase_count)
        } else {
          if (result.isEmpty) {
            //if the pattern is not found, create it and set the count to 1
            //first match the existing nodes
            var create_filter = "MATCH "
            nodes.foreach( node => {
              val (i, key, entity) = node
              if (i > 0)
                create_filter += ", "
              create_filter += " (x_" + i.toString() + ":" + key + " {" + attributes(key) + ":'" + entity + "'})"
            })

            //create a filter with count one
            create_filter += " CREATE (f:filter {count:1}) "
            i = 0
            //merge its relationships with the existing nodes
            nodes.foreach( node => {
              val (i, key, entity) = node
              create_filter += " MERGE (x_" + i.toString() + ")<-[:W_" + key.toUpperCase() + "]-(f) "
            })

            debug(create_filter)
            session.run(create_filter)
          } else {
            //more patterns should never be found
            error(result)
            error("Multiple patterns should never be found!")
          }
        }
      }

    } finally {
      session.close()
    }

  }

  def getTopFilters(top: Int): List[Tuple2[Int, List[Tuple2[String, String]]]] = {
    val lang = "nl"

    val name_w_language = "name_" + lang

    val attributes: Map[String, String] = Map("author"->"name",
      "department"->name_w_language ,
      "party"->"name",
      "topic"->name_w_language)

    val session = driver.session()
    val cypher = "MATCH (f:filter)--(x) WITH f , collect(DISTINCT x) AS entities WHERE NOT f.count IS NULL WITH f, entities, EXTRACT(x IN entities | labels(x)[0]) AS node_labels RETURN f.count AS c, node_labels, entities ORDER BY c DESC LIMIT " + top.toString

    val result = session.run(cypher)
      .asScala
      .toList

    var mostUsedFilters =  List[Tuple2[Int, List[Tuple2[String, String]]]]()

    result.foreach( element => {
      val entities = element.get("entities")
      val labels = element.get("node_labels")
      var filters = List[Tuple2[String, String]]()
      if (entities.size == labels.size) {
        for (i <- 0 until entities.size) {
          val label: String = labels.get(i).asString()
          val entity: String = entities.get(i).get(attributes(label)).asString()
          filters =  filters :+ (label, entity)
        }
      }
      val count: Int = element.get("c").asInt()
      mostUsedFilters = mostUsedFilters :+ (count, filters)
    })
    session.close()
    mostUsedFilters
  }

  def getAllSDOCNAMES: Set[String] = {
    val cypher =
      """
        MATCH (q:question) RETURN q.sdocname as sdocname
      """

    val session = driver.session()
    val result = session.run(cypher).asScala.toList
    session.close()
    result.map( element => element.get("sdocname").asString()).toSet
  }

}
