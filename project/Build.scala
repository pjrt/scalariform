import sbt._
import sbt.Keys._
import com.github.retronym.SbtOneJar
import com.typesafe.sbteclipse.core.EclipsePlugin.EclipseKeys._
import com.typesafe.sbteclipse.core.EclipsePlugin._

import com.typesafe.sbtscalariform.ScalariformPlugin
import com.typesafe.sbtscalariform.ScalariformPlugin.ScalariformKeys
import scalariform.formatter.preferences._

object ScalariformBuild extends Build {

  lazy val buildSettings = Defaults.defaultSettings ++ ScalariformPlugin.defaultScalariformSettings ++ Seq(
    organization := "scalariform",
    version      := "0.1.2-SNAPSHOT",
    scalaVersion := "2.9.2",
    crossScalaVersions := Seq("2.8.0", "2.8.1", "2.8.2", "2.9.0", "2.9.0-1", "2.9.1", "2.9.2", "2.10.0-SNAPSHOT"),
    resolvers += ScalaToolsSnapshots,
    retrieveManaged := true,
    scalacOptions += "-deprecation",
    pomExtra := pomExtraXml,
    parallelExecution in Test := false,
    publishMavenStyle := true,
    credentials += Credentials(Path.userHome / ".ivy2" / ".credentials"),
    EclipseKeys.withSource := true,
    EclipseKeys.eclipseOutput := Some("bin"))

  lazy val subprojectSettings = buildSettings ++ Seq(
     ScalariformKeys.preferences <<= baseDirectory.apply(dir => PreferencesImporterExporter.loadPreferences((dir / ".." / "formatterPreferences.properties").getPath))	
  )

  lazy val root: Project = Project("root", file("."), settings = buildSettings) aggregate(scalariform, gui, perf, corpusScan, scalariformCli)

  lazy val scalariform: Project = Project("scalariform", file("scalariform"), settings = subprojectSettings ++ 
    Seq(
      libraryDependencies <<= (scalaVersion, libraryDependencies) { (sv, deps) =>
         val scalatestVersion = sv match {
           case "2.8.0"           => "org.scalatest" %% "scalatest"       % "1.3.1.RC2" % "test"
          
//           case "2.8.1"           => "org.scalatest" %% "scalatest"       % "1.5.1"     % "test"
//           case "2.8.2"           => "org.scalatest" %% "scalatest"       % "1.5.1"     % "test"
           case "2.10.0-SNAPSHOT" => "org.scalatest" %  "scalatest_2.10.0-M2" % "1.8-SNAPSHOT"     % "test"
           case _                 => "org.scalatest" %% "scalatest"       % "1.7.2"     % "test"
         }
         deps :+ scalatestVersion
      },
      exportJars := true, // Needed for scalariformCli oneJar
      publishTo <<= version { (v: String) =>
        if (v endsWith "-SNAPSHOT")
          Some(ScalaToolsSnapshots)
        else
          Some(ScalaToolsReleases)
	      }
    ),
    delegates = root :: Nil)

  lazy val scalariformCli = Project("scalariform-cli", file("scalariform-cli"), settings = subprojectSettings ++ SbtOneJar.oneJarSettings ++
    Seq(
      libraryDependencies += "commons-io" % "commons-io" % "1.4",
      mainClass in (Compile, packageBin) := Some("scalariform.commandline.Main"),
      artifactName in SbtOneJar.oneJar := { (config: String, module: ModuleID, artifact: Artifact) => artifact.name + "." + artifact.extension }
    )) dependsOn(scalariform)

  lazy val perf: Project = Project("perf", file("perf"), settings = subprojectSettings) dependsOn(scalariform)

  lazy val corpusScan: Project = Project("corpusscan", file("corpusscan"), settings = subprojectSettings ++
    Seq(
      libraryDependencies += "commons-io" % "commons-io" % "1.4"
    )) dependsOn(scalariform)

  lazy val gui: Project = Project("gui", file("gui"), settings = subprojectSettings ++
    Seq(
      libraryDependencies += "com.miglayout" % "miglayout" % "3.7.4",
      mainClass in (Compile, run) := Some("scalariform.gui.Main")
    )) dependsOn(scalariform)

   def pomExtraXml =
      <inceptionYear>2010</inceptionYear>
      <url>http://github.com/mdr/scalariform</url>
      <licenses>
        <license>
          <name>MIT License</name>
          <url>http://www.opensource.org/licenses/mit-license.php</url>
          <distribution>repo</distribution>
        </license>
      </licenses>

}
