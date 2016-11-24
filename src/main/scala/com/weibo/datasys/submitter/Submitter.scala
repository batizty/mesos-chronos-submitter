package com.weibo.datasys.submitter

import java.util

import org.apache.commons.httpclient.methods.{PostMethod, StringRequestEntity}
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

  def getPostUrl(): String = {
    s"""http://$host:$port/$prefix_url/$no_dependent_url"""
  }


  def post(json: String): Unit = {
    val url = getPostUrl()
    println(s" url = $url")
    println(s" json = $json")

    val post = new HttpPost(url)
    post.setHeader("Content-Type", "application/json")
    post.setEntity(new StringEntity(json))

    val response = (new DefaultHttpClient).execute(post)
    println("--- HEADERS ---")
    response.getAllHeaders.foreach(arg => println(arg))
    println(s"response = $response")
  }
}
