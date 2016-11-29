package com.weibo.datasys.submitter

import com.typesafe.config.ConfigFactory
import com.weibo.datasys.jobs.Job
import com.weibo.datasys.utils.MyLogging
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient


/**
  * Created by tuoyu on 11/21/16.
  * post data to chronos server and get the result
  */
object Submitter {
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

    MyLogging.info(s"post url = $url")
    MyLogging.info(s"post json = ${job.toJson}")

    // TODO 这里需要改成post，future，并且可以重试

    val post = new HttpPost(url)
    post.setHeader("Content-Type", "application/json")
    post.setEntity(new StringEntity(job.toJson))

    // TODO 这里修改下返回的结果
    val response = (new DefaultHttpClient).execute(post)
    MyLogging.debug("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => MyLogging.debug(arg.toString))
    MyLogging.info(s"response status = ${response.getStatusLine}")
  }

  def getScheduledPostUrl: String = {
    s"""http://$host:$port/$prefix_url/$no_dependent_url"""
  }

  def getDependentPostUrl(): String = {
    s"""http://$host:$port/$prefix_url/$dependent_url"""
  }
}
