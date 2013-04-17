package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers


class ContentApiSanityTest extends FlatSpec with ShouldMatchers with Http {

  val CacheControl = """public, max-age=(\d+)""".r

  "Content api" should "serve gzipped" in {

    val connection = GET(
      "http://content.guardianapis.com/search?format=json",
      compress = true
    )

    connection.bodyFromGzip should include("results")
  }
}
