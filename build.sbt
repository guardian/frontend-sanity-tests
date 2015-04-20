resolvers := Seq(Classpaths.typesafeResolver,
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)

libraryDependencies += "org.scalatest" %% "scalatest" % "2.1.5" % "test"

libraryDependencies += "org.apache.httpcomponents" % "httpclient" % "4.3.4"

libraryDependencies += "com.typesafe.play" %% "play-json" % "2.3.2"