package com.weibo.datasys.utils

import org.apache.commons.lang.StringUtils

import scala.io.Source
import scala.reflect.io.Path


/**
  * Created by tuoyu on 12/2/16.
  * parser command and find right env.sh, then write the properties into the right sh
  * 1 得到划分路径的层次
  * 2 获得log文件的名称
  */
object CommandParser {
  val env_file_name = "env.sh"

  def parseCommand(cmd: String): String = {
    val cmdList: List[String] = cmd
      .split("\\s+")
      .toList
      .map(_.trim)
      .flatMap(_.split(";"))

    println(s" cmdlist = $cmdList")
    val shList: List[String] = cmdList
      .filter(_.startsWith("/"))

    for {sh <- shList} {
      println(s" sh = $sh")
      getAllPath(sh)
    }
    cmd
  }

  def getAllPath(path: String): List[String] = {
    val subdir = path.split("\\/")
    var tmpdir = ""

    var envFiles: List[String] = List()
    for (d <- subdir) {
      tmpdir += d + "/"
      envFiles = envFiles ++ List(tmpdir + env_file_name)
    }

    envFiles.filter(Path(_).exists)
  }

  def main(args: Array[String]): Unit = {
    if (StringUtils.isBlank(args(0))) {
      println(s" args 0 should be file name for read")
      sys.exit(-1)
    }
    val lines = Source
      .fromFile(args(0))
      .getLines()
      .map(_.trim)
      .filter(l => false == l.startsWith("#"))
      .filter(_.length > 0)


    for {line <- lines} {
      println(s" raw line : $line")
      //      val r = parseCommand(line)
      //      println(s" ret line : $r")
    }
    ()
  }
}
