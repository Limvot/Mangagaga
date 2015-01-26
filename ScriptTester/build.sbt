name := "ScriptTester"

version := "0.1"

scalaVersion := "2.11.5"

val root = Project("root", file("."))
target in Compile := file("./target")
