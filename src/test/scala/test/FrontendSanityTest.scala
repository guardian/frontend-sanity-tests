package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import System._


class FrontendSanityTest extends FlatSpec with ShouldMatchers with Http {

  val CacheControl = """max-age=(\d+), s-maxage=(\d+), stale-while-revalidate=(\d+), stale-if-error=(\d+)""".r

  "m.guardian.co.uk" should "serve with correct headers with no gzip" in {

    val connection = GET(
      s"http://m.guardian.co.uk/?cachebust=${currentTimeMillis}",
      compress = false
    )

    connection.body should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(maxAge, sMaxAge, _, _) =>
        maxAge.toInt should be > 50
        sMaxAge.toInt should be > 50

      case _ => fail("Bad cache control")
    }
  }

  it should "serve with correct headers with gzip" in {

    val connection = GET(
      s"http://m.guardian.co.uk/?cachebust=${currentTimeMillis}",
      compress = true
    )

    connection.bodyFromGzip should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(maxAge, _, _, _) => maxAge.toInt should be > 50
      case _ => fail("Bad cache control")
    }
  }

  it should "compress json" in {
    val connection = GET(
      s"http://m.guardian.co.uk/commentisfree/trails?callback=trails&cachebust=${currentTimeMillis}",
      compress = true
    )

    connection.bodyFromGzip should include("""{"html":""")

    connection.header("Vary") should include ("Accept-Encoding")
    connection.header("Content-Type") should be ("application/javascript")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(maxAge, _, _, _) => maxAge.toInt should be > 50
      case _ => fail("Bad cache control")
    }
  }

  "m.guardiannews.com" should "serve with correct headers with no gzip" in {

    val connection = GET(
      s"http://m.guardiannews.com/?cachebust=${currentTimeMillis}",
      compress = false
    )

    connection.body should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(maxAge, _, _, _) => maxAge.toInt should be > 50
      case _ => fail("Bad cache control")
    }
  }

  it should "serve with correct headers with gzip" in {

    val connection = GET(
      s"http://m.guardiannews.com/?cachebust=${currentTimeMillis}",
      compress = true
    )

    connection.bodyFromGzip should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(maxAge, _, _, _) => maxAge.toInt should be > 50
      case _ => fail("Bad cache control")
    }
  }

  it should "compress json" in {
    val connection = GET(
      s"http://m.guardiannews.com/commentisfree/trails?callback=trails&cachebust=${currentTimeMillis}",
      compress = true
    )

    connection.bodyFromGzip should include("""{"html":""")

    connection.header("Vary") should be ("Origin,Accept,Accept-Encoding")
    connection.header("Content-Type") should be ("application/javascript")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(maxAge, _, _, _) => maxAge.toInt should be > 50
      case _ => fail("Bad cache control")
    }
  }
}


