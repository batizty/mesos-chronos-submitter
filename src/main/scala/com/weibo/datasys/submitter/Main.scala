package com.weibo.datasys.submitter


import java.io.{File, PrintWriter}

import com.weibo.datasys.conf.{BaseConf, DataStrategyConf}
import com.weibo.datasys.jobs.Job
import com.weibo.datasys.utils.MyLogging


/**
  * Created by tuoyu on 11/21/16.
  */
object Main {
  def main(args: Array[String]): Unit = {
    lazy val cmd = new CommandLineConf(args)

    if (cmd.help()) {
      cmd.printHelp()
      sys.exit(0)
    }

    implicit val _debug_mode: Boolean = cmd.debug_mode()

    if (_debug_mode) {
      MyLogging.debug("debug mode is open")
    }

    MyLogging.debug(s"parse cmd command = ${cmd.command}")
    MyLogging.debug(s"parse cmd conf_file = ${cmd.conf_file}")
    MyLogging.debug(s"parse cmd owner = ${cmd.owner}")
    MyLogging.debug(s"parse cmd name = ${cmd.name}")
    MyLogging.debug(s"parse cmd command = ${cmd.command}")

    val path = new File(getConfPath)
    if (false == path.exists()) {
      MyLogging.info(s"Generate Conf file for this job dir = ${path.toString}")
      createConf()
      MyLogging.info(s"Please check .conf file and modify it firstly")
      MyLogging.info(s"Now .conf content is ${BaseConf.getExampleConf}")
      sys.exit(-1)
    }

    MyLogging.info(s"Now .conf content is ${BaseConf.getExampleConf}")
    sys.exit(0)

    val t = DataStrategyConf(
      "xxx_003",
      "echo 'hi'",
      "tuoyu",
      user = Some("root")
    )

    println(s" json = ${Job(t).toString}")
    val t2: String =
      """{
          "name": "50",
          "command": "echo 'hi'",
          "shell": true,
          "epsilon": "PT1M",
          "executor": "",
          "executorFlags": "",
          "retries": 2,
          "owner": "tuoyu@staff.weibo.com",
          "async": false,
          "cpus": 1.0,
          "schedule": "R/2014-03-08T20:00:00.000Z/PT5M",
          "constraints": []
        }
      """

    Submitter.post(Job(t))
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
    getCurrentDir + "/" + ".conf"
  }

  def getCurrentDir: String = {
    new File(".").getCanonicalPath()
  }
}