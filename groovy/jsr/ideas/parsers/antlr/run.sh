#/bin/sh

export CLASSPATH=lib/antlr-2.7.4.jar:dist/groovyc-0.0.2.jar
java org.codehaus.groovy.antlr.Main -showtree ../../../tck/test/misc/HelloWorld.groovy
