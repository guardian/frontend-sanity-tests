package test

import java.io.File

import org.scalatest.{FlatSpec, Matchers, OptionValues}
import play.api.libs.json._

import scala.sys.process._

class SeoSanityTest extends FlatSpec with Matchers with Http with OptionValues {

  def checkUrl(url: String, filter: Seq[String] => Seq[String] = identity): Unit = {

    val linterCommand = Process(Seq("/usr/bin/env", "ruby", "./linter.rb", url), new File("linter-master/"))

    val messagesJson = new StringBuilder()

    val exitCode = linterCommand ! ProcessLogger(
      out => {
        println(s"linter out: $out")
        messagesJson.append(out)
      },
      err => println(s"linter stderr: $err")
    )

    withClue("linter should return successful exit code, see above for stderr") {
      exitCode should be(0)
    }

    val json: JsValue = Json.parse(messagesJson.toString)

    val messages = json.as[JsArray].value.seq.map { _.as[String] }

    val filtered = filter(ignoreHtmlErrors(messages))

    withClue(s"${filtered.mkString("\n")}\n") {
      filtered.size should be(0)
    }
  }

  /**
   * this is needed because the validator validates just about everything even stuff we don't care about
   * @param messages
   */
  def ignoreHtmlErrors(messages: Seq[String]) = {
    val tag = ".*: Tag [^ ]* invalid".r
    val entity = ".*: htmlParseEntityRef: .*".r
    val scriptWithClose = ".*: Element script embeds close tag".r
    messages.filter {
      case tag() => false
      case entity() => false
      case scriptWithClose() => false
      case _ => true
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
    checkUrl("http://www.theguardian.com/football/live/2015/apr/18/reading-arsenal-fa-cup-semi-final-live", {
      // this is a temp hack as LiveBlogPosting is not on schema.org yet. Please remove if http://schema.org/LiveBlogPosting is there
      messages: Seq[String] =>
        messages.filter {
              case "class schema:LiveBlogPosting: No class definition found" => false
              case "property schema:liveBlogUpdate: No property definition found" => false
              case _ => true
            }
        }
    )
    // when this line fails, it's time to remove the above filters
    GET("http://schema.org/LiveBlogPosting").responseCode should be(404)
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

  "Review pages" should "serve correct and valid seo meta data" in {
    checkUrl("http://www.theguardian.com/stage/2015/jun/01/the-elephant-man-review-bradley-cooper-london")
  }

}
