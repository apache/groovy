AstBuilder and AST Transformation Example

This example shows how to use the AstBuilder to add a public static void main(String[]) 
method to a class. 

The example requires ant in your path and the Groovy 1.7 (or greater) 
Jar in your classpath. 

To build the example run "ant" from the current directory. The default 
target will compile the classes needed. The last step of the build 
script prints out the command needed to run the example. 

To run the example perform either of the following from the command lines: 
  groovy MainExample.groovy
  groovyc MainExample.groovy (and then invoke with java or view with javap)
  
The example should print: 
  Hello from the greet() method!

No exceptions should occur. 

The MainIntegrationTest.groovy file shows how to invoke an ASTTransformation
from a unit test. An IDE should be able to debug this. 
