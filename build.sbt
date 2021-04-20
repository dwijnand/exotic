val exotic = project in file(".")

inThisBuild(Def.settings(
    organization  := "com.dwijnand",
         version  := "1.3.0-SNAPSHOT",
    scalaVersion  := "2.13.5",
   scalacOptions ++= List("-deprecation", "-feature", "-language:_", "-unchecked", "-Xlint"),
   scalacOptions  += "-Wunused:-imports",
))

Compile / javaSource := (Compile / javaSource).value / "com.github.forax.exotic"
Test    / javaSource := (Test    / javaSource).value / "com.github.forax.exotic"
sourcesInBase := false

libraryDependencies ++= List(
  "net.aichler"        % "jupiter-interface"      % JupiterKeys.jupiterVersion.value % Test,
  "org.apiguardian"    % "apiguardian-api"        % "1.0.0"                          % Test,
  "org.junit.jupiter"  % "junit-jupiter-api"      % "5.1.0"                          % Test,
  "org.junit.platform" % "junit-platform-commons" % "1.1.0"                          % Test,
  "org.opentest4j"     % "opentest4j"             % "1.0.0"                          % Test,
)

enablePlugins(JmhPlugin)

inConfig(Jmh)(Def.settings(
      sourceDirectory := (Test /     sourceDirectory).value,
       classDirectory := (Test /      classDirectory).value,
  dependencyClasspath := (Test / dependencyClasspath).value,
              compile := compile.dependsOn(Test / compile).value,
                  run := run.dependsOn(compile).evaluated,
))
