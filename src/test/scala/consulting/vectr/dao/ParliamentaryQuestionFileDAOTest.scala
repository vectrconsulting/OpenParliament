package consulting.vectr.dao

import com.twitter.inject.Test
import ammonite.ops.{Path, ls, mkdir, pwd, rm}
import com.twitter.util.Await
import com.typesafe.config.Config
import com.typesafe.config.ConfigFactory
import org.scalatest.{FlatSpec, Matchers}

class ParliamentaryQuestionFileDAOTest extends FlatSpec with Matchers {
  behavior of "writePQuestion"
  it should "write a json file to the directory" in {
    rm !  pwd / 'src / 'test / 'resources / 'writePQ
    mkdir !  pwd / 'src / 'test / 'resources / 'writePQ

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'writePQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    filedao.writePQuestion("test", "test")

    (ls ! pwd / 'src / 'test / 'resources / 'writePQ).toList
      .map(path => path.toString.split("/").last)
      .contains("test.json") shouldBe true

    rm !  pwd / 'src / 'test / 'resources / 'writePQ
    mkdir !  pwd / 'src / 'test / 'resources / 'writePQ
  }
  it should "thow an exception if the directory doesn't exist" in {
    val cacheFolder = "/this/is/not/a/valid/path"
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    assertThrows[Throwable]{
      filedao.writePQuestion("test", "test")
    }
  }

  behavior of "loadPQuestions"
  it should "return no PQuestions if the directory is empty" in {
    rm !  pwd / 'src / 'test / 'resources / 'emptyPQ
    mkdir ! pwd / 'src / 'test / 'resources / 'emptyPQ

    val cacheFolder = (pwd / 'src / 'test / 'resources / 'emptyPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val result = filedao.loadPQuestions()

    Await.result[Boolean](result.isEmpty) shouldBe true
  }
  it should "return no PQuestion if the directory contains no json files" in {
    val cacheFolder = (pwd / 'src / 'test / 'resources / 'testfiles).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val result = filedao.loadPQuestions()

    Await.result[Boolean](result.isEmpty) shouldBe true
  }
  it should "return no PQuestion if the directory contains no valid json files" in {
    val cacheFolder = (pwd / 'src / 'test / 'resources / 'faultyPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val result = filedao.loadPQuestions()

    Await.result[Boolean](result.isEmpty) shouldBe true
  }
  it should "return PQuestions if the directory isn't empty" in {
    val cacheFolder = (pwd / 'src / 'test / 'resources / 'loadPQ).toString()
    val conf = ConfigFactory.parseString(s"cachedirectory=$cacheFolder")
    val filedao = new ParliamentaryQuestionFileDAO(conf)

    val result = filedao.loadPQuestions()

    Await.result[Boolean](result.isEmpty) shouldBe false
  }

}
