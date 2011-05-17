$Id$

Building
========

To build you will need:

 * JDK 1.5+ (http://java.sun.com/j2se/)
 * Apache Ant 1.7+ (http://ant.apache.org)

For detailed instructions please see:

    http://groovy.codehaus.org/Building+Groovy+from+Source

To build everything, run tests and create a complete installation:

    ant install

To build without running tests:

    ant install -DskipTests=true

To build from Eclipse:

    1. ant install
    2. ensure that the M2_REPO classpath variable exists and points to the correct place (typically ~/.m2/repository)
       To Change this, go to Preferences -> Java -> Build Path -> Classpath variables
    3. Project -> Clean... and then build.  Should compile with no errors.  But if there are errors, then send a message to the groovy users mailing list
    4. You will *not* be able to run the tests from inside of Eclipse. 