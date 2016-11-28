package com.weibo.datasys.submitter

import com.weibo.datasys.jobs.Job
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.StringEntity
import org.apache.http.impl.client.DefaultHttpClient

/**
  * Created by tuoyu on 11/21/16.
  * post data to chronos server and get the result
  */
object Submitter {
  val host = "10.77.16.213"
  val port = "4400"
  val prefix_url = "scheduler"
  val no_dependent_url = "iso8601"
  val dependent_url = "dependency"

  def post(job: Job): Unit = {

    val url = if (job.withDependencies)
      getDependentPostUrl()
    else
      getScheduledPostUrl

    println(s" url = $url")
    println(s" json = ${job.toString}")

    val post = new HttpPost(url)
    post.setHeader("Content-Type", "application/json")
    post.setEntity(new StringEntity(job.toString))

    // TODO 这里修改下返回的结果
    val response = (new DefaultHttpClient).execute(post)
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))
    println(s"response = $response")
  }

  def getScheduledPostUrl: String = {
    s"""http://$host:$port/$prefix_url/$no_dependent_url"""
  }

  def getDependentPostUrl(): String = {
    s"""http://$host:$port/$prefix_url/$dependent_url"""
  }
}
