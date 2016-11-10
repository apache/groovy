This is the home of the new antlr4 parser for Groovy.

In the gradle build the property useAntlr4 has to be set to enable the build of the parser and the execution of all tests with it.

Command line example:
./gradlew -PuseAntlr4=true bootstrapJar

To enable the new parser automatically at runtime the system property groovy.antlr4 has to be set.

Command line example:
export JAVA_OPTS="-Dgroovy.antlr4=true"
groovy foo.groovy

This system property also controls groovyc and has to be used in case it is used outside of this build, for example with:

groovyOptions.forkOptions.jvmArgs += ["-Dgroovy.antlr4=true"]
