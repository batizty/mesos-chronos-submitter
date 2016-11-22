package com.weibo.datasys.jobs

import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime

/**
  * Created by tuoyu on 11/21/16.
  *
  * 从Chronos的job configuration中生成自己的conf的配置文件，方便快速进行作业提交
  * Chronos的job configuration在 https://mesos.github.io/chronos/docs/api.html#job-configuration
  * 下面为摘抄出来的内容
  *
  *   Field	        Description	          Default
  *   name	        Name of job.	-
  *   description	  Description of job.	-
  *   command	      Command to execute.	-
  *   arguments	    Arguments to pass to the command. Ignored if shell is true	-
  *   shell	        If true, Mesos will execute command by running /bin/sh -c <command> and will ignore arguments. If false, command will be treated as the filename of an executable and arguments will be the arguments passed. If this is a Docker job and shell is true, the entrypoint of the container will be overridden with /bin/sh -c	true
  *   epsilon	      If, for any reason, a job can't be started at the scheduled time, this is the window in which Chronos will attempt to run the job again	PT60S or --task_epsilon
  *   executor	    Mesos executor. By default Chronos uses the Mesos command executor.	-
  *   executorFlags	Flags to pass to Mesos executor.	-
  *   retries       Number of retries to attempt if a command returns a non-zero status	2
  *   owner         Email address(es) to send job failure notifications. Use comma-separated list for multiple addresses.	-
  *   ownerName     Name of the individual responsible for the job.	-
  *   async         Execute using Async executor.	false
  *   successCount	Number of successes since the job was last modified.	-
  *   errorCount    Number of errors since the job was last modified.	-
  *   lastSuccess   Date of last successful attempt.	-
  *   lastError     Date of last failed attempt.	-
  *   cpus          Amount of Mesos CPUs for this job.	0.1 or --mesos_task_cpu
  *   mem           Amount of Mesos Memory (in MB) for this job.	128 or --mesos_task_mem
  *   disk          Amount of Mesos disk (in MB) for this job.	256 or --mesos_task_disk
  *   disabled      If set to true, this job will not be run.	false
  *   uris          An array of URIs which Mesos will download when the task is started.	-
  *   schedule      ISO 8601 repeating schedule for this job. If specified, parents must not be specified.	-
  *   scheduleTimeZone	The time zone for the given schedule, specified in the tz database format.	-
  *   parents       An array of parent jobs for a dependent job. If specified, schedule must not be specified.	-
  *   runAsUser     Mesos will run the job as this user, if specified.	--user
  *   container     This contains the subfields for the Docker container: type (required), image (required), forcePullImage (optional), network (optional), and volumes (optional).	-
  *   dataJob       Toggles whether the job tracks data (number of elements processed)	false
  *   environmentVariables	An array of environment variables passed to the Mesos executor. For Docker containers, these are also passed to Docker using the -e flag.	-
  *   constraints   Control where jobs run. Each constraint is compared against the attributes of a Mesos slave. See Constraints.
  */
trait JobConf {
  val default_retry_times: Int = 0
  val default_retry_interval: Int = 60
  val default_retry_interval_max: Int = 86400
  val default_cpus: Double = 0.1
  val default_disk: Double = 0.0
  val default_mem:  Double = 0.0
  val default_epsilon: String = "PT5M"

  def name: String
  def command: String
  def retries: Int = default_retry_times
  def owner: String
  def ownerName: String
  def description: String = ""

  def async: Boolean = false
  def cpus: Double = 0.1
  def disk: Long = 1024
  def mem: Long = 1024
  def disabled: Boolean = false           // not used
  def highPriority: Boolean = false       // not used yet
  def shell: Boolean = true

  def retryInterval: Int = default_retry_interval
  def runUser: Option[String] = None
  def host: String = ""
  def URIs: String = ""

  def schedule: String
  def constraints: Set[String] = {
    // TODO 根据host生成
    Set.empty
  }
  def uris: Set[String] = {
    // TODO 根据URIs生成
    Set.empty
  }
  def runAsUser: String = {
    runUser.map(u =>
      if (StringUtils.isBlank(u)) ownerName else u
    ).getOrElse(ownerName)
  }

