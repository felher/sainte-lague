import org.scalajs.linker.interface.ModuleSplitStyle

val scala3Version = "3.4.2"

lazy val root = project
  .in(file("."))
  .enablePlugins(ScalaJSPlugin)
  .settings(
    organization                    := "org.felher",
    name                            := "sainte-lague",
    version                         := "1.0.0",
    scalaVersion                    := scala3Version,
    scalaJSUseMainModuleInitializer := true,
    scalaJSLinkerConfig ~= {
      _.withModuleKind(ModuleKind.ESModule)
        .withModuleSplitStyle(ModuleSplitStyle.SmallModulesFor(List("org.felher.sainte_lague")))
    },
    libraryDependencies ++= Seq(
      "org.scala-js"  %%% "scalajs-dom"               % "2.4.0",
      "org.felher"    %%% "beminar"                   % "1.0.0",
      "io.circe"      %%% "circe-core"                % "0.14.7",
      "com.raquo"     %%% "laminar"                   % "17.0.0",
      "io.circe"      %%% "circe-generic"             % "0.14.7",
      "io.circe"      %%% "circe-parser"              % "0.14.7",
      ("org.scala-js" %%% "scalajs-java-securerandom" % "1.0.0").cross(CrossVersion.for3Use2_13)
    )
  )
