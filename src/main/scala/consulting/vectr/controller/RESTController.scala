package consulting.vectr.controller

import javax.inject.Inject

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import consulting.vectr.service.DataService

import scala.language.postfixOps

class RESTController @Inject()(service: DataService) extends Controller {
  get("/pq") { request: Request =>
    service.getAllParliamentaryQuestions(request.getParam("lang"))
  }

  get("/loadpq") { request: Request =>
    service.getDataFromWebAndInsertInNeo4j()
  }

  get("/loadfiles") { request: Request =>
    service.getDataFromFilesAndInsertInNeo4j()
  }

  get("/questionfilter") { request: Request => {
      val entities = service.getResolvedEntitiesAndSaveToNeo4j(request.getParam("q"))
      entities
    }

  }

  get("/topquestions") { request: Request =>
    service.getTopQuestionsFromNeo4j(request.getParam("top", "5").toInt,request.getParam("lang","nl"))
  }

}
