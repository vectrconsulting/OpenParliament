package consulting.vectr.service

import scalaj.http.{Http, HttpRequest, HttpResponse}

/**
  * Wrapper for scalaj http service so we can inject it
  */
class HttpService {

  def url(url: String, params: Map[String, String]=Map(), headers:Map[String, String]=Map("Accept"->"application/json")): HttpResponse[String] =
    Http(url).headers(headers)
      .params(params)
      .asString
}

