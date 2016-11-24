package com.weibo.datasys.jobs

import com.weibo.datasys.conf.BaseConf
import org.json4s.{NoTypeHints, ShortTypeHints}
import org.json4s.native.Serialization


/**
  * Created by tuoyu on 11/21/16.
  */
case class Job(
                name: String,
                command: String,
                shell: Boolean = true,
                epsilon: String,
                retries: Int,
                owner: String,
                ownerName: String,
                description: String,
                cpus: Double,
                disk: Long,
                mem: Long,
                disabled: Boolean = false,
                runAsUser: String,
                schedule: String,
//                scheduleTimeZone: String = "",
                highPriority: Boolean = false,
                arguments: Set[String] = Set(),
                uris: Set[String] = Set(),
                environment: Set[String] = Set(),
                constrains: Set[String] = Set()) {
  import org.json4s._
  implicit val format = Serialization.formats(NoTypeHints)

  override def toString = {
    Serialization.writePretty(this)
  }
  def toJson: String = {
   Serialization.write(this)
  }
}


object Job {
  def apply(conf: BaseConf): Job = Job(
    name = conf.name,
    command = conf.parseCommand,
    epsilon = conf.epsilon,
    retries = conf.retries,
    owner = conf.owner,
    ownerName = conf.owner,
    description = conf.jobDescription,
    cpus = conf.cpus,
    disk = conf.disk,
    mem = conf.mem,
    runAsUser = conf.user.getOrElse(conf.owner),
    schedule = conf.parseCron,
    uris = conf.uris,
    constrains = conf.getConstrains
  )
}



