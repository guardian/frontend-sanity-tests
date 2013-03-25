package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import System._


class OriginSanityTest extends FlatSpec with ShouldMatchers with Http {

   val CacheControl = """public, max-age=(\d+)""".r

   "beta-origin.guardian.co.uk" should "serve with correct headers for m.guardian.co.uk" in {

     val connection = GET(
       s"http://beta-origin.guardian.co.uk/?cachebust=${currentTimeMillis}",
       compress = false,
      headers = Seq(("host" -> "m.guardian.co.uk"))
     )

     connection.body should include("The Guardian")

     connection.header("Vary") should be ("Accept-Encoding")
     connection.header("Content-Type") should be ("text/html; charset=utf-8")
     connection.responseCode should be (200)
     connection.header("Cache-Control") match {
       case CacheControl(seconds) => seconds.toInt should be > 50
       case _ => fail("Bad cache control")
     }
   }

  it should "serve with correct headers for m.guardiannews.com" in {

    val connection = GET(
      s"http://beta-origin.guardian.co.uk/?cachebust=${currentTimeMillis}",
      compress = false,
      headers = Seq(("host" -> "m.guardiannews.com"))
    )

    connection.body should include("The Guardian")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("text/html; charset=utf-8")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(seconds) => seconds.toInt should be > 50
      case _ => fail("Bad cache control")
    }
  }
}


