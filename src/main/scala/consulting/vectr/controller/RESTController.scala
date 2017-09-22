package consulting.vectr.controller

import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.FileResolver
import com.twitter.util.Duration

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}
import consulting.vectr.service.DataService

class RESTController @Inject() (service: DataService) extends Controller {

  get("/pq") { request: Request =>
    service.getAllParliamentaryQuestions()
  }
  
  get("/loadpq") { request: Request =>
    service.getDataFromFileAndInserInNeo4j()
  }

}
