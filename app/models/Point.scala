package models

import org.joda.time.DateTime

/**
  * Route point data
  *
  * @param lat       latitude
  * @param lon       longitude
  * @param elevation elevation
  * @param hr        heart rate
  * @param time      current date + time
  */
case class Point(lat: Double, lon: Double, elevation: Double, hr: Option[Int], time: DateTime)
