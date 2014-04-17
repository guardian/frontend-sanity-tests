package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import System._


class FrontendSanityTest extends FlatSpec with ShouldMatchers with Http {

  // caching coming through Fastly
  val HtmlCacheControl = """max-age=(\d+), private""".r
  val AjaxCacheControl = """max-age=(\d+), private""".r

  "www.theguardian.com" should "serve with correct headers with no gzip" in {

    val connection = GET(
      s"http://www.theguardian.com/uk?view=mobile",
      compress = false
    )

    connection.body should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding,User-Agent")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case HtmlCacheControl(maxAge) =>
        maxAge.toInt should be > 0
      case bad => fail("Bad cache control" + bad)
    }
  }

  it should "serve with correct headers with gzip" in {

    val connection = GET(
      s"http://www.theguardian.com/uk?view=mobile",
      compress = true
    )

    connection.bodyFromGzip should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding,User-Agent")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case HtmlCacheControl(maxAge) => maxAge.toInt should be > 0
      case bad => fail("Bad cache control" + bad)
    }
  }

  it should "compress json" in {
    val connection = GET(
      s"http://api.nextgen.guardianapps.co.uk/top-stories/trails.json?page-size=10&view=link&_edition=UK&cachebust=$currentTimeMillis",
      compress = true
    )

    connection.bodyFromGzip should include("""{"html":""")

    connection.header("Vary") should be ("Accept, Origin, Accept-Encoding")
    connection.header("Content-Type") should be ("application/json; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case AjaxCacheControl(maxAge) => maxAge.toInt should be > 0
      case bad => fail("Bad cache control" + bad)
    }
  }
}


