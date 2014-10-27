package test

import org.scalatest.FlatSpec
import org.scalatest.matchers.ShouldMatchers
import System._


class AmiSanityTest extends FlatSpec with ShouldMatchers with Http {

  "www.theguardian.com" should "be on the latest version of the AMI" in {

    // if this test fails you probably need to update the stack to the latest AMI (and update this test)

    val connection = GET(
      "http://aws.amazon.com/amazon-linux-ami/",
      compress = false
    )

    connection.body should include("ami-9c7ad8eb")
  }
}
