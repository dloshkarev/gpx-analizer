package utils

object ActivityUtils {

  /**
    * Convert Int to String by time format:
    * 0 = "00"
    * 1 = "01"
    * 10 = "10"
    * @param n integer value
    * @return string value
    */
  def int2TimeString(n: Int): String = if (n == 0) "00" else if (n < 10) "0" + n else n.toString

  /**
    * Convert time in minutes to time as String
    * @param time time in minutes
    * @return time as String
    */
  def time2String(time: Double): String = {
    val hours = time.toInt / 60
    val minutes = time.toInt % 60
    val seconds = ((time * 60) % 60).toInt
    (if (hours > 0) int2TimeString(hours) + ":" else "") + int2TimeString(minutes) + ":" + int2TimeString(seconds)
  }
}
