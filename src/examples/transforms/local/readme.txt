Local AST Transformation Example

This example shows how to wire together a local transformation. 

The example requires ant in your path and the Groovy 1.6 (or greater) 
Jar in your classpath. 

To build the example run "ant" from the current directory. The default 
target will compile the classes needed. The last step of the build 
script prints out the command needed to run the example. 

To run the example perform the following from the command line: 
  groovy LoggingExample.groovy
  
The example should print: 
  Hello World
  Starting greetWithLogging
  Hello World
  Ending greetWithLogging

No exceptions should occur. 