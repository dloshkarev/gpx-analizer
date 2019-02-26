package actors

import actors.EmailSendActor.Send
import akka.actor.{Actor, ActorLogging}

object EmailSendActor {
  case class Send(email: String)
}

/**
  * Send an email
  */
class EmailSendActor extends Actor with ActorLogging {
  override def receive: Receive = {
    case Send(email) => {
      log.info(s"EmailSendActor.Send: email = $email")

    }
  }
}
