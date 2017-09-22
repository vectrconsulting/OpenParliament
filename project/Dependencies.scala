import sbt._


object Dependencies {

  val resolutionRepos = Seq(
    "RepoOne" at "https://repo1.maven.org/maven2/",
    "MVNRepository" at "https://mvnrepository.com/",
    "Twitter Maven Repo" at "http://maven.twttr.com/",
    "Finatra Repo" at "http://twitter.github.com/finatra",
    "Sonatype Releases" at "https://oss.sonatype.org/content/repositories/releases/",
    "Typesafe Releases" at "http://repo.typesafe.com/typesafe/releases/",
    "conjars.org" at "http://conjars.org/repo/",
    "bintray" at "https://dl.bintray.com/readytalk/maven/",
    "Flyway" at "https://flywaydb.org/repo"
  )


  val logbackVersion = "1.1.7"
  val neo4jVersion = "3.2.3"
  val graphawareVersion = "3.1.4.49"
  val graphawareTimeTreeVersion = "3.1.4.49.28-SNAPSHOT"
  val apocVersion = "3.1.3.7"
  val typesafeConfigVersion = "1.3.0" // was 1.2.1
  val flywayVersion = "4.2.0"

  def compileStage(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "compile")

  def providedStage(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "provided")

  def providedStageSeq(deps: Seq[ModuleID]): Seq[ModuleID] = deps map (_ % "provided")

  def testStage(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "test")

  def compileStageWithClassifier(deps: ModuleID*): Seq[ModuleID] =
    deps map (_ % "compile" classifier "tests" withSources() withJavadoc())

  def testStageWithClassifier(deps: ModuleID*): Seq[ModuleID] =
    deps map (_ % "test" classifier "tests" withSources() withJavadoc())

  def runtimeStage(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "runtime")

  def containerStage(deps: ModuleID*): Seq[ModuleID] = deps map (_ % "container")

  val csvReader = "au.com.bytecode" % "opencsv" % "2.4" exclude("log4j", "log4j")
  val csvReaderStream = "com.github.tototoshi" %% "scala-csv" % "1.3.3" exclude("log4j", "log4j")
  val neo4jKernel = "org.neo4j" % "neo4j-kernel" % neo4jVersion exclude("log4j", "log4j")
  val neo4jIO = "org.neo4j" % "neo4j-io" % neo4jVersion exclude("log4j", "log4j")
  val neo4j = "org.neo4j" % "neo4j" % neo4jVersion exclude("log4j", "log4j")
  val neo4jDriver = "org.neo4j.driver" % "neo4j-java-driver" % "1.0.4" exclude("log4j", "log4j")
  val neo4jServer = "org.neo4j.app" % "neo4j-server" % neo4jVersion exclude("log4j", "log4j")
  val neo4jTestHarness = "org.neo4j.test" % "neo4j-harness" % neo4jVersion exclude("log4j", "log4j")
  val jerseyCore = "com.sun.jersey" % "jersey-core" % "1.19" exclude("log4j", "log4j")
  val jerseyClient = "org.glassfish.jersey.core" % "jersey-client" % "2.22.1" exclude("log4j", "log4j")
  val jerseyServer = "org.glassfish.jersey.core" % "jersey-server" % "2.22.1" exclude("log4j", "log4j")
  val commons = "commons-codec" % "commons-codec" % "1.9" exclude("log4j", "log4j")
  val commonslang = "org.apache.commons" % "commons-lang3" % "3.5" exclude("log4j","log4j")
  val apoc = "org.neo4j.procedure" % "apoc" % apocVersion exclude("log4j","log4j")

  val statsd = Seq(
    "com.timgroup" % "java-statsd-client" % "3.0.1" withSources() withJavadoc()
  ).map(_.exclude("log4j", "log4j"))

  val jetty = Seq(
    "org.eclipse.jetty" % "jetty-server" % "9.3.16.v20170120",
    "org.eclipse.jetty" % "jetty-webapp" % "9.3.16.v20170120"
  )

  val graphaware = Seq(
    "com.graphaware.neo4j" % "runtime" % graphawareVersion % "compile",
    "com.graphaware.neo4j" % "server" % graphawareVersion % "compile",
    "com.graphaware.neo4j" % "timetree" % graphawareTimeTreeVersion % "compile"
  )

  val neo4jDependencies = Seq(
    "org.neo4j" % "neo4j-io" % neo4jVersion exclude("log4j", "log4j"),
    "org.neo4j" % "neo4j-cypher" % neo4jVersion exclude("log4j", "log4j")
      exclude("org.neo4j", "neo4j-cypher-compiler"),
    "org.neo4j.app" % "neo4j-server" % neo4jVersion exclude("log4j", "log4j"),
    "org.neo4j.driver" % "neo4j-java-driver" % "1.0.4" exclude("log4j", "log4j")
  )

  val finatraVersion = "2.12.0"

  val finatraDependencies =Seq(
  "com.twitter" %% "finatra-http" % finatraVersion,
  "com.twitter" %% "finatra-httpclient" % finatraVersion,
  "com.twitter" %% "finatra-thrift" % finatraVersion,
  "com.twitter" %% "finatra-utils" % finatraVersion,
  "com.twitter" %% "finatra-jackson" % finatraVersion,
  "com.twitter" %% "inject-server" % finatraVersion,
  "com.twitter" %% "inject-app" % finatraVersion ,
  "com.twitter" %% "inject-core" % finatraVersion,
  "com.twitter" %% "inject-modules" % finatraVersion,
  "com.twitter" %% "inject-thrift-client-http-mapper" % finatraVersion,

  // for the monoapp we have included all test libs in compile stage
  "com.twitter" %% "finatra-http" % finatraVersion % "compile" classifier "tests",
  "com.twitter" %% "finatra-thrift" % finatraVersion % "compile" classifier "tests",
  "com.twitter" %% "finatra-jackson" % finatraVersion % "compile" classifier "tests",
  "com.twitter" %% "inject-server" % finatraVersion % "compile" classifier "tests",
  "com.twitter" %% "inject-app" % finatraVersion % "compile" classifier "tests",
  "com.twitter" %% "inject-core" % finatraVersion % "compile" classifier "tests",
  "com.twitter" %% "inject-modules" % finatraVersion % "compile" classifier "tests",

  "com.twitter" %% "bijection-util" % "0.9.2",

  "com.google.inject.extensions" % "guice-testlib" % "4.0" % "test"

  ).map(_.exclude("log4j", "log4j")).map(_.exclude("javax.servlet","servlet-api"))

  val logbackElastic = Seq(
    "net.logstash.logback" % "logstash-logback-encoder" % "4.3",
    "ch.qos.logback" % "logback-classic" % logbackVersion,
    "ch.qos.logback" % "logback-core" % logbackVersion
  ).map(_.exclude("log4j", "log4j"))

  val dependencies = Seq(
    "junit" % "junit" % "4.12" % "test",
    "com.typesafe" % "config" % typesafeConfigVersion,
    "org.scalatest" %% "scalatest" %  "3.0.1" % "test"
  ).map(_.exclude("log4j", "log4j"))

}
