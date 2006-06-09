package gls.scope

import gls.scope.CompilableTestSupport

public class MultipleDefinitionOfSameVariableTest extends CompilableTestSupport {

   public void testInSameBlock() {
     shouldNotCompile("""
       def foo = 1
       def foo = 2
     """)
      
     shouldNotCompile("""
       class Foo {
         def foo() {
           def bar=1
           def bar=2
         }
       }
     """)
   }
   
   public void testInSubblocks() {
     shouldNotCompile("""
       def foo = 1
       5.times { def foo=2 }
     """)
     
     shouldNotCompile("""
       def foo = 1
       label1: { def foo=2 }
     """)
     
     shouldNotCompile("""
       def foo = 1
       for (i in []) { def foo=2 }
     """)
     
     shouldNotCompile("""
       def foo = 1
       while (true) { def foo=2 }
     """)     
   } 
   
   public void testInNestedClosure() {
     shouldNotCompile("""
       def foo = 1
       5.times { 6.times {def foo=2 }
     """)
     
     assertScript ("""
       def foo = 1
       5.times { 6.times {foo=2 } }
       assert foo == 2
     """)
   }
   
   public void testBindingHiding() {
     assertScript("""
       foo = 1
       def foo = 3
       assert foo==3
       assert this.foo == 1
       assert binding.foo == 1
     """)
   }
   
   public void testBindingAccessInMethod() {
	   assertScript("""
	     def methodUsingBinding() {
	       try {
	         s = "  bbb  ";
	       } finally {
	         s = s.trim();
	       }
	       assert s == "bbb"
	     } 
	     methodUsingBinding()
	     assert s == "bbb"
	   """)
   }
   
   public void testMultipleOfSameName() {
   		shouldNotCompile("""
   		  class DoubleField {
			def zero = 0
			public zero = 0
		  }
		  
   		""")
   
   		shouldNotCompile("""
   		  class DoubleField {
			def zero = 0
			def zero = 0
		  }
		  
   		""")

   }
   
   
}