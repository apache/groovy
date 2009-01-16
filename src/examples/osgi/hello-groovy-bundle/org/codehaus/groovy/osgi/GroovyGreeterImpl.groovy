package org.codehaus.groovy.osgi

/**
* A simple POGO that prints a greeting to standard out. 
* 
* @author Hamlet D'Arcy
*/ 
public class GroovyGreeterImpl implements GroovyGreeter {
  
    public void sayHello() {
        println "Hello from the Groovy Greeter!"
    }
}
