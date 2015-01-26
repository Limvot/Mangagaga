#! /bin/bash

#scalac -feature -cp ./lib/gson-2.3.jar:./lib/luaj-jse-3.0.jar:./class/ -d ./class ./src/*.scala
sbt compile
