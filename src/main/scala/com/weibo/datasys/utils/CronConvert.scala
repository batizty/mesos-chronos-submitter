package com.weibo.datasys.utils


import org.joda.time.DateTime

import scala.io.Source
import scala.util.{Success, Try}

/**
  * Created by tuoyu on 11/23/16.
  */
object CronConvert {
  implicit val _debug_mode: Boolean = true

  val SYMBOL_STAR = "*"
  val SYMBOL_EVERY = "/"

  val CRON_KEY_MIN = "min"
  val CRON_KEY_HOUR = "hour"
  val CRON_KEY_DAY = "day"
  val CRON_KEY_WEEKDAY = "weekday"
  val CRON_KEY_DAY_OF_MONTH = "dayOfMonth"

  val SECONDS_OF_MIN: Long = 60
  val MIN_OF_HOUR: Long = 60
  val HOUR_OF_DAY: Long = 24
  val DAY_OF_WEEK: Long = 7
  val SECONDS_OF_HOUR: Long = SECONDS_OF_MIN * MIN_OF_HOUR
  val SECONDS_OF_DAY: Long = SECONDS_OF_HOUR * HOUR_OF_DAY
  val SECONDS_OF_WEEK: Long = SECONDS_OF_DAY * DAY_OF_WEEK

  /** test main **/
  def main(args: Array[String]): Unit = {
    val lines = Source
      .fromFile("cron_job_list.txt")
      .getLines()
      .map(_.trim)
      .filter(_.length > 0)
      .filter(false == _.startsWith("#"))

    for (line <- lines) {
      convert(line)
    }
  }

  /**
    * 完成cron作业的时间设置的自动转换，如果无法转换，就给个错误
    *
    * @param cron
    * @return
    */
  def convert(cron: String): List[(DateTime, Long)] = {
    MyLogging.debug(s"raw cron string = $cron")

    var result: List[(DateTime, Long)] = List.empty
    val arr = cron.split("\\s+")
    if (arr.size > 6) {
      val List(min, hour, day, dayOfMonth, weekday) = arr.toList.take(5)

      val cron_map: Map[String, String] = Map(CRON_KEY_MIN -> min,
        CRON_KEY_HOUR -> hour,
        CRON_KEY_DAY -> day,
        CRON_KEY_DAY_OF_MONTH -> dayOfMonth,
        CRON_KEY_WEEKDAY -> weekday)
        .filter { case (k, v) => v != SYMBOL_STAR }

      MyLogging.debug(s" cron_map = $cron_map ")

      val wk_list: List[Int] = getListValues(cron_map.get(CRON_KEY_WEEKDAY))

      var interval: Long = 0L
      if (wk_list.nonEmpty) {
        interval = SECONDS_OF_WEEK
        val hh: Int = cron_map
          .get(CRON_KEY_HOUR)
          .map(x => Try {
            x.toInt
          }.getOrElse(0))
          .getOrElse(0)
        val mm: Int = cron_map
          .get(CRON_KEY_MIN)
          .map(x => Try {
            x.toInt
          }.getOrElse(0))
          .getOrElse(0)
        wk_list.map { wd =>
          result = (getLastWeekDay(wd, hh, mm), interval) :: result
        }
      } else {
        if (cron_map.get(CRON_KEY_HOUR).getOrElse(SYMBOL_STAR) == SYMBOL_STAR
          && cron_map.get(CRON_KEY_MIN).getOrElse(SYMBOL_STAR) == SYMBOL_STAR) {
          MyLogging.error("Could not parse cron syntax now\n" +
            s"now cron string = $cron")
        } else {
          val pattern = "\\*\\/([0-9]+)".r
          if (cron_map.get(CRON_KEY_MIN).isDefined) {
            cron_map.get(CRON_KEY_MIN).foreach { mm =>
              mm match {
                case pattern(number) =>
                  Try {
                    number.toInt
                  } match {
                    case Success(i) => result = (DateTime.now(), i * SECONDS_OF_MIN) :: result
                    case _ => ()
                  }
                case _ =>
                  val mins = getListValues(Some(mm))
                  val hours = getListValues(cron_map.get(CRON_KEY_HOUR))
                  if (mins.size > 1) {
                    mins map { mmm =>
                      result = (getLastHourMin(mmm), SECONDS_OF_HOUR) :: result
                    }
                  } else if (mins.size == 1 && hours.size == 0) {
                    mins map { mmm =>
                      result = (getLastHourMin(mmm), SECONDS_OF_HOUR) :: result
                    }
                  }
              }
            }
          }

          if (cron_map.get(CRON_KEY_HOUR).isDefined) {
            cron_map.get(CRON_KEY_HOUR).foreach { hh =>
              hh match {
                case pattern(number) =>
                  Try {
                    number.toInt
                  } match {
                    case Success(i) => result = (DateTime.now(), i * SECONDS_OF_HOUR) :: result
                    case _ => ()
                  }
                case _ =>
                  val hours = getListValues(Some(hh))
                  if (hours.nonEmpty) {
                    hours map { hhh =>
                      interval = SECONDS_OF_DAY
                      val mm: Int = cron_map
                        .get(CRON_KEY_MIN)
                        .map(x => Try {
                          x.toInt
                        }.getOrElse(0))
                        .getOrElse(0)
                      result = (getLastDayHour(hhh, mm), SECONDS_OF_DAY) :: result
                    }
                  }
              }
            }
          }
        }
      }
    }
    result foreach { x => showResult(x._1, x._2) }
    result
  }

  def showResult(d: DateTime, i: Long): Unit = {
    MyLogging.debug(s"  >>> start dt = $d interval = $i <<<")
  }

  def getLastWeekDay(wd: Int, h: Int, min: Int): DateTime = {
    DateTime.now
      .withDayOfWeek(wd)
      .minusWeeks(1)
      .withHourOfDay(h)
      .withMinuteOfHour(min)
      .withSecondOfMinute(0)
  }

  def getLastHourMin(min: Int): DateTime = {
    DateTime.now
      .withMinuteOfHour(min)
      .minusHours(1)
      .withSecondOfMinute(0)
  }

  def getLastDayHour(hour: Int, min: Int): DateTime = {
    DateTime.now
      .withHourOfDay(hour)
      .withMinuteOfHour(min)
      .withSecondOfMinute(0)
      .minusDays(1)
  }

  def getListValues(raw: Option[String]): List[Int] = {
    raw.map { values =>
      values.split(",").toList map { v: String =>
        try { Some(v.toInt) } catch {
          case e: Throwable =>
            MyLogging.error(s"value $v could not be parse as Int\n" +
              s"error message : ${e.getMessage}\n" +
              s"stack : ${e.printStackTrace()}")
            None
        }
      } flatten
    }.getOrElse(List.empty)
  }
}
