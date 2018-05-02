package consulting.vectr.controller

import java.util.Date
import java.util.concurrent.TimeUnit
import javax.inject.Inject

import com.twitter.finagle.http.Request
import com.twitter.finatra.http.Controller
import com.twitter.finatra.http.response.ResponseBuilder
import com.twitter.finatra.http.routing.FileResolver
import com.twitter.inject.Logging
import com.twitter.util.Duration
import consulting.vectr.service.ScheduledLoaderService

import scala.language.postfixOps
import scala.util.{Failure, Success, Try}

object AppController {
  private val DEFAULT_EXPIRE_TIME_MS: Long = 86400000L // 1 day
}

class AppController @Inject() (resolver: FileResolver,
                               scheduledloader: ScheduledLoaderService) extends Controller with Logging {
  import AppController._

  private val disableCache: Boolean = false
  scheduledloader.start()

  get("/") {request : Request =>
      response.ok.fileOrIndex(
        "",
        "/app-ui/welcome.html"
      )
    }

  get("/ui") {request : Request =>
    response.ok.fileOrIndex(
      "",
      "/app-ui/dashboard.html"
    )
  }
  
  get("/test.html") {request : Request =>
    response.ok.fileOrIndex(
      "",
      "/app-ui/test.html"
    )
  }

  get("/css/:*") {request: Request =>
    val fileResourceURI: String = "/app-ui/css/" + request.getParam("*")

    if (isDirectoryRequest(fileResourceURI)) {
      response.forbidden
    } else {
      val inputStream = getClass.getResourceAsStream(fileResourceURI)
      if (inputStream != null) {
        val resp = response.ok
        try {
          val filename: String = getFileName(fileResourceURI)
          resp.mediaType = resolver.getContentType(filename)
          resp.body(inputStream)
        } finally {
          inputStream.close
        }
      } else {
        response.notFound
      }
    }
  }

  get("/logos/:*") {request: Request =>
    val fileResourceURI: String = "/app-ui/logos/" + request.getParam("*")

    if (isDirectoryRequest(fileResourceURI)) {
      response.forbidden
    } else {
      val inputStream = getClass.getResourceAsStream(fileResourceURI)
      if (inputStream != null) {
        val resp = response.ok
        try {
          val filename: String = getFileName(fileResourceURI)
          resp.mediaType = resolver.getContentType(filename)
          resp.body(inputStream)
        } finally {
          inputStream.close
        }
      } else {
        response.notFound
      }
    }
  }

  get("/dist/:*") {request: Request =>
    val fileResourceURI: String = "/app-ui/dist/" + request.getParam("*")

    if (isDirectoryRequest(fileResourceURI)) {
      response.forbidden
    } else {
      val inputStream = getClass.getResourceAsStream(fileResourceURI)
      if (inputStream != null) {
        val resp = response.ok
        try {
          val filename: String = getFileName(fileResourceURI)
          resp.mediaType = resolver.getContentType(filename)
          resp.body(inputStream)
        } finally {
          inputStream.close
        }
      } else {
        response.notFound
      }
    }
  }

  get("/build/:*") { request: Request =>
    val resourcePath = request.getParam("*")

    val fileResourceURI: String = "/app-ui/build/" + resourcePath
    //logger.log(Level.FINE, "Webjars resource requested: {0}", webjarsResourceURI)

    if (isDirectoryRequest(fileResourceURI)) {
      response.forbidden
    } else {
      val inputStream = getClass.getResourceAsStream(fileResourceURI)
      if (inputStream != null) {
        val resp = response.ok
        try {
          val filename: String = getFileName(fileResourceURI)
          resp.mediaType = resolver.getContentType(filename)
          resp.body(inputStream)
        } finally {
          inputStream.close
        }
      } else {
        response.notFound
      }
    }
  }

  private def isDirectoryRequest(uri: String): Boolean = {
    uri.endsWith("/")
  }

  private def getFileName(webjarsResourceURI: String): String = {
    val tokens: Array[String] = webjarsResourceURI.split("/")
    tokens(tokens.length - 1)
  }

  private def getETagName(webjarsResourceURI: String): String = {
    val tokens: Array[String] = webjarsResourceURI.split("/")
    if (tokens.length < 7) {
      throw new IllegalArgumentException("insufficient URL has given: " + webjarsResourceURI)
    }
    val version: String = tokens(5)
    val fileName: String = tokens(tokens.length - 1)
    val eTag: String = fileName + "_" + version
    eTag
  }

  private def checkETagMatch(request: Request, eTagName: String): Boolean = {
    request.headerMap.get("If-None-Match") match {
      case None => false
      case Some(token) => token == eTagName
    }
  }

  private def checkLastModify(request: Request): Boolean = {
    request.headerMap.get("If-Modified-Since").map(_.toLong) match {
      case None => false
      case Some(last) => last - System.currentTimeMillis > 0L
    }
  }

  private def prepareCacheHeaders(response: ResponseBuilder#EnrichedResponse, eTag: String): Unit = {
    response.header("ETag", eTag)
    response.expires = new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS)
    response.lastModified = new Date(System.currentTimeMillis() + DEFAULT_EXPIRE_TIME_MS)
    response.cacheControl = Duration(DEFAULT_EXPIRE_TIME_MS, TimeUnit.MILLISECONDS)
  }

}
