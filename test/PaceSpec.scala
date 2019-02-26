import models.Pace
import org.scalatest.{FlatSpec, Matchers}

class PaceSpec extends FlatSpec with Matchers {

  "Pace" should "convert to seconds" in {
    Pace(3, 0).asSeconds shouldBe 180
  }

  "Pace" should "convert to String" in {
    Pace(3, 0).toString shouldBe "03:00"
    Pace(3, 5).toString shouldBe "03:05"
    Pace(3, 10).toString shouldBe "03:10"
    Pace(10, 0).toString shouldBe "10:00"
    Pace(10, 5).toString shouldBe "10:05"
    Pace(10, 10).toString shouldBe "10:10"
  }

  "Pace" should "be comparable" in {
    Pace(3, 0) > Pace(4, 0) shouldBe true
    Pace(3, 5) > Pace(4, 0) shouldBe true
    Pace(3, 5) > Pace(4, 5) shouldBe true
    Pace(5, 0) > Pace(4, 0) shouldBe false
  }
}
