package com.weibo.datasys.conf

/**
  * Created by tuoyu on 11/22/16.
  */
object DataSysConf {

}

case class DataSysConf (
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
                         override val description: Option[String]
                       ) extends BaseConf {
  override def parseCommand: String = ???

  override def parseCron: String = ???

  override def checkValid: (Boolean, Option[String]) = super.checkValid
}