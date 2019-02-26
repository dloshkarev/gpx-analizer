package dao

import models.{Activity, ActivityStatistic}

import scala.concurrent.Future

/**
  * DAO give access to activities data
  */
trait ActivityDao {
  /**
    * Save new activity
    * @param activity activity data
    */
  def insert(activity: Activity): Future[Unit]

  /**
    * Fetch all activities processed by user byYear guid
    *
    * @param requestGuid user byYear guid
    * @param yearStart activities start period
    * @param yearFinish activities end period
    */
  def fetchAllByGuid(requestGuid: String, yearStart: Option[Int], yearFinish: Option[Int]): Future[Seq[Activity]]

  /**
    * Get all years in byYear activities list
    * @param requestGuid user byYear guid
    * @return list of years
    */
  def getActivityYears(requestGuid: String): Future[Seq[Int]]

  /**
    * Check if user request exists
    * @param requestGuid user request guid
    * @return request exists?
    */
  def isUserRequestExists(requestGuid: String): Future[Boolean]

  /**
    * Fetch activity running index grouped by year and week
    * @param requestGuid user request guid
    * @return activity statistic data
    */
  def fetchActivityStatistic(requestGuid: String): Future[Seq[ActivityStatistic]]
}
