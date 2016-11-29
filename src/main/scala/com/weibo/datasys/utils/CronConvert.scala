package com.weibo.datasys.utils


import com.typesafe.config.ConfigFactory
import org.joda.time.DateTime

import scala.io.Source
import scala.util.{Success, Try}

/**
  * Created by tuoyu on 11/23/16.
  */
object CronConvert {
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

  val conf = ConfigFactory.load()

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
    * @return List[(DateTime, Long)]
    */
  def convert(cron: String)(implicit _debug_mode: Boolean = false): List[ISOPeriodTime] = {
    MyLogging.debug(s"raw cron string = $cron")

    var result: List[ISOPeriodTime] = List.empty
    val arr = cron.split("\\s+")
    if (arr.size >= conf.getInt("chronos.crontab.item-number")) {
      val List(min, hour, day, dayOfMonth, weekday) = arr.toList.take(5)

      val cron_map: Map[String, String] = Map(CRON_KEY_MIN -> min,
        CRON_KEY_HOUR -> hour,
        CRON_KEY_DAY -> day,
        CRON_KEY_DAY_OF_MONTH -> dayOfMonth,
        CRON_KEY_WEEKDAY -> weekday)
        .filter { case (k, v) => v != SYMBOL_STAR }

      MyLogging.debug(s"cron_map = $cron_map ")

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
          result = ISOPeriodTime(getLastWeekDay(wd, hh, mm), interval) :: result
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
                    case Success(i) =>
                      result = ISOPeriodTime(DateTime.now(), i * SECONDS_OF_MIN) :: result
                    case _ => ()
                  }
                case _ =>
                  val mins = getListValues(Some(mm))
                  val hours = getListValues(cron_map.get(CRON_KEY_HOUR))
                  if (mins.size > 1) {
                    mins map { mmm =>
                      result = ISOPeriodTime(getLastHourMin(mmm), SECONDS_OF_HOUR) :: result
                    }
                  } else if (mins.size == 1 && hours.size == 0) {
                    mins map { mmm =>
                      result = ISOPeriodTime(getLastHourMin(mmm), SECONDS_OF_HOUR) :: result
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
                    case Success(i) =>
                      result = ISOPeriodTime(DateTime.now(), i * SECONDS_OF_HOUR) :: result
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
                      result = ISOPeriodTime(getLastDayHour(hhh, mm), SECONDS_OF_DAY) :: result
                    }
                  }
              }
            }
          }
        }
      }
    }
    result foreach { x => MyLogging.debug(x.toString) }
    result
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

  def showResult(d: DateTime, i: Long): Unit = {
    MyLogging.debug(s"  >>> start dt = $d interval = $i <<<")
  }
}

case class ISOPeriodTime(
                          start: DateTime,
                          interval: Long,
                          count: Int = -1) {
  override def toString: String = {
    var str = s"start time = $start interval = $interval"
    if (count > 0) {
      str += s" count = $count"
    }
    str //+ " scheduler string = " + toScheduleString
  }

  def toScheduleString: String = {
    val r = if (count > 0) s"R$count" else "R"
    s"$r/$start/PT${interval}S"
  }

}