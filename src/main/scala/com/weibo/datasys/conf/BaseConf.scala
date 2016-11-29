package com.weibo.datasys.conf

import com.weibo.datasys.utils.{CronConvert, MyLogging}
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime

import scala.io.Source

/**
  * Created by tuoyu on 11/21/16.
  * Job中的环境设置代码，分为
  * 1 DataStrategyConf 算法主feed的环境内容
  * 2 DataSysConf 大数据的基本环境
  */
object BaseConf {
  val default_retries_times: Int = 2
  val default_retry_interval: Int = 60
  val default_cpu_value: Double = 0.1
  val default_mem_value: Long = 1024
  val default_disk_value: Long = 1024

  val default_one_time_job_prepare_time: Int = 5
  val default_one_time_job_timeout: Long = 1000000

  val example: BaseConf = DataStrategyConf("", "", "")

  def getExampleConf: String = example.getFullConf

  def readConfFile(path: String)(implicit _debug_mode: Boolean = false): BaseConf = {
    val content = Source
      .fromFile(path)
      .getLines()
      .toList
      .map(_.trim)
      .filter(false == _.startsWith("#"))
      .filter(false == _.startsWith("//"))
      .filter(false == _.startsWith("/*"))
      .filter(_.length > 0)
      .mkString("\n")
    MyLogging.info(s"read conf file content = $content")

    try {
      import org.json4s._
      import org.json4s.native.JsonMethods._
      implicit val formats = DefaultFormats

      // TODO now just support DataStrategyConf
      parse(content).extract[DataStrategyConf]

    } catch {
      case e: Throwable =>
        MyLogging.error(e.getMessage)
        sys.exit(-1)
    }
  }

  def apply(name: String, owner: String, command: String): BaseConf = {
    DataStrategyConf(
      name = name,
      owner = owner,
      command = command)
  }
}

trait BaseConf {
  // required
  def name: String

  def command: String

  def owner: String

  // available
  /* 当前作业的周期性配置，如果不配置，且没有依赖的时候，认为是单次作业 */
  def cron: Option[String]

  /* 当前作业的依赖关系，如果配置，则cron内容不起作用 */
  def dependencies: Set[String] = Set()

  /* 作业执行的用户 */
  def user: Option[String] = None

  def getConstrains: Set[Array[String]] = {
    host map { h =>
      Set(Array("hostname", "EQUALS", s"$h"),
        Array("ip", "EQUALS", s"$h"))
    } getOrElse Set()
  }

  /* 作业执行的目标机器 */
  def host: Option[String] = None

  /* 运行的时刻的参数配置 */

  /* 作业执行URI资源 */
  def uris: Set[String] = Set()

  /* 作业描述 */
  def description: Option[String]

  def jobDescription: String

  // 必须要实现的函数，方便对不同的作业进行解析
  def parseCron(implicit _debug_mode: Boolean): List[String] = {
    val cmd = {
      val c = cron.getOrElse("")
      if (StringUtils.isBlank(c))
        command
      else
        c
    }

    val periodTimes: List[String] = CronConvert
      .convert(cmd)
      .map(_.toScheduleString)

    MyLogging.debug(s"period times = $periodTimes")

    if (periodTimes.isEmpty)
      List(s"R1/${
        DateTime
          .now
          .plusMinutes(BaseConf.default_one_time_job_prepare_time)
          .toDateTimeISO
      }/PT${BaseConf.default_one_time_job_timeout.toString}S")
    else
      periodTimes
  }

  def parseCommand: String

  def checkValid: (Boolean, Option[String]) = {
    if (StringUtils.isBlank(name)) {
      (false, Some("name should not be null or empty"))
    } else if (StringUtils.isBlank(owner)) {
      (false, Some("owner should not be null or empty"))
    } else if (StringUtils.isBlank(command)) {
      (false, Some("command should not be null or empty"))
    } else (true, None)
  }

  def getFullConf: String = {
    s"""{
          ${getRequiredExample},

          // 如果提交的为单次作业，请跳到可选填部分修改

          // 如果提交的为周期性作业，并且无依赖，请填写，语法请查看crontab说明
          "cron" : "",

          // 如果提交的为周期性作业，并且依赖于某个之前提交的作业，之前填写的调度时间将失去效果
          "dependencies" : [],

          ${getAvailableExample}
    }"""
  }

  private def getRequiredExample: String = {
    s"""
          // 必须要填写的内容
          // 你的邮箱前缀
          "owner"       : "",
          // 作业名称，可以作为依赖存在，被其他任务所依赖
          "name"        : "",
          // 作业执行命令，填写你需要执行的命令
          "command"     : """""
  }

  private def getAvailableExample: String = {
    s"""
          // 可选填写部分
          // 作业执行失败之后的重试次数，默认为$retries
          // "retries"  : $retries,
          // 作业执行失败之后，重试之间的间隔时间，时间单位为S(秒)，默认为$retryInterval
          // "retryInterval" : $retryInterval

          // 作业申请cpu资源，默认为0.1
          // "cpus"        : $cpus,
          // 作业申请磁盘资源，单位为MB，默认为1024
          // "disk"        : $disk,
          // 作业申请内存资源，单位为MB，默认为1024
          // "mem"         : $mem,

          // 执行作业账号，默认和owner相同
          // "user"   : "owner",

          // 作业执行机器限制条件，默认为空
          // "host"        : []
          // 作业申请URI资源，在作业执行之前会自动下载至作业工作目录, 默认为空
          // "uris"        : [],

          // 作业描述，默认为 "提交时间 owner : owner Submit Job"
          // "description" : ""
    """
  }

  // Option
  /* 作业失败之后的重试次数 */
  def retries: Int = BaseConf.default_retries_times

  def cpus: Double = BaseConf.default_cpu_value

  /* 作业分配内存默认值，单位 MB */
  def mem: Long = BaseConf.default_mem_value

  /* 作业分配磁盘默认值，单位 MB */
  def disk: Long = BaseConf.default_disk_value

  /* 作业失败之后，重试的间隔时间，单位为s（秒）*/
  def retryInterval: Int = BaseConf.default_retry_interval

  def epsilon: String = s"PT${retryInterval}S"
}

