#!/bin/sh

$GROOVY_HOME/bin/groovyc Simple.groovy
CLASSPATH=../../libgroovy/libgroovy.jar gcj --main=Simple -o Simple -L../../libgroovy -lgroovy *.class
rm -rf *.class

