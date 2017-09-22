package consulting.vectr.dao

import scala.xml.XML

import ammonite.ops.Path
import ammonite.ops.ls
import consulting.vectr.model.ParliamentaryQuestion
import org.neo4j.driver.v1.Driver
import javax.inject.Inject
import org.neo4j.driver.v1.exceptions.ClientException
import consulting.vectr.model.ParliamentaryQuestionSummary

class ParliamentaryQuestionNeo4jDAO @Inject() (driver: Driver) {
  import scala.collection.JavaConverters._

  def storePQuestions(list: List[ParliamentaryQuestion]): Unit = {
    for (
      pq <- list
    ) { storePQuestion(pq) }
  }

  def storePQuestion(pq: ParliamentaryQuestion): Unit = {
    val session = driver.session()
    try {
      val cypher = """
        MERGE (a:author {name:{name}})
        MERGE (d:department {name:{depname}})
        MERGE (p:party {name:{parname}})
        MERGE (q:question {id:{questionid}})
        MERGE (a)-[:IS_MEMBER_OF]->(p)
        MERGE (a)-[:ASKED]->(q)
        MERGE (a)-[:ASKED_TO]->(d)
        MERGE (q)-[:ASKED_FOR]->(p)
        return *      
        """
      session.run(cypher,
        Map[String, Object](
            "name" -> pq.author,
            "depname"->pq.department,
            "parname"->pq.party,
            "questionid"->pq.questionId).asJava)
      pq.topicNL.foreach { topic =>
        val topicCypher = """
        MERGE (q:question {id:{questionid}})
        MERGE (t:topic {name:{topicname}})
        MERGE (q)-[:IS_ABOUT]->(t)
        return *      
        """
      session.run(topicCypher,
        Map[String, Object](
            "questionid" -> pq.questionId,
            "topicname"-> topic).asJava)
        
      }
    } catch {
      case client: ClientException => {
        client.printStackTrace()
        throw client
      }
      case x: Throwable => {
        x.printStackTrace()
        throw x
      }
    } finally {
      session.close
    }

  }
  
  def getAllPQuestions(): List[ParliamentaryQuestionSummary] = {
    val session = driver.session()
    try {
      val cypher = """
        MATCH (p:party)<-[:IS_MEMBER_OF]-(a:author)-[:ASKED]->(q:question)-[:IS_ABOUT]->(t:topic) WHERE (q)-[:ASKED_FOR]->(p) return a.name as author, p.name as party, t.name as topic, count(q) as count      
        """
      val result = session.run(cypher).asScala.toList
      result.map(rec => ParliamentaryQuestionSummary(
          rec.get("author").asString(),
          rec.get("party").asString(),
          rec.get("topic").asString(),
          rec.get("count").asInt()))
      
    } catch {
      case client: ClientException => {
        client.printStackTrace()
        throw client
      }
      case x: Throwable => {
        x.printStackTrace()
        throw x
      }
    } finally {
      session.close
    }
    
  }

}