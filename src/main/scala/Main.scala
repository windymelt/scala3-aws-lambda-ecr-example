package com.github.windymelt.scala3awslambdaecrexample

object Handler {
  @main def hello: Unit =
    println("Hello world!")
    println(msg)

  def msg = "I was compiled by Scala 3. :)"
}
