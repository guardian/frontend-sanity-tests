resolvers := Seq(Classpaths.typesafeReleases,
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/",
  "Secured Central Repository" at "https://repo1.maven.org/maven2"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.2.4" % "test"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.4"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.2"