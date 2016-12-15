package com.weibo.datasys.submitter


import java.io.{File, PrintWriter}

import com.weibo.datasys.conf.BaseConf
import com.weibo.datasys.jobs.Job
import com.weibo.datasys.utils.MyLogging


/**
  * Created by tuoyu on 11/21/16.
  */
object Main {
  val defaultConfExampleName = ".conf"

  def main(args: Array[String]): Unit = {
    lazy val cmd = new CommandLineConf(args)

    if (cmd.help()) {
      cmd.printHelp()
      sys.exit(0)
    }

    implicit val _debug_mode: Boolean = cmd.debug_mode()

    if (_debug_mode) {
      MyLogging.debug("debug mode is open")
      cmd.showDebug()
    }

    var confOption: Option[BaseConf] = None

    if (cmd.example() == true) {
      MyLogging.info(s"Generate Job Conf Example File")
      createConf()
      sys.exit(0)
    } else if (cmd.list_jobs() == true) {
      MyLogging.info(s"List all Job Conf")
      for {job <- Submitter.getJobs()} {
        println(s"------- Job Name : ${job.name} --------")
        println(s" ${job.toString}")
      }
      sys.exit(0)
    } else if (cmd.conf_file.isDefined) {
      MyLogging.info(s"Read Job Conf from ${cmd.conf_file().getPath}")
      confOption = Some(BaseConf.readConfFile(cmd.conf_file().getPath))
    } else if (cmd.command.isEmpty
      || cmd.name.isEmpty
      || cmd.owner.isEmpty) {
      MyLogging.error(s"Could not find right argument for mesos-chronos-submitter")
      cmd.printHelp()
      sys.exit(-1)
    } else {
      confOption = Some(BaseConf(
        cmd.name(),
        cmd.owner(),
        cmd.command(),
        cmd.target_host.toOption,
        cmd.dependencies()
      ))
    }

    if (confOption.isEmpty) {
      MyLogging.error(s"Could not get right conf data, please check example to refill the conf file")
      MyLogging.error(s"example : ${BaseConf.getExampleConf}")
      sys.exit(-1)
    }

    val (ok, err) = confOption.get.checkValid
    if (ok == false || err.isDefined) {
      MyLogging.error(s"Could not get right conf data, please check example to refill the conf file")
      err foreach { e =>
        MyLogging.error(s"$e")
      }
      MyLogging.error(s"example : ${BaseConf.getExampleConf}")
      sys.exit(-1)
    }

    Job(confOption.get) foreach { job =>
      Submitter.post(job)
    }

    sys.exit(0)
  }

  def createConf(): Boolean = {
    val conf_content = BaseConf.getExampleConf
    try {
      val writer = new PrintWriter(new File(getConfPath))
      writer.write(conf_content)
      writer.close()
      MyLogging.info(s"Write $defaultConfExampleName into your work dir $getConfPath")
      true
    } catch {
      case e: Throwable =>
        MyLogging.error(e.getMessage)
        false
    }
  }

  def getConfPath: String = {
    getCurrentDir + "/" + defaultConfExampleName
  }

  def getCurrentDir: String = {
    new File(".").getCanonicalPath()
  }
}