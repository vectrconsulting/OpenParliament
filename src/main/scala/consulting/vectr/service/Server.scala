package consulting.vectr.service


import com.twitter.finagle.http.{Request, Response}
import com.twitter.finatra.http.HttpServer
import com.twitter.finatra.http.filters.{CommonFilters, ExceptionMappingFilter, LoggingMDCFilter, TraceIdMDCFilter}
import com.twitter.finatra.http.routing.HttpRouter
import consulting.vectr.controller.{AppController, RESTController}
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
