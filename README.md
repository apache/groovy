Groovy
===

[Groovy][Groovy] is an agile and dynamic language for the Java Virtual Machine. It builds upon the strengths of Java, but has additional power features inspired by languages like Python, Ruby and Smalltalk. Groovy makes modern programming features available to Java developers with almost-zero learning curve as well as supports Domain-Specific Languages and other compact syntax so your code becomes easy to read and maintain. Groovy makes writing shell and build scripts easy with its powerful processing primitives, OO abilities and an Ant DSL. It also increases developer productivity by reducing scaffolding code when developing web, GUI, database or console applications. Groovy simplifies testing by supporting unit testing and mocking out-of-the-box. Groovy also seamlessly integrates with all existing Java classes and libraries and compiles straight to Java bytecode so you can use it anywhere you can use Java.
[Groovy]: http://groovy.codehaus.org/

Building
---

_Starting from Groovy 2.0-0-rc-1, the Ant build is no longer supported_

To build you will need:

* [JDK 1.7+](http://www.oracle.com/technetwork/java/javase/downloads)


For detailed instructions please see:

* [http://groovy.codehaus.org/Building+Groovy+from+Source](http://groovy.codehaus.org/Building+Groovy+from+Source)

To build everything using Gradle (the command below will download Gradle automatically, you do not need to download it first).

    ./gradlew clean dist

This will generate a distribution similar to the zip you can download on the Groovy download page.

To build everything and launch unit tests, use

    ./gradlew test

To build from IntelliJ IDEA

    ./gradlew idea
    ./gradlew jarAll

Then open the generated project in IDEA.

To build from Eclipse:

TBD


InvokeDynamic support
---

The Groovy build supports the new Java 7 JVM instruction ```invokedynamic```. If you want to build Groovy with invokedynamic, you can use the project property ```indy```:

    ./gradlew -Pindy=true clean test

Please note that the following Gradle tasks generate both indy and non indy variants of the jars, so you don't need to use the system property:

* dist
* install
* uploadArchives

License
---

Groovy is licensed under the terms of the [Apache License, Version 2.0][Apache License, Version 2.0].
[Apache License, Version 2.0]: http://www.apache.org/licenses/LICENSE-2.0.html
