package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers


class FrontendSanityTest extends FlatSpec with ShouldMatchers with Http {

  // caching coming through Fastly
  val HtmlCacheControl = """max-age=(\d+), stale-while-revalidate=(\d+), stale-if-error=(\d+), private""".r
  val AjaxCacheControl = """max-age=(\d+), stale-while-revalidate=(\d+), stale-if-error=(\d+), private""".r

  "www.theguardian.com" should "serve with correct headers with no gzip" in {

    val connection = GET(
      s"http://www.theguardian.com/uk?view=mobile",
      compress = false
    )

    connection.body should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding,User-Agent")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.header("X-GU-Platform") should be ("next-gen-router")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case HtmlCacheControl(maxAge, _, _) =>
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
    connection.header("X-GU-Platform") should be ("next-gen-router")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case HtmlCacheControl(maxAge, _, _) => maxAge.toInt should be > 0
      case bad => fail("Bad cache control" + bad)
    }
  }

  it should "compress json" in {
    val connection = GET(
      s"http://api.nextgen.guardianapps.co.uk/most-read/world.json",
      compress = true,
      headers = Seq(
        "Accept" -> "application/json",
        "Origin" -> "http://www.theguardian.com"
      )
    )

    connection.body should include("""{"html":""")

    connection.responseCode should be (200)
    connection.header("Content-Type") should be ("application/json; charset=utf-8")
    connection.header("Cache-Control") match {
      case AjaxCacheControl(maxAge, _, _) => maxAge.toInt should be > 0
      case bad => fail("Bad cache control" + bad)
    }
    connection.header("Vary") should be ("Accept-Encoding,Origin,Accept")
  }
}


