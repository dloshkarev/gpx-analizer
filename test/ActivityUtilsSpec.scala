import org.scalatest.{ FlatSpec, Matchers }
import utils.ActivityUtils

class ActivityUtilsSpec extends FlatSpec with Matchers {

  "Int" should "convert to String" in {
    ActivityUtils.int2TimeString(0) shouldBe "00"
    ActivityUtils.int2TimeString(1) shouldBe "01"
    ActivityUtils.int2TimeString(10) shouldBe "10"
  }

  "Time" should "convert to String" in {
    ActivityUtils.time2String(10.5) shouldBe "10:30"
    ActivityUtils.time2String(9.5) shouldBe "09:30"
    ActivityUtils.time2String(9.1) shouldBe "09:06"
    ActivityUtils.time2String(0) shouldBe "00:00"
    ActivityUtils.time2String(70.5) shouldBe "01:10:30"
  }
}
