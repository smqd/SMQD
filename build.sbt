
import com.typesafe.sbt.SbtNativePackager.autoImport.NativePackagerHelper._
import sbt.Keys._
import sbt.StdoutOutput

import scala.sys.process._

val smqdVersion = "0.4.0-SNAPSHOT"

lazy val npmBuildTask = taskKey[Unit]("build ui")
npmBuildTask := {
  println("Building JavaScript ui ...")
  val uiDir = new File("./ui")
  val npmInstall = Process("npm install", uiDir)
  val npmBuild   = Process("npm run build", uiDir)
  val res = npmInstall #&& npmBuild !

  println(s"Build: '$res'")
}

lazy val npmCleanTask = taskKey[Unit]("clean ui")
npmCleanTask := {
  println("Cleaning JavaScript output")
  val rmAssets = Process("rm -rf ./src/main/resources/dashboard")
  val res = rmAssets.!
  println(s"Clean: $res")
}

val smqd = project.in(file(".")).enablePlugins(
  JavaAppPackaging, AutomateHeaderPlugin
).settings(
  organization := "com.thing2x",
  name := "smqd",
  version := smqdVersion,
  // no publish
  publish := ((): Unit),
  publishLocal := ((): Unit),
  publishArtifact := false
).settings(
  libraryDependencies ++= Seq (
    if (smqdVersion.endsWith("-SNAPSHOT"))
      "com.thing2x" %% "smqd-core" % smqdVersion changing()
    else
      "com.thing2x" %% "smqd-core" % smqdVersion
  ),
  resolvers += Resolver.sonatypeRepo("public")
).settings(
  (stage in Universal) := ((stage in Universal) dependsOn npmBuildTask).value,
  (dist in Universal)  := ((dist in Universal) dependsOn npmBuildTask).value,
  (compile  in Compile):= ((compile in Compile) dependsOn npmBuildTask).value,
  (clean in ThisBuild) := ((clean in ThisBuild) dependsOn npmCleanTask).value
).settings(
  // License
  organizationName := "UANGEL",
  startYear := Some(2018),
  licenses += ("Apache-2.0", new URL("https://www.apache.org/licenses/LICENSE-2.0.txt")),
  headerMappings := headerMappings.value + (HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment),
  headerMappings := headerMappings.value + (HeaderFileType.sh -> HeaderCommentStyle.hashLineComment),
  headerMappings := headerMappings.value + (HeaderFileType.conf -> HeaderCommentStyle.hashLineComment)
).settings(
  // sbt runtime options
  javaOptions in run ++= Seq(
    "-Xmx1G",
    "-Dconfig.file=./conf/smqd.conf",
    "-Dlogback.configurationFile=./conf/logback.xml",
    "-Djava.net.preferIPv4Stack=true",
    "-Djava.net.preferIPv6Addresses=false"
  ),
  fork in run := true,
  outputStrategy := Some(StdoutOutput)
).settings(
  // Packaging Settings
  mappings in Universal ++= directory(sourceDir = "bin").filterNot{ case (_, fname) => Set("bin/.gitkeep").contains(fname) },
  mappings in Universal ++= directory(sourceDir = "conf").filterNot{ case (_, fname) => Set("conf/smqd-dev.conf").contains(fname) },
  mappings in Universal ++= directory(sourceDir = "plugin").filterNot{ case (_, fname) => Set("plugin/.gitkeep").contains(fname) },
  mainClass in Compile := Some("com.thing2x.smqd.Main"),
  packageName in Universal := s"smqd-v$smqdVersion",
  executableScriptName := "smqd",
  bashScriptConfigLocation := Some("${SMQD_HOME_DIR}/conf/smqd-jvm.ini"),
  // Not need for production,
  // bashScriptExtraDefines ++= Seq("""addJava "-DAPP_HOME=$(dirname $app_home)" """)
  // scriptClasspath := Seq("${app_home}/../conf") ++ scriptClasspath.value
)