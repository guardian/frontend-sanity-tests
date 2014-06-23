package test

import org.apache.http.client.methods.{CloseableHttpResponse, HttpGet}
import org.apache.http.client.protocol.{RequestAcceptEncoding, ResponseContentEncoding}
import org.apache.http.impl.client.HttpClients
import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers

import scala.io.Source


class StaticFilesSanityTest extends FlatSpec with ShouldMatchers with Http {

  val CacheControl = """public, max-age=(\d+)""".r

  "assets.guim.co.uk" should "serve css assets without compression" in {

    val connection = GET(
      s"http://assets.guim.co.uk/stylesheets/main.min.3658efd6a677794423bdd3839d503289.css?cacheBust=${System.currentTimeMillis}",
      compress = false
    )


    connection.body should include(".table-football-header{border-bottom:1px solid #DEDEDD;}")

    connection.header("Vary") should be ("Origin,Accept,Accept-Encoding")
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

    connection.header("Vary") should be ("Origin,Accept,Accept-Encoding")
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

    connection.header("Vary") should be ("Origin,Accept,Accept-Encoding")
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

    connection.header("Vary") should be ("Origin,Accept,Accept-Encoding")
    connection.header("Content-Type") should be ("application/x-javascript")
    connection.responseCode should be (200)
    connection.header("Cache-Control") match {
      case CacheControl(seconds) => seconds.toInt should be > 100000
      case _ => fail("Bad cache control")
    }
  }

  it should "add the Access-Control-Allow-Origin header" in {

    val connection = GET(
      s"http://assets.guim.co.uk/stylesheets/main.min.3658efd6a677794423bdd3839d503289.css?cacheBust=${System.currentTimeMillis}",
      compress = true,
      headers = Seq("Origin" -> "www.theguardian.com")
    )

    connection.header("Access-Control-Allow-Origin").trim should be ("*")
    connection.header("Access-Control-Allow-Credentials").trim should be ("true")
    connection.header("Access-Control-Allow-Headers").trim should be ("GET")
  }

  it should "serve 404s" in {
    val connection = GET(s"http://assets.guim.co.uk/javascripts/does-not-exist.js?cacheBust=${System.currentTimeMillis}")
    connection.responseCode should be (404)
  }
}

class Response(val response: CloseableHttpResponse) {
  private val entity = response.getEntity
  lazy val body = Source.fromInputStream(entity.getContent).getLines().mkString("")
  lazy val bodyFromGzip = body//Source.fromInputStream(new GZIPInputStream(entity.getContent)).getLines().mkString("")

  lazy val responseCode = response.getStatusLine.getStatusCode

  lazy val responseMessage = response.getStatusLine.getReasonPhrase

  def header(name: String) = response.getFirstHeader(name).getValue

  def disconnect() { response.close() }
}

trait Http {

  def GET(url: String, compress: Boolean = false, headers: Seq[(String, String)] = Nil): Response = {

    val client = if (compress) {
      HttpClients.custom().addInterceptorFirst(new RequestAcceptEncoding).addInterceptorLast(new ResponseContentEncoding).build()
    } else {
      HttpClients.createDefault()
    }

    val request = new HttpGet(url)

    headers.foreach{
      case (key, value) => request.setHeader(key, value)
    }

    val response = client.execute(request)
    new Response(response)
  }
}


