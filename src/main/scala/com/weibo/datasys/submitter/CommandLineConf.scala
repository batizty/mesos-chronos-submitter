package com.weibo.datasys.submitter

import java.io.File

import com.weibo.datasys.utils.MyLogging
import org.rogach.scallop.ScallopConf

/**
  * Created by tuoyu on 11/25/16.
  */
class CommandLineConf(args: Seq[String]) extends ScallopConf(args) {
  version("mesos-chronos-submitter version 0.0.1")
  banner("""Usage: mesos-chronos-submitter """)
  footer("\nIf you met any question, please email to tuoyu@staff.weibo.com")

  val help = opt[Boolean](
    descr = "print this message",
    default = Some(false))

  val debug_mode = opt[Boolean](
    name = "debug_mode",
    default = Some(false),
    short = 'X',
    hidden = true
  )

  val conf_file = opt[File](
    name = "conf_file",
    descr = "Job Config file, default should be .conf",
    required = false,
    noshort = true
  )
  validateFileExists(conf_file)

  val owner = opt[String](
    name = "owner",
    descr = "This Job Owner, if you didn't prepare the conf file, this is required",
    required = false,
    noshort = true
  )

  val name = opt[String](
    name = "name",
    descr = "This Job Name, if you didn't prepare the conf file ready, this is required",
    required = false,
    noshort = true
  )

  val command = opt[String](
    name = "command",
    descr = "This Job Command Line, if didn't prepare the conf file ready, this is required",
    required = false,
    noshort = true
  )

  val example = opt[Boolean](
    descr = "generate example conf file",
    default = Some(false),
    required = false,
    noshort = true
  )

  // TODO 加上host参数
  // TODO 加上dependencies参数


  override def verify() = {
    super.verify()
    if (conf_file.isEmpty
      && (name.isEmpty || owner.isEmpty || command.isEmpty)
      && (example.isEmpty)) {
      showError("Could not enough arguments from command line")
    }
  }

  override def onError(e: Throwable): Unit = {
    showError(e.getMessage)
  }

  def showError(e: String): Unit = {
    MyLogging.error(s"Parse Command Line Failed")
    MyLogging.error(s"$e")
    this.printHelp()
    sys.exit(-1)
  }

  verify()
}
