package com.weibo.datasys.jobs

import com.weibo.datasys.conf.BaseConf
import com.weibo.datasys.utils.MyLogging
import org.json4s._
import org.json4s.native.JsonMethods.parse
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
                schedule: String = "",
                highPriority: Boolean = false,
                arguments: Set[String] = Set(),
                uris: Set[String] = Set(),
                environment: Set[String] = Set(),
                constrains: Set[Array[String]] = Set(),
                parents: Set[String] = Set()
              ) {
  implicit val format = Serialization.formats(NoTypeHints)

  override def toString = {
    Serialization.writePretty(this)
  }

  def toJson: String = {
    val json = parse(Serialization.write(this))
    if (withDependencies) {
      Serialization.writePretty(json.removeField(x => x._1 == "schedule"))
    } else {
      Serialization.writePretty(json.removeField(x => x._1 == "parents"))
    }
  }

  def withDependencies: Boolean = parents.nonEmpty
}


object Job {
  implicit val formats = DefaultFormats

  def parseJobs(str: String): List[Job] = {
    try {
      parse(str).extract[List[Job]]
    } catch {
      case e: Throwable =>
        e.printStackTrace()
        MyLogging.error(e.getMessage)
        sys.exit(-1)
    }
  }

  def apply(conf: BaseConf)(implicit _debug_mode: Boolean): List[Job] = {
    val crons: List[String] = conf.parseCron
    var index: Option[Int] = None
    if (crons.size > 1) {
      index = Some(-1)
    }
    crons.map { cron =>
      index foreach { i =>
        index = Some(i + 1)
      }

      Job(
        name = conf.name + index.map("_seq_" + _.toString).getOrElse(""),
        command = conf.parseCommand,
        epsilon = conf.epsilon,
        retries = conf.retries,
        owner = conf.owner,
        ownerName = conf.owner,
        description = conf.jobDescription + index.map("_seq_" + _.toString).getOrElse(""),
        cpus = conf.cpus,
        disk = conf.disk,
        mem = conf.mem,
        runAsUser = conf.user.getOrElse(conf.owner),
        schedule = cron,
        uris = conf.uris,
        constrains = conf.getConstrains,
        parents = conf.dependencies
      )
    }
  }
}



