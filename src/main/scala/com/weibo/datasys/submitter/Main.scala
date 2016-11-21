package com.weibo.datasys.submitter

import com.weibo.datasys.jobs.{OneTimeJobConf, JobConf}
import org.apache.commons.cli._

/**
  * Created by tuoyu on 11/21/16.
  */
object Main {

  val option_generate_example_short = "g"
  val option_generate_example_long  = "generate_example"


  def main(args: Array[String]): Unit = {
//    val parser: CommandLineParser = new BasicParser

//    val options: Options = new Options

//    options.addOption(
//      OptionBuilder
//          .withLongOpt(option_generate_example_long)
//      option_generate_example_short,
//      option_generate_example_long,
//      true,
//      "Generate example Type, default is " + processor_type)
//
//    options.addOption(
//      OPTION_SHORT_REGION,
//      OPTION_LONG_REGION,
//      true,
//      "Process region, default is " + processor_region)
//
//    options.addOption(
//      OptionBuilder
//        .withLongOpt(OPTION_CONFIG_FILE_NAME)
//        .withDescription("Config file for this processor program")
//        .hasArg
//        .withArgName(OPTION_CONFIG_FILE_NAME)
//        .create)
//
//    options.addOption(
//      OptionBuilder
//        .withLongOpt(OPTION_DARWIN_MBLOG_SHOW_BATCH_URL)
//        .withDescription("Darwin interface URL for mblog show batch")
//        .hasArg
//        .withArgName(OPTION_DARWIN_MBLOG_SHOW_BATCH_URL)
//        .create)
//
//    options.addOption(
//      OptionBuilder
//        .withLongOpt(OPTION_DARWIN_GENERAL_DOC_SHOW_URL)
//        .withDescription("Darwin interface URL for general doc show batch")
//        .hasArg
//        .withArgName(OPTION_DARWIN_GENERAL_DOC_SHOW_URL)
//        .create)
//
//    options.addOption(
//      OptionBuilder
//        .withLongOpt(OPTION_DARWIN_GENERAL_DOC_PUT_URL)
//        .withDescription("Darwin interface URL for general doc put batch")
//        .hasArg.withArgName(OPTION_DARWIN_GENERAL_DOC_PUT_URL)
//        .create)
//
//    options.addOption(
//      OptionBuilder
//        .withLongOpt(OPTION_REDIS_CLUSTER_CONFIG)
//        .withDescription("Redis Cluster Configs, the format should be\n" + "\"{ \"master_host1:master_port1\":\"slave_host1:slave_port1\",\n" + "    \"master_host2:master_port2\":\"slave_host2:slave_port2\",\n" + "     ...\n" + "\"}")
//        .hasArg
//        .withArgName(OPTION_REDIS_CLUSTER_CONFIG)
//        .create)
//
//    val hf: HelpFormatter = new HelpFormatter

    val s = OneTimeJobConf.toString()
    val exp = OneTimeJobConf.getExampleConf
    val z: JobConf = OneTimeJobConf.emptyObject
    println(s" s = $s")
    println(s" example = ${exp}")
    println(s" full conf = ${z.getFullConf}")
    ()
  }
}
