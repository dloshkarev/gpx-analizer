package actors

import java.io.{File, FileInputStream, FileOutputStream}
import java.nio.file.{Files, Paths}
import java.util.UUID
import java.util.zip.ZipInputStream

import actors.UnzipActor.Unzip
import akka.actor.{Actor, ActorLogging, ActorRef}
import javax.inject.{Inject, Named}
import models.exception.ActivityProcessException

object UnzipActor {

  /**
    * Unzip activities archive and start processing files
    *
    * @param userEmail       user email
    * @param archiveFilePath activities archive temp file path
    */
  case class Unzip(userEmail: String, archiveFilePath: String)

}

/**
  * Actor for unzip activities archive, split them by fixed size parts and start processing each part separately
  *
  * @param activityProcessActor reference to activities processing actor
  */
class UnzipActor @Inject()(@Named("activityProcessActor") activityProcessActor: ActorRef) extends Actor with ActorLogging {

  /**
    * Unzip activities into temp directory
    *
    * @param archive input activities archive
    * @return temp directory with unzipped activities
    */
  private def unzipActivities(archive: File): File = {
    val zis = new ZipInputStream(new FileInputStream(archive))
    try {
      val tempDirectory = Files.createTempDirectory("temp" + System.nanoTime().toString)
      Stream.continually(zis.getNextEntry).takeWhile(_ != null).filter(_.getName.endsWith("-Run.gpx")).foreach { file =>
        val fout = new FileOutputStream(tempDirectory.toAbsolutePath + File.separator + file.getName)
        val buffer = new Array[Byte](1024)
        Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(fout.write(buffer, 0, _))
      }
      tempDirectory.toFile
    } finally {
      zis.close()
      //archive.delete()
    }
  }

  override def receive: Receive = {
    case Unzip(userEmail: String, archiveFilePath: String) => {
      log.info(s"UnzipActor.Unzip: userEmail = $userEmail, archiveFilePath = $archiveFilePath")

      if (Files.exists(Paths.get(archiveFilePath))) {
        val requestGuid = UUID.randomUUID().toString
        log.info(s"requestGuid = $requestGuid")
        // Unzip activities to temp directory
        val tempDirectory = unzipActivities(new File(archiveFilePath))

        // Start process files
        activityProcessActor ! ActivityProcessActor.Process(requestGuid, userEmail, tempDirectory)
      } else throw ActivityProcessException(s"Archive $archiveFilePath doesn't exist")
    }
  }
}
