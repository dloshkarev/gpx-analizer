package models

import play.api.libs.json.{JsString, Writes}

/**
  * Pace data
  *
  * @param min minutes
  * @param sec remain seconds
  */
case class Pace(min: Int, sec: Int) extends Ordered[Pace] {
  /**
    * @return pace as seconds
    */
  def asSeconds = min * 60 + sec

  override def toString: String = (if (min < 10) "0" + min else min) + ":" + (if (sec < 10) "0" + sec else sec)

  override def compare(that: Pace): Int = that.asSeconds - asSeconds
}

object Pace {
  def apply(seconds: Int): Pace = Pace(seconds / 60, seconds % 60)
  val writer: Writes[Pace] = (pace: Pace) => JsString(pace.toString())
}
