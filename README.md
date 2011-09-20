Groovy
===

[Groovy][Groovy] is an agile and dynamic language for the Java Virtual Machine. It builds upon the strengths of Java, but has additional power features inspired by languages like Python, Ruby and Smalltalk. Groovy makes modern programming features available to Java developers with almost-zero learning curve as well as supports Domain-Specific Languages and other compact syntax so your code becomes easy to read and maintain. Groovy makes writing shell and build scripts easy with its powerful processing primitives, OO abilities and an Ant DSL. It also increases developer productivity by reducing scaffolding code when developing web, GUI, database or console applications. Groovy simplifies testing by supporting unit testing and mocking out-of-the-box. Groovy also seamlessly integrates with all existing Java classes and libraries and compiles straight to Java bytecode so you can use it anywhere you can use Java.
[Groovy]: http://groovy.codehaus.org/

Building
---

To build you will need:

* [JDK 1.5+](http://www.oracle.com/technetwork/java/javase/downloads)

Plus one of the following (Gradle will download itself if needed):
* [Apache Ant 1.7+](http://ant.apache.org)
* [Gradle 0.9.2+](http://gradle.org/)

For detailed instructions please see:

* [http://groovy.codehaus.org/Building+Groovy+from+Source](http://groovy.codehaus.org/Building+Groovy+from+Source)

To build everything using ant (including running tests and creating a complete installation):

    ant install

To build without running tests or creating OSGi information:

    ant install -DskipTests=true -DskipOsgi=true

To run tests from gradle (will download gradle the first time):

    gradlew test

To build from Eclipse:

* ant install
* ensure that the M2_REPO classpath variable exists and points to the correct place (typically ~/.m2/repository)
    * To Change this, go to Preferences -> Java -> Build Path -> Classpath variables
* Project -> Clean... and then build.  Should compile with no errors.  But if there are errors, then send a message to the groovy users mailing list
* You will *not* be able to run the tests from inside of Eclipse. 

License
---

Groovy is licensed under the terms of the [Apache License, Version 2.0][Apache License, Version 2.0].
[Apache License, Version 2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
