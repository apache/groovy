package groovy

class MultilineChainExpressionTest extends GroovyTestCase {
   void testMultiLineChain() {
       // the code below should be compileable
       assert (
	       System
	           .out
	           .class 
 	       == 
	       PrintStream
	           .class
       )
       assert System
              .err
              .class == PrintStream.class
   }
}