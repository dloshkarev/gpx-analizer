package controllers

import java.io.File
import java.nio.file.{Files, Path}

import actors.UnzipActor
import akka.actor.ActorRef
import akka.stream.IOResult
import akka.stream.scaladsl._
import akka.util.ByteString
import dao.ActivityDao
import forms.ActivitiesUploadForm
import javax.inject.{Inject, Named}
import models.{Activity, ActivityStatistic, Pace}
import org.joda.time.DateTime
import org.webjars.play.WebJarsUtil
import play.api._
import play.api.libs.json.{JsString, Json, Writes}
import play.api.libs.streams._
import play.api.mvc.MultipartFormData.FilePart
import play.api.mvc._
import play.core.parsers.Multipart.FileInfo
import utils.ActivityUtils

import scala.concurrent.{ExecutionContext, Future}

/**
  * Main application controller
  *
  * @param cc         The Play controller components.
  * @param unzipActor starting actor for unzip input activities archive
  */
class ApplicationController @Inject()(cc: ControllerComponents, @Named("unzipActor") unzipActor: ActorRef, activityDao: ActivityDao)(
  implicit executionContext: ExecutionContext,
  webJarsUtil: WebJarsUtil,
  assets: AssetsFinder) extends AbstractController(cc) {

  private val logger = Logger(this.getClass)

  // JSON writers
  implicit val jodaDateWriter: Writes[DateTime] = (d: DateTime) => JsString(d.toString("dd.MM.yyyy"))
  implicit val doubleWriter: Writes[Double] = (d: Double) => JsString(BigDecimal(d).setScale(2, BigDecimal.RoundingMode.HALF_UP).toString())
  implicit val paceWriter: Writes[Pace] = models.Pace.writer

  implicit val activityWrites = new Writes[Activity] {
    def writes(activity: Activity) = Json.obj(
      "id" -> activity.id,
      "requestGuid" -> activity.requestGuid,
      "eventDate" -> activity.eventDate,
      "distance" -> activity.distance,
      "duration" -> ActivityUtils.time2String(activity.duration),
      "elevationGain" -> activity.elevationGain,
      "elevationLoss" -> activity.elevationLoss,
      "avgHr" -> activity.avgHr.toInt,
      "avgPace" -> activity.avgPace,
      "runningIndex" -> activity.runningIndex
    )
  }

  implicit val activityStatisticWrites = new Writes[ActivityStatistic] {
    def writes(statistic: ActivityStatistic) = Json.obj(
      "year" -> statistic.year,
      "week" -> statistic.week,
      "avgRunningIndex" -> statistic.avgRunningIndex
    )
  }

  /**
    * Show main page for activities uploading
    *
    * @return main page
    */
  def index = Action.async { implicit request =>
    Future.successful(Ok(views.html.index()))
  }

  /**
    * Show main page for activities uploading
    *
    * @return main page
    */
  def generalInfo(requestGuid: String): Action[AnyContent] = Action.async { implicit request =>
    activityDao.isUserRequestExists(requestGuid).map {
      case true => Ok(views.html.generalInfo(requestGuid))
      case false => Ok(views.html.notFound())
    }
  }

  /**
    * Show page with activities analyzing by years
    *
    * @param requestGuid user request guid
    * @return page for activity analyzing
    */
  def byYear(requestGuid: String): Action[AnyContent] = Action.async { implicit request =>
    activityDao.getActivityYears(requestGuid).map { years =>
      if (years.nonEmpty) Ok(views.html.byYear(requestGuid, years))
      else Ok(views.html.notFound())
    }
  }

  /**
    * Show page with activities performance growth analyzing
    *
    * @param requestGuid user request guid
    * @return page for activity performance growth analyzing
    */
  def performanceGrowth(requestGuid: String): Action[AnyContent] = Action.async { implicit request =>
    activityDao.isUserRequestExists(requestGuid).map {
      case true => Ok(views.html.performanceGrowth(requestGuid))
      case false => Ok(views.html.notFound())
    }
  }

  type FilePartHandler[A] = FileInfo => Accumulator[ByteString, FilePart[A]]

  /**
    * Uses a custom FilePartHandler to return a type of "File" rather than
    * using Play's TemporaryFile class.  Deletion must happen explicitly on
    * completion, rather than TemporaryFile (which uses finalization to
    * delete temporary files).
    *
    * @return temp file with input data
    */
  private def handleFilePartAsFile: FilePartHandler[File] = {
    case FileInfo(partName, filename, contentType) =>
      val path: Path = Files.createTempFile("multipartBody", "tempFile")
      val fileSink: Sink[ByteString, Future[IOResult]] = FileIO.toPath(path)
      val accumulator: Accumulator[ByteString, IOResult] = Accumulator(fileSink)
      accumulator.map {
        case IOResult(count, status) =>
          logger.info(s"File size = $count, status = $status")
          FilePart(partName, filename, contentType, path.toFile)
      }
  }

  /**
    * Uploads an archive with user activities + user email for notification
    */
  def uploadActivitiesZip: Action[MultipartFormData[File]] = Action(parse.multipartFormData(handleFilePartAsFile)) { implicit request =>
    request.body.file("activitiesZip").map {
      case FilePart(key, filename, contentType, file) =>
        logger.info(s"key = $key, filename = $filename, contentType = $contentType, file = $file")
        ActivitiesUploadForm.form.bindFromRequest.fold(
          form => BadRequest(views.html.index()),
          email => {
            logger.info(s"email = $email")
            unzipActor ! UnzipActor.Unzip(email, file.getAbsolutePath)
            Ok("ok")
          }
        )
    }
    BadRequest(views.html.index())
  }

  /**
    * Fetch all activities processed by byYear
    *
    * @param requestGuid user byYear guid
    * @return list of activities
    */
  def fetchRequestActivities(requestGuid: String, yearStart: Option[Int], yearFinish: Option[Int]): Action[AnyContent] = Action.async { implicit request =>
    activityDao.fetchAllByGuid(requestGuid, yearStart, yearFinish).map { activities =>
      Ok(Json.toJson(activities))
    }
  }

  /**
    * Fetch activities statistic
    *
    * @param requestGuid user byYear guid
    * @return list of activities
    */
  def fetchRequestActivitiesStatistic(requestGuid: String): Action[AnyContent] = Action.async { implicit request =>
    activityDao.fetchActivityStatistic(requestGuid).map { statistic =>
      Ok(Json.toJson(statistic))
    }
  }
}
