package com.weibo.datasys.utils

import org.joda.time.DateTime

/**
  * Created by tuoyu on 11/22/16.
  */
object MyLogging {
  val LOG_LEVEL_INFO = "INFO"
  val LOG_LEVEL_DEBUG = "DEBUG"
  val LOG_LEVEL_ERROR = "ERROR"

  def info(info: String): Unit = {
    printOutMessage(LOG_LEVEL_INFO, info)
  }

  def info(fmt: String, info: String): Unit = {
    printOutMessage(LOG_LEVEL_INFO, String.format(fmt, info))
  }

  def debug(debug: String)(implicit _debug_mode: Boolean = false): Unit = {
    if (_debug_mode)
      printOutMessage(LOG_LEVEL_DEBUG, debug)
  }

  def error(err: String): Unit = {
    printOutMessage(LOG_LEVEL_ERROR, err)
  }

  private def printOutMessage(prefix: String, s: String): Unit = {
    val date = DateTime.now.toString("YYYY-MM-dd HH:mm:SS")
    println(s"$date $prefix : $s")
  }

  def error(fmt: String, err: String): Unit = {
    printOutMessage(LOG_LEVEL_ERROR, String.format(fmt, err))
  }
}
