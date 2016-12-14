package com.weibo.datasys.submitter

import com.typesafe.config.ConfigFactory
import com.weibo.datasys.jobs.Job
import com.weibo.datasys.utils.{DispatchClient, MyLogging}
import org.apache.commons.httpclient.HttpStatus
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient


/**
  * Created by tuoyu on 11/21/16.
  * post data to chronos server and get the result
  */
object Submitter {

  import scala.concurrent.duration._

  implicit val timeout = 5 seconds

  val conf = ConfigFactory.load()

  val host = conf.getString("chronos.host")
  val port = conf.getString("chronos.port")
  val prefix_url = "scheduler"
  val no_dependent_url = "iso8601"
  val dependent_url = "dependency"

  def post(job: Job)(implicit _debug_mode: Boolean): Unit = {

    val url = if (job.withDependencies)
      getDependentPostUrl()
    else
      getScheduledPostUrl

    if (job.withDependencies && false == checkDependencies(job.parents)) {
      MyLogging.error("Check Dependencies failed")
      sys.exit(-1)
    }

    MyLogging.debug(s"post url = $url")
    MyLogging.info(s"post job ${job.toJson} to $url")

    val post = new HttpPost(url)
    post.setHeader("Content-Type", "application/json")
    post.setEntity(new StringEntity(job.toJson))

    val response = (new DefaultHttpClient).execute(post)
    MyLogging.debug("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => MyLogging.debug(arg.toString))
    val status = response.getStatusLine.getStatusCode
    if (status == HttpStatus.SC_OK
      || status == HttpStatus.SC_CREATED
      || status == HttpStatus.SC_ACCEPTED
      || status == HttpStatus.SC_NON_AUTHORITATIVE_INFORMATION
      || status == HttpStatus.SC_NO_CONTENT
      || status == HttpStatus.SC_RESET_CONTENT
      || status == HttpStatus.SC_PARTIAL_CONTENT) {
      MyLogging.info(s"Post OK, Please check status through http://$host:$port/#")
    } else {
      MyLogging.error(s"Post Error, Plase check your input")
    }
  }

  def checkDependencies(dps: Set[String]): Boolean = {
    if (dps.nonEmpty) {
      try {
        val jsonStr = DispatchClient.get(checkJobsUrl())
        if (jsonStr.nonEmpty) {
          val jobs: List[Job] = Job.parseJobs(jsonStr)
          val jobNames: Set[String] = jobs.map(_.name).toSet
          val minusSet: Set[String] = dps -- jobNames
          if (minusSet.nonEmpty) {
            MyLogging.error(s"Dependencies Jobs [${minusSet.mkString(",")}] Not found, valid Jobs [${jobNames.mkString(",")}]")
            false
          } else true
        } else false
      } catch {
        case e: Throwable =>
          MyLogging.error(s"Could not access URL=${checkJobsUrl()} with Message : ${e.getMessage}")
          sys.exit(-1)
      }
    } else true
  }

  def checkJobsUrl(): String = {
    s"""http://$host:$port/scheduler/jobs"""
  }

  def getScheduledPostUrl: String = {
    s"""http://$host:$port/$prefix_url/$no_dependent_url"""
  }

  def getDependentPostUrl(): String = {
    s"""http://$host:$port/$prefix_url/$dependent_url"""
  }

}
