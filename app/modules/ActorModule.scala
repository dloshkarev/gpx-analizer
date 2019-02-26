package modules

import actors.{ActivityProcessActor, EmailSendActor, UnzipActor}
import com.google.inject.AbstractModule
import play.api.libs.concurrent.AkkaGuiceSupport

/**
  * Module contains akka actors configuration
  */
class ActorModule extends AbstractModule with AkkaGuiceSupport {
  override def configure() = {
    //TODO: добавить периодическую задачу по удалению обработанных запросов с истекшим сроком действия
    bindActor[UnzipActor]("unzipActor")
    bindActor[ActivityProcessActor]("activityProcessActor")
    bindActor[EmailSendActor]("emailSendActor")
  }
}
