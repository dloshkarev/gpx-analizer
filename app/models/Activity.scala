package models

import org.joda.time.DateTime

/**
  * Activity data
  *
  * @param id            activity id
  * @param requestGuid   user byYear guid, which processed this activity
  * @param eventDate     activity date + time
  * @param distance      distance in km
  * @param duration      duration in minutes
  * @param elevationGain elevation gain in meters
  * @param elevationLoss elevation loss in meters
  * @param avgHr         average heart rate
  * @param avgPace       average pace
  * @param runningIndex  running index
  */
case class Activity(id: Option[Long], requestGuid: String, eventDate: DateTime, distance: Double, duration: Double,
                    elevationGain: Double, elevationLoss: Double, avgHr: Double, avgPace: Pace, runningIndex: Double) {
  override def toString: String = "Activity(date = " + eventDate + "; distance = " + distance + "; duration = " + duration +
    "; elevationGain = " + elevationGain + "; elevationLoss = " + elevationLoss + "; avgHr = " + avgHr +
    "; avgPace = " + avgPace + "; runningIndex = " + runningIndex + ")"
}