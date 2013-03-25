package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import java.net._
import io.Source
import java.util.zip.GZIPInputStream
import scala.Some


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

  lazy val responseMessage = connection.getResponseMessage

  def header(name: String) = connection.getHeaderField(name)

  def disconnect() { connection.disconnect() }
}

trait Http {

  val proxy: Option[Proxy] = (Option(System.getProperty("http.proxyHost")), Option(System.getProperty("http.proxyPort"))) match {
    case (Some(host), Some(port)) => Some(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port.toInt)))
    case _ => None
  }

  def GET(url: String, compress: Boolean = false, headers: Seq[(String, String)] = Nil): Response = {

    val connection = proxy.map(p => new URL(url).openConnection(p)).getOrElse(new URL(url).openConnection()).asInstanceOf[HttpURLConnection]

    if (compress)
      connection.setRequestProperty("Accept-Encoding", "deflate,gzip")
    else
      connection.setRequestProperty("Accept-Encoding", "")

    headers.foreach{
      case (key, value) => connection.setRequestProperty(key, value)
    }
    new Response(connection)
  }
}


