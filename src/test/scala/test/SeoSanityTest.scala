package test

import org.scalatest.{OptionValues, FlatSpec}
import org.scalatest.matchers.ShouldMatchers
import play.api.libs.json._

class SeoSanityTest extends FlatSpec with ShouldMatchers with Http with OptionValues {

  def checkUrl(url: String): Unit = {
    val connection = GET(
      s"http://linter.structured-data.org/?url=$url",
      compress = true,
      headers = Seq(
        "Accept" -> "application/json"
      )
    )

    val json: JsValue = Json.parse(connection.body)

    val messages = (json \ "messages").asOpt[JsArray]

    withClue(s"${messages.value.as[Seq[String]].mkString("\n")}\n") {
      messages.value.value.size should be(0)
    }
  }

  "The network front" should "serve correct and valid seo meta data" in {
      checkUrl("http://www.theguardian.com/uk")
  }

  "Tag pages" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/world/series/guardian-world-networks")
  }

  "Article pages" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/politics/commentisfree/2015/apr/16/tv-opposition-leaders-debate-spin-room-westminster-artless-bullshit")
  }

  "Live blogs" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/football/live/2015/apr/18/reading-arsenal-fa-cup-semi-final-live")
  }

  "Gallery pages" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/artanddesign/gallery/2015/apr/15/postcards-from-the-ruins-the-desolate-edge-of-eastern-europe-tamas-dezso-in-pictures")
  }

  "Video pages" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/lifeandstyle/video/2015/apr/17/motivation-finish-studies-problem-agony-aunt-video")
  }

  "Interactive pages" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/environment/ng-interactive/2015/apr/16/gates-foundation-wellcome-trust-climate-change-divest-fossil-fuels-guardian")
  }
}
