package gls.scope

import gls.scope.CompilableTestSupport

public class StaticScopeTest extends CompilableTestSupport {

   public void testNormalStaticScope() {
     shouldNotCompile("""
       static foo() {
         foo = 1
       }
     """)
      
     shouldCompile("""
       static foo() {
         def foo=1
       }
     """)
     
     assertScript("""
       class A {
         static i
         static foo() {
           i=1
         }
       }
       A.foo()
       assert A.i == 1  
     """)
   }
   
   public void testClosureInStaticScope() {
     shouldCompile("""
       5.times { foo=2 }
     """)
     
     shouldCompile("""
       5.times { foo=it }
     """)
     
   }
   
   public void testFullyQualifiedClassName() {
      assertScript """
        static foo() {java.lang.Integer}
        assert foo() == java.lang.Integer
      """
      shouldNotCompile """
        static foo() { java.lang.JavaOrGroovyThatsTheQuestion }
      """
      shouldCompile """
        foo() { java.lang.JavaOrGroovyThatsTheQuestion }
      """
   }
   
   public void testStaticPropertyInit() {
      // GROOVY-1910
      assertScript """
        class Foo {
           static p1 = 1
           static p2 = p1
        }
        assert Foo.p2 == Foo.p1
        assert Foo.p1 == 1
      """
      
      // should not compile for mistyped name
      shouldNotCompile """
        class Foo {
           static p1 = 1
           static p2 = x1
        }
        assert Foo.p2 == Foo.p1
        assert Foo.p1 == 1
        
      """
   }   
}