addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.6.0")

addSbtPlugin("com.gu" % "sbt-teamcity-test-reporting-plugin" % "1.5")

resolvers := Seq(Classpaths.typesafeResolver,
  "Typesafe Repo" at "http://repo.typesafe.com/typesafe/releases/"
)