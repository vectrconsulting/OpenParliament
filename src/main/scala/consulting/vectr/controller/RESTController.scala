package consulting.vectr.controller

import javax.inject.Inject

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import consulting.vectr.service.DataService

import scala.language.postfixOps

class RESTController @Inject()(service: DataService) extends Controller {
  get("/questions") { request: Request =>
    service.allParliamentaryQuestions(request.getParam("lang"))
  }

  get("/paths") { request: Request =>
    service.AllParliamentaryPaths(request.getParam("lang"))
  }

  get("/pqsmall") { request: Request =>
    service.AllParliamentaryPaths(request.getParam("lang"))
  }

  post("/loadpq") { request: Request =>
    service.dataFromWebAndInsertInNeo4j()
  }

  post("/loadfiles") { request: Request =>
    service.dataFromFilesAndInsertInNeo4j()
  }

  get("/questionfilter") { request: Request =>
    service.resolvedEntitiesAndSaveToNeo4j(request.getParam("q"), request.getParam("lang", "nl"))
  }

  get("/topquestions") { request: Request =>
    service.topQuestionsFromNeo4j(request.getParam("top", "5").toInt, request.getParam("lang", "nl"))
  }

  get("/allfilters") { request: Request =>
    service.allQuestionFromNeo4j(request.getParam("lang", "nl"))
  }

  post("/updateFilter") { request: Request =>
    service.updateFilterInNeo4j(request.getParam("id").toInt, request.getParam("public", "false").toBoolean)
  }

  post("/removeFilter") { request: Request =>
    service.removeFilterInNeo4j(request.getParam("id").toInt)
  }

}
