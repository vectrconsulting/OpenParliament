package consulting.vectr.service

import com.twitter.inject.Logging
import consulting.vectr.dao.ParliamentaryQuestionFileDAO
import javax.inject.Inject
import consulting.vectr.dao.ParliamentaryQuestionNeo4jDAO

class DataService @Inject() (neodao: ParliamentaryQuestionNeo4jDAO,filedao: ParliamentaryQuestionFileDAO) extends Logging{
  
  def getDataFromFileAndInserInNeo4j() = {   
    neodao.storePQuestions(filedao.getAllPQuestions())
  }
  
  def getAllParliamentaryQuestions() = {
    neodao.getAllPQuestions()
  }
  
  
}