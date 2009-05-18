package gls.scope;

import gls.CompilableTestSupport

public class ClassVariableHidingTest extends CompilableTestSupport {

   def foo=1;
   def bar=2;
   
   public void testFooHiding() {
     assert foo==1
     def foo = 5
     assert foo == 5
   }
   
   public void testBarHiding() {
     assert bar==2
     def bar = 5
     assert bar == 5
   }
 }