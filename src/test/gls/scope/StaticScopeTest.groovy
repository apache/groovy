package gls.scope

public class StaticScopeTest extends CompilableTestSupport {

    public void testNormalStaticScopeInScript() {
        shouldNotCompile """
       static foo() {
         foo = 1
       }
     """

        shouldCompile """
       static foo() {
         def foo=1
       }
     """
    }

    public void testStaticImportInclass() {
        assertScript """
        import static Math.*
        class B {
            static main(args) { assert cos(2 * PI) == 1.0 }
        }
        """
    }

    public void testStaticImportProperty() {
        assertScript """
        import static A.*
        class B {
            static main(args) { assert temperature == 42 }
        }
        class A { static temperature = 42 }
        """
    }

    public void testNormalStaticScopeInclass() {
        assertScript """
       class A {
         static i
         static foo() {
           i=1
         }
       }
       A.foo()
       assert A.i == 1  
     """
        shouldNotCompile """
       class A {
         def i
         static foo() {
           i=1
         }
       }
     """

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

    public void testSpecialConstructorAccess() {
        shouldNotCompile """
       class A{ A(x){} }
       class B extends A {
         B(x) { super(nonExistingParameter) }
       }
     """
     
     shouldCompile """
       class A{ A(x){} }
       class B extends A {
         B(x) { super(x) }
       }
     """

     shouldNotCompile """
       class A{ A(x){} }
       class B extends A {
         def doNotAccessDynamicFieldsOrProperties
         B(x) { super(doNotAccessDynamicFieldsOrProperties) }
       }
     """

        shouldCompile """
       class A{ A(x){} }
       class B extends A {
         static allowUsageOfStaticPropertiesAndFields
         B(x) { super(allowUsageOfStaticPropertiesAndFields) }
       }
     """
    }
}
