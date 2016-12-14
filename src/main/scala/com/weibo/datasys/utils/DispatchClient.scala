package com.weibo.datasys.utils

import com.ning.http.client.generators.ByteArrayBodyGenerator
import dispatch._

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}

/**
  * Created by tuoyu on 12/14/16.
  */
object DispatchClient {

  import scala.concurrent.ExecutionContext.Implicits.global

  implicit val timeout = 5 seconds

  def post(json: String, URL: String): Future[Array[Byte]] = {
    val body: ByteArrayBodyGenerator = new ByteArrayBodyGenerator(json.getBytes)
    val svc = url(URL)
      .POST
      .setHeader("Content-Type", "application/json")
      .setBody(body)

    Http[Array[Byte]](svc OK as.Bytes)
  }

  def get(URL: String): String = {
    val svc = url(URL)
      .GET
    try {
      Await.result(Http[String](svc OK as.String), timeout)
    } catch {
      case e: Throwable =>
        MyLogging.debug(s"print stack : ${e.getStackTrace}")
        MyLogging.error(s"Could Access $URL with Message ${e.getMessage}")
        sys.exit(-1)
    }
  }
}
