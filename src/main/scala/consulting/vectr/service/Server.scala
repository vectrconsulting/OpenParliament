package consulting.vectr.service


import com.google.inject.Module
import com.twitter.finagle.Http.Server
import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, ExceptionMappingFilter, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import com.twitter.util.Duration
import consulting.vectr.controller.AppController
import consulting.vectr.controller.RESTController
import consulting.vectr.modules._

object ServerMain extends OpenDataServer

class OpenDataServer extends HttpServer {
  override protected val disableAdminHttpServer: Boolean = true

  override val modules = Seq(TypesafeConfigModule,EmbeddedNeo4jModule)

  override def configureHttp(router: HttpRouter) {
    router
      .filter[LoggingMDCFilter[Request, Response]]
      .filter[TraceIdMDCFilter[Request, Response]]
      .filter[CommonFilters]
      .filter[ExceptionMappingFilter[Request]]
      .add[AppController]
      .add[RESTController] 
  }

}
