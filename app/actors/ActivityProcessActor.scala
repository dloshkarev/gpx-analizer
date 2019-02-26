package actors

import java.io.File

import actors.ActivityProcessActor.Process
import akka.actor.{Actor, ActorLogging, ActorRef}
import dao.ActivityDao
import javax.inject.{Inject, Named}
import models.{Activity, Pace, Point}
import org.joda.time.format.ISODateTimeFormat
import org.joda.time.{DateTime, Seconds}

import scala.annotation.tailrec
import scala.language.postfixOps
import scala.xml.XML

object ActivityProcessActor {

  /**
    * Start process activities catalog
    *
    * @param requestGuid       user byYear guid
    * @param userEmail         user email
    * @param activitiesCatalog catalog with activities
    */
  case class Process(requestGuid: String, userEmail: String, activitiesCatalog: File)

}

/**
  * Actor for processing group of activity .gpx files
  *
  * @param emailSendActor actor for sending an email with notification for user
  * @param activityDao dao for storing activities data into database
  */
class ActivityProcessActor @Inject()(
                                      @Named("emailSendActor") emailSendActor: ActorRef,
                                      activityDao: ActivityDao
                                    ) extends Actor with ActorLogging {

  private val AVERAGE_RADIUS_OF_EARTH_KM = 6371
  private val ELEVATION_THRESHOLD = 0.5
  private val PACE_BOTTOM_LIMIT = Pace(7, 0)
  private val PACE_TOP_LIMIT = Pace(2, 0)
  private val ELEVATION_GAIN_LIMIT = 500

  /**
    * Parse .gpx file
    *
    * @param requestGuid user byYear GUID for processing activities
    * @param file        .gpx file data
    * @return activity data
    */
  private def parse(requestGuid: String, file: File): Option[Activity] = {
    try {
      val xml = XML.loadFile(file)
      if ((xml \ "trk").nonEmpty) {
        val datetime = (xml \ "metadata" \ "time") text

        val points = (xml \ "trk" \ "trkseg" \ "trkpt").map { x =>
          val lat = (x \ "@lat").text
          val lon = (x \ "@lon").text
          val elevation = (x \ "ele").text
          val hr = (x \ "extensions" \ "TrackPointExtension" \ "hr").text
          val time = (x \ "time").text
          Point(lat.toDouble, lon.toDouble, elevation.toDouble, if (hr.nonEmpty) Some(hr.toInt) else None, DateTime.parse(time, ISODateTimeFormat.dateTimeNoMillis()))
        }
        val (totalDistance, elevationGain, elevationLoss, totalHr) = accumulateActivityData(points.head, points.tail, 0.0, 0.0, 0.0, Some(0))
        val totalSec = Seconds.secondsBetween(points.head.time, points.last.time).getSeconds.toDouble
        val avgPace = calculatePace(totalSec, totalDistance)
        if (totalHr.isDefined) {
          val avgHr = totalHr.get.toDouble / points.size
          val activity = Activity(None, requestGuid, DateTime.parse(
            datetime, ISODateTimeFormat.dateTimeNoMillis()), totalDistance, totalSec / 60,
            elevationGain, elevationLoss, avgHr,
            avgPace, calculateRunningIndex(avgHr, avgPace, elevationGain, elevationLoss, totalDistance)
          )
          excludeArtifacts(activity)
        } else None
      } else None
    } catch {
      case e: Exception => throw new RuntimeException(s"File: ${file.getName}", e)
    }
  }

  /**
    * Exclude wrond activities with artifacts in pace, hr, elevation or distance
    * @param activity initial activity
    * @return activity or None
    */
  def excludeArtifacts(activity: Activity): Option[Activity] = {
    if (activity.avgPace > PACE_TOP_LIMIT ||
      (activity.avgPace < PACE_BOTTOM_LIMIT && activity.elevationGain < ELEVATION_GAIN_LIMIT)) None
    else Some(activity)
  }

  /**
    * Calculate average pace per distance
    *
    * @param totalTime     total time per distance
    * @param totalDistance total distance
    * @return pace data
    */
  private def calculatePace(totalTime: Double, totalDistance: Double): Pace = {
    val secondsPerKm = totalTime / totalDistance
    val paceMin = (secondsPerKm / 60).toInt
    val paceSec = (secondsPerKm - (paceMin * 60)).toInt
    Pace(paceMin, paceSec)
  }

  /**
    * Calculate distance in km between 2 route points
    *
    * @param current current route point
    * @param next    next route point
    * @return distance in km
    */
  private def calculateDistance(current: Point, next: Point): Double = {
    val latDistance = Math.toRadians(current.lat - next.lat)
    val lngDistance = Math.toRadians(current.lon - next.lon)
    val sinLat = Math.sin(latDistance / 2)
    val sinLng = Math.sin(lngDistance / 2)
    val a = sinLat * sinLat +
      (Math.cos(Math.toRadians(current.lat)) *
        Math.cos(Math.toRadians(next.lat)) *
        sinLng * sinLng)
    AVERAGE_RADIUS_OF_EARTH_KM * 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
  }

  /**
    * Calculate activity running index using heart rate, pace, elevation gain and elevation loss
    *
    * @param avgHr average heart rate per distance
    * @param avgPace  average pace per distance
    * @param elevationGain elevation gain per distance
    * @param elevationLoss elevation loss per distance
    * @param distance distance in km
    * @return running index
    */
  def calculateRunningIndex(avgHr: Double, avgPace: Pace, elevationGain: Double, elevationLoss: Double, distance: Double): Double = {
    (avgHr / avgPace.asSeconds) +
      (elevationGain / (distance * 100)) -
      (elevationLoss / (distance * 200))
  }

  /**
    * Process route points and aggregate activity data
    *
    * @param current       current route point
    * @param route         route remains
    * @param totalDistance accumulator for activity distance
    * @param elevationGain accumulator for activity elevation gain
    * @param elevationLoss accumulator for activity elevation loss
    * @param sumHr         accumulator for activity hr
    * @return (total distance, total elevation gain, total elevation loss, total hr)
    */
  @tailrec
  private def accumulateActivityData(current: Point, route: Seq[Point], totalDistance: Double,
                                     elevationGain: Double, elevationLoss: Double, sumHr: Option[Int]): (Double, Double, Double, Option[Int]) = {
    if (route.isEmpty) (totalDistance, elevationGain, elevationLoss, if (current.hr.isDefined) Some(sumHr.get + current.hr.get) else None)
    else {
      val nextPoint = route.head
      val distance = calculateDistance(current, nextPoint)
      val elevation = nextPoint.elevation - current.elevation
      accumulateActivityData(nextPoint, route.tail,
        totalDistance + distance,
        if (elevation - ELEVATION_THRESHOLD > 0) elevationGain + elevation else elevationGain,
        if (elevation + ELEVATION_THRESHOLD < 0) elevationLoss + math.abs(elevation) else elevationLoss,
        if (current.hr.isDefined) Some(sumHr.get + current.hr.get) else None
      )
    }
  }

  override def receive: Receive = {
    case Process(requestGuid: String, userEmail: String, activitiesCatalog: File) => {
      log.info(s"ActivityProcessActor.Process: requestGuid = $requestGuid, userEmail = $userEmail, activitiesCatalog = $activitiesCatalog. ")
      if (activitiesCatalog.exists()) {
        try {
          for (gpxFile <- activitiesCatalog.listFiles()) {
            try {
              parse(requestGuid, gpxFile) match {
                case Some(activity) => activityDao.insert(activity)
                case None => log.warning(s"Skipped activity from file: ${gpxFile.getName} for byYear: $requestGuid")
              }
            } finally {
              gpxFile.delete()
            }
          }
        } finally {
          activitiesCatalog.delete()
          log.info(s"Request $requestGuid processing completed")
          emailSendActor ! EmailSendActor.Send(userEmail)
        }
      } else log.error(s"Activity catalog: $activitiesCatalog for byYear: $requestGuid doesn't exists")
    }
  }
}
