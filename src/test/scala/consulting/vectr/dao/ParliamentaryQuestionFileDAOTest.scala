package consulting.vectr.dao

import com.twitter.inject.Test
import ammonite.ops.pwd
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory

class ParliamentaryQuestionFileDAOTest extends Test {

  test("A directory with 2 files containing parliamentary questions should only return with status answered") {
    
    val cacheFolder = (pwd/'src/'test/'resources/'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=${cacheFolder}")
    val repo = new ParliamentaryQuestionFileDAO(conf)
    println(repo.getAllPQuestions().mkString("\n"))
  }
}
