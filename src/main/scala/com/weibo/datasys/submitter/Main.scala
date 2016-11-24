package com.weibo.datasys.submitter


import java.io.{File, PrintWriter}

import com.weibo.datasys.conf.{DataStrategyConf, BaseConf}
import com.weibo.datasys.jobs.Job
import com.weibo.datasys.utils.MyLogging
import org.apache.commons.lang.StringUtils


/**
  * Created by tuoyu on 11/21/16.
  */
object Main {

  val default_conf_file: String = ".conf"

  val option_generate_example_short: String = "g"
  val option_generate_example_long: String = "generate_example"

  val option_debug_mode = "-d"
  val option_command: String = "--command"
  val option_generate_example: String = "--generate_example"
  val option_owner: String = "--owner"
  val option_name: String = "--name"


  def main(args: Array[String]): Unit = {
    implicit val _debug_mode = (args.length > 0
      && StringUtils.isNotBlank(args(0))
      && args(0).toLowerCase() == option_debug_mode)

    if (_debug_mode) {
      MyLogging.debug("debug mode is open")
    }

    val path = new File(getConfPath)
    if (false == path.exists()) {
      MyLogging.info(s"Generate Conf file for this job dir = ${path.toString}")
      createConf()
      MyLogging.info(s"Please check .conf file and modify it firstly")
      MyLogging.info(s"Now .conf content is ${BaseConf.getExampleConf}")
      sys.exit(-1)
    }

    MyLogging.info(s"Now .conf content is ${BaseConf.getExampleConf}")

    val t = DataStrategyConf(
      "test",
      "echo 'hi'",
      "tuoyu",
      user = Some("root")
    )

    println(s" json = ${Job(t).toString}")

  }

  def createConf(): Boolean = {
    val conf_content = BaseConf.getExampleConf
    try {
      val writer = new PrintWriter(new File(getConfPath))
      writer.write(conf_content)
      writer.close()
      MyLogging.info(s"Write .conf into your work dir $getConfPath")
      true
    } catch {
      case e: Throwable =>
        MyLogging.error(e.getMessage)
        false
    }
  }

  def getConfPath: String = {
    getCurrentDir + "/" + default_conf_file
  }

  def getCurrentDir: String = {
    new File(".").getCanonicalPath()
  }
}