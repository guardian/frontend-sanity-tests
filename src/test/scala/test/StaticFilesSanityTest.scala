package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.net.{HttpURLConnection, URL}
import io.Source
import java.util.zip.{DeflaterInputStream, GZIPInputStream}



class StaticFilesSanityTest extends FlatSpec with ShouldMatchers with Http {

  val CacheControl = """public, max-age=(\d+)""".r

  "assets.guim.co.uk" should "serve css assets without compression" in {

    val connection = GET(
      s"http://assets.guim.co.uk/stylesheets/main.min.3658efd6a677794423bdd3839d503289.css?cacheBust=${System.currentTimeMillis}",
      compress = false
    )


    connection.body should include(".table-football-header{border-bottom:1px solid #DEDEDD;}")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("text/css")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(seconds) => seconds.toInt should be > 100000
      case _ => fail("Bad cache control")
    }
  }

  it should "serve css assets with compression" in {

    val connection = GET(
      s"http://assets.guim.co.uk/stylesheets/main.min.3658efd6a677794423bdd3839d503289.css?cacheBust=${System.currentTimeMillis}",
      compress = true
    )

    connection.bodyFromGzip should include(".table-football-header{border-bottom:1px solid #DEDEDD;}")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Encoding") should be ("gzip")
    connection.header("Content-Type") should be ("text/css")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(seconds) => seconds.toInt should be > 100000
      case _ => fail("Bad cache control")
    }
  }

  it should "serve javascript assets without compression" in {

    val connection = GET(
      s"http://assets.guim.co.uk/javascripts/bootstraps/app.1652e31709cbe1d89f0d9c0d1d0220a2.js?cacheBust=${System.currentTimeMillis}",
      compress = false
    )

    connection.body should include("return 0==aa.call(e).indexOf(\"[object \"+t)")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Type") should be ("application/x-javascript")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(seconds) => seconds.toInt should be > 100000
      case _ => fail("Bad cache control")
    }
  }

  it should "serve javascript assets with compression" in {

    val connection = GET(
      s"http://assets.guim.co.uk/javascripts/bootstraps/app.1652e31709cbe1d89f0d9c0d1d0220a2.js?cacheBust=${System.currentTimeMillis}",
      compress = true
    )

    connection.bodyFromGzip should include("return 0==aa.call(e).indexOf(\"[object \"+t)")

    connection.header("Vary") should be ("Accept-Encoding")
    connection.header("Content-Encoding") should be ("gzip")
    connection.header("Content-Type") should be ("application/x-javascript")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(seconds) => seconds.toInt should be > 100000
      case _ => fail("Bad cache control")
    }
  }

  it should "serve 404s" in {
    val connection = GET(s"http://assets.guim.co.uk/javascripts/does-not-exist.js?cacheBust=${System.currentTimeMillis}")
    connection.responseCode should be (404)
  }
}

class Response(val connection: HttpURLConnection) {
  lazy val body = Source.fromInputStream(connection.getInputStream).getLines().mkString("")
  lazy val bodyFromGzip = Source.fromInputStream(new GZIPInputStream(connection.getInputStream)).getLines().mkString("")

  lazy val responseCode = connection.getResponseCode

  def header(name: String) = connection.getHeaderField(name)

  def disconnect() { connection.disconnect() }
}

trait Http {

  def GET(url: String, compress: Boolean = false): Response = {

    println(System.getProperty("http.proxyHost"))

    //TODO proxy
    val connection = new URL(url).openConnection().asInstanceOf[HttpURLConnection]

    if (compress)
      connection.setRequestProperty("Accept-Encoding", "deflate,gzip")
    else
      connection.setRequestProperty("Accept-Encoding", "")

    connection.connect()

    new Response(connection)
  }

}


