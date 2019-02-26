package forms

import play.api.data.Forms._
import play.api.data._

/**
  * Form with data of user who uploaded activities archive
  */
object ActivitiesUploadForm {
  val form = Form(
    "email" -> text
  )
}