  def epsilon: String = {
    var ri = default_retry_interval
    if (retryInterval <= 0
      || retryInterval > default_retry_interval_max)
      ri = default_retry_interval
    else
      ri = retryInterval
    s"PT${ri}S"
  }

  // TODO add later
  //  def environmentVariables: Seq[EnvironmentVariable] = List()
  //  def arguments: Seq[String] = List()
  //  def softError: Boolean = false
  //  def dataProcessingJobType: Boolean = false
  //  def constraints: Seq[Constraint] = List() */

  /**
    * Chronos submit post data
    * @return
    */
  override def toString: String = {
    s"""
        "name"        : "$name",
        "command"     : "$command",
        "epsilon"     : "$epsilon",
        "retries"     : $retries,
        "owner"       : "$owner",
        "ownerName"   : "$ownerName",
        "description" : "$description",
        "cpus"        : $cpus,
        "disk"        : $disk,
        "mem"         : $mem,
        "uris"        : [],
        "runAsUser"   : "$runAsUser",
        "shell"       : $shell
    """
  }

  def getExampleConf: String = {
    s"""
        {
          ${getRequiredExampleConf},
          ${getAvailableExampleConf}
        }
     """
  }

  def getRequiredExampleConf: String = {
    s"""// 必须要填写的内容
          // 你的邮箱前缀
          "owner"       : ,
          // 作业名称，可以作为依赖存在，被其他任务所依赖
          "name"        : "",
          // 作业执行命令，填写你需要执行的命令
          "command"     : "Command you wanna to run""""
  }

  def getAvailableExampleConf: String = {
    s"""
          // 可选填写部分
          // 作业执行失败之后的重试次数，默认为2
          // "retries"  : $default_retry_times,
          // 作业执行失败之后，重试之间的间隔时间，时间单位为S(秒)
          // "retryInterval" : $default_retry_interval
          // 用户名称，默认与owner相同
          // "ownerName"   : "",
          // 作业描述，默认为 "提交时间 owner : owner Submit Job"
          // "description" : "",
          // 作业申请cpu资源，默认为0.1
          // "cpus"        : $cpus,
          // 作业申请磁盘资源，单位为MB，默认为1024
          // "disk"        : $disk,
          // 作业申请内存资源，单位为MB，默认为1024
          // "mem"         : $mem,
          // 作业申请URI资源，在作业执行之前会自动下载至作业工作目录, 默认为空
          // "uris"        : "",
          // 执行作业账号，默认和owner相同
          // "runAsUser"   : "owner",
          // 作业执行机器限制条件，默认为空
          // "host"        : "" """
  }

  def getFullConf: String = {
    s"""{
          ${getRequiredExampleConf},

          // 如果提交的为单次作业，请跳到可选填部分修改

          // 如果提交的为周期性作业，并且无依赖，请填写
          "cron" : "",

          // 如果提交的为周期性作业，并且依赖于某个之前提交的作业，之前填写的调度时间将失去效果
          "DependencyJobs" : "",

       ${getAvailableExampleConf}
    }""".stripMargin
  }
}

object JobConf {
  def checkValid(conf: String): (Boolean, Option[String]) = {
    (false, None)
  }
}

/**
  * 单次运行作业类型
  */
object OneTimeJobConf {
  val emptyObject = OneTimeJobConf(owner = "", name = "", command = "")
  override def toString: String = emptyObject.toString()
  def getExampleConf: String = emptyObject.getExampleConf
}

case class OneTimeJobConf(
                           owner: String,
                           name: String,
                           command: String,
                           override val retries: Int = 2,
                           override val retryInterval: Int = 60,
                           desc: Option[String] = None,
                           override val cpus: Double = 0.1,
                           override val disk: Long = 1024,
                           override val mem: Long = 1024,
                           override val URIs: String = "",
                           override val runUser: Option[String] = None,
                           override val host: String = ""
                         ) extends JobConf {
  val dt = DateTime.now()

  override def ownerName: String = name
  override def description: String = {
    desc.getOrElse(s"${dt} owner : $owner submit Job")
  }


  override def schedule: String = {
    // TODO

    // 直接写入一个Now时间
    ""
  }

  override def toString(): String = {
    var submitStr =s"""{ ${super.toString()} """
    submitStr += "}"
    submitStr
  }

  override def getExampleConf: String = {
    super.getExampleConf
  }
}


