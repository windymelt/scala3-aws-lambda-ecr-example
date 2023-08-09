import com.amazonaws.regions.Region
import com.amazonaws.regions.Regions

val scala3Version = "3.3.0"

// `sbt Docker/publishLocal` to build image to local.
// `sbt ecr:push` to push image to ECR.

lazy val root = project
  .in(file("."))
  .settings(
    name := "scala3-aws-lambda-ecr-example",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "0.7.29" % Test,
    libraryDependencies ++= Seq(
      // This library provides entrypoint class.
      "com.amazonaws" % "aws-lambda-java-runtime-interface-client" % "2.3.2",
      // This library contains common glossary to build lambda function.
      "com.amazonaws" % "aws-lambda-java-core" % "1.2.2"
    )
  )
  .enablePlugins(JavaAppPackaging) // for DockerPlugin
  .enablePlugins(EcrPlugin) // to upload to ECR
  .enablePlugins(DockerPlugin) // to build image
  .settings(
    dockerBaseImage := "amazoncorretto:17.0.8",
    // DockerPlugin emits entrypoint script into /opt/docker/bin.
    // Supply "sh" to help AWS Lambda (omitting this causes permission error)
    dockerEntrypoint := Seq("sh", s"/opt/docker/bin/${name.value}"),
    // Entrypoint script interprets CMD as Java classname/method designator.
    // We have to specify handler method here.
    dockerCmd := Seq(
      "com.github.windymelt.scala3awslambdaecrexample.Handler::hello"
    ),
    // corretto doesn't have useradd command: specify existing `daemon` user to avoid adding user
    Docker / daemonUserUid := None,
    Docker / daemonUser := "daemon",
    Docker / packageName := "scala3-aws-lambda-ecr-example",
    // We have to specify mainClass to reflect main class information to entrypoint script.
    Compile / mainClass := Some(
      "com.amazonaws.services.lambda.runtime.api.client.AWSLambda"
    ),
    Ecr / region := Region.getRegion(Regions.AP_NORTHEAST_1),
    Ecr / repositoryName := "scala3-aws-lambda-ecr-example",
    Ecr / localDockerImage := (Docker / packageName).value + ":" + (Docker / version).value,
    Ecr / repositoryTags := Seq(
      sys.env.get("TAG")
    ).flatten,
    Ecr / push := ((Ecr / push) dependsOn (Docker / publishLocal, Ecr / login)).value
  )
