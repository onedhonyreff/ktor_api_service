package id.bts.utils

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateTimeUtils {

  fun String?.parseToDate(format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): Date? {
    val simpleDateFormat = SimpleDateFormat(format)
    return try {
      simpleDateFormat.parse(this)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun Date?.parseToString(format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): String? {
    val simpleDateFormat = SimpleDateFormat(format)
    return this?.let { date ->
      try {
        simpleDateFormat.format(date)
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
    }
  }

  fun Date?.parseToLocalDate(): LocalDate? {
    val instant = this?.toInstant()
    return instant?.atZone(ZoneId.systemDefault())?.toLocalDate()
  }

  fun String?.parseToLocalDate(format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): LocalDate? {
    val dateTimeFormatter = DateTimeFormatter.ofPattern(format)
    return try {
      LocalDate.parse(this, dateTimeFormatter)
    } catch (e: Exception) {
      e.printStackTrace()
      null
    }
  }

  fun LocalDate?.parseToString(format: String = "yyyy-MM-dd'T'HH:mm:ss'Z'"): String? {
    val formatter = DateTimeFormatter.ofPattern(format)
    return this?.let { localDate ->
      try {
        val zonedDateTime = ZonedDateTime.of(localDate.atStartOfDay(), ZoneId.of("UTC"))
        zonedDateTime.format(formatter)
      } catch (e: Exception) {
        e.printStackTrace()
        null
      }
    }
  }
}