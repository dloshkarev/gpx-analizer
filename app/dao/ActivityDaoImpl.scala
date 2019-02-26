package dao

import java.sql.Timestamp

import javax.inject.{Inject, Singleton}
import models.{Activity, ActivityStatistic, Pace}
import org.joda.time.DateTime
import play.api.db.slick.{DatabaseConfigProvider, HasDatabaseConfigProvider}
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class ActivityDaoImpl @Inject()(protected val dbConfigProvider: DatabaseConfigProvider)(implicit executionContext: ExecutionContext) extends HasDatabaseConfigProvider[JdbcProfile] with ActivityDao {

  import profile.api._

  private val Activities = TableQuery[ActivityTable]

  // Implicit mappers
  implicit def timestamp2dateTime = MappedColumnType.base[DateTime, Timestamp](
    dateTime => new Timestamp(dateTime.getMillis),
    date => new DateTime(date))

  implicit def secondsToPace = MappedColumnType.base[Pace, Int](
    pace => pace.asSeconds,
    seconds => Pace(seconds))

  implicit val getActivityStatisticResult = GetResult(r => ActivityStatistic(r.<<, r.<<, r.<<))

  private class ActivityTable(tag: Tag) extends Table[Activity](tag, _tableName = "activity") {
    def id = column[Long]("id", O.PrimaryKey, O.AutoInc)

    def requestGuid = column[String]("request_guid")

    def eventDate = column[DateTime]("event_date")

    def distance = column[Double]("distance")

    def duration = column[Double]("duration")

    def elevationGain = column[Double]("elevation_gain")

    def elevationLoss = column[Double]("elevation_loss")

    def avgHr = column[Double]("avg_hr")

    def avgPace = column[Pace]("avg_pace")

    def runningIndex = column[Double]("running_index")

    def * = (id.?, requestGuid, eventDate, distance, duration, elevationGain, elevationLoss, avgHr, avgPace, runningIndex) <> (Activity.tupled, Activity.unapply)
  }

  /**
    * Save new activity
    *
    * @param activity activity data
    */
  override def insert(activity: Activity): Future[Unit] = {
    db.run(Activities += activity).map { _ => () }
  }

  /**
    * Fetch all activities processed by user byYear guid
    *
    * @param requestGuid byYear guid
    * @param yearStart   activities start period
    * @param yearFinish  activities end period
    */
  override def fetchAllByGuid(requestGuid: String, yearStart: Option[Int], yearFinish: Option[Int]): Future[Seq[Activity]] = {
    var query = Activities.filter(_.requestGuid === requestGuid)
    if (yearStart.isDefined) {
      query = query.filter(_.eventDate >= new DateTime(yearStart.get, 1, 1, 0, 0))
    }
    if (yearStart.isDefined) {
      query = query.filter(_.eventDate <= new DateTime(yearFinish.get, 12, 31, 23, 59))
    }
    db.run(query.result)
  }

  override def getActivityYears(requestGuid: String): Future[Seq[Int]] = {
    db.run(sql"""select distinct extract(YEAR from event_date) as year from activity order by year""".as[Int])
  }

  /**
    * Check if user request exists
    *
    * @param requestGuid user request guid
    * @return request exists?
    */
  override def isUserRequestExists(requestGuid: String): Future[Boolean] = {
    db.run(Activities.filter(_.requestGuid === requestGuid).size.result).map(_ != 0)
  }

  /**
    * Fetch activity running index grouped by year and week
    *
    * @param requestGuid user request guid
    * @return activity statistic data
    */
  override def fetchActivityStatistic(requestGuid: String): Future[Seq[ActivityStatistic]] = {
    db.run(sql"""select extract(YEAR from event_date) as year, extract(WEEK from event_date) as week, avg(running_index) as avg_running_index from activity a group by week, year order by year, week;""".as[ActivityStatistic])
  }
}
