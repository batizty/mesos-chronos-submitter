package com.weibo.datasys.conf

import com.weibo.datasys.utils.CronConvert
import org.apache.commons.lang.StringUtils
import org.joda.time.DateTime

/**
  * Created by tuoyu on 11/22/16.
  */
object DataStrategyConf {

}

case class DataStrategyConf (
                              name: String,
                              command: String,
                              owner: String,
                              override val cron: Option[String] = None,
                              override val dependencies: Set[String] = Set(),
                              override val retries: Int = BaseConf.default_retries_times,
                              override val retryInterval: Int = BaseConf.default_retry_interval,
                              override val cpus: Double = BaseConf.default_cpu_value,
                              override val mem : Long = BaseConf.default_mem_value,
                              override val disk: Long = BaseConf.default_disk_value,
                              override val user: Option[String] = None,
                              override val host: Option[String] = None,
                              override val uris: Set[String] = Set(),
                              override val description: Option[String] = None
                            ) extends BaseConf {
  override def parseCommand: String = {
    command
  }

  override def parseCron: List[String] = {
    cron match {
      case Some(c) if (StringUtils.isNotBlank(c)) =>
        val periodTimes: List[String] = CronConvert
          .convert(c)
          .map(_.toScheduleString)
        if (periodTimes.isEmpty)
          List(s"R1/${DateTime.now.plusMinutes(5).toDateTimeISO}/PT10000S")
        else
          periodTimes
      case _ => List(s"R1/${DateTime.now.plusMinutes(5).toDateTimeISO}/PT10000S")
    }
  }

  override def jobDescription: String = {
    description match {
      case Some(desc) if (StringUtils.isNotBlank(desc)) => desc
      case _ =>
        s"""User <$owner> submit Job <$name> with Command <<$command>> with account <${this.user.getOrElse(this.owner)}> at ${DateTime.now}""".stripMargin
    }
  }

  override def getConstrains: Set[String] = Set()

  override def checkValid: (Boolean, Option[String]) = {
    // TODO 加上自己有类似git的限制条件
    super.checkValid
  }
}