package com.weibo.datasys.submitter

import com.weibo.datasys.conf.BaseConf
import com.weibo.datasys.jobs.{JobConf, OneTimeJobConf}

//import org.slf4j.LoggerFactory

import org.json4s._
import org.json4s.native.JsonMethods._


/**
  * Created by tuoyu on 11/21/16.
  */
object Main {
//  val logger = LoggerFactory.getLogger("OfflineSubmitter")

  val default_conf_file: String = ".conf"

  val option_generate_example_short: String = "g"
  val option_generate_example_long: String = "generate_example"

//  def createConf(p: Path): String = {
//    ""
//  }
//
//  def readConf(p: Path): String = {
//    ""
//  }
//
//  def getCurrentDir: String = {
//    new File(".").getCanonicalPath
//  }


  def main(args: Array[String]): Unit = {
    println(s" example conf = ${BaseConf.getExampleConf}")
//    case class Student(name: String, age: Int, number: Array[Int], male: Option[String])
//    val t=parse(""" { "name" : "tom","age":23,"number":[1,2,3,4], "male":"male" } """)
//    println(t)
//
//    val s = t.extract[Student]
//    println(s" s = $s")
//    println(t.extract[Student])

//    val file_path = Path(getCurrentDir + "/" + default_conf_file)
//    logger.info(s"current work directory : ${file_path.toString}")
//    val conf_str = if (file_path.exists == false)
//        createConf(file_path)
//      else
//        readConf(file_path)
//    logger.info(conf_str)
//
//    val (ok: Boolean, err: Option[String]) = JobConf.checkValid(conf_str)
//    if (ok == false) {
//      sys.error(err.getOrElse("error"))
//    }
  }
}