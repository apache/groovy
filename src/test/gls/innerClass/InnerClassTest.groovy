package gls.innerClass

import gls.CompilableTestSupport

class InnerClassTest extends CompilableTestSupport {

    void testTimerAIC() {
        assertScript """
            boolean called = false

            Timer timer = new Timer()
            timer.schedule(new TimerTask() {
                void run() {
                    called = true
                }
            }, 0)
            sleep 100

            assert called
        """
    }

    void testAICReferenceInClosure() {
        assertScript """
            def y = [true]
            def o = new Object() {
              def foo() {
                def c = {
                  assert y[0]
                }
                c()
              }
            }
            o.foo()
        """
    }

    void testExtendsObjectAndAccessAFinalVariableInScope() {
        assertScript """
            final String objName = "My name is Guillaume"

            assert new Object() {
                String toString() { objName }
            }.toString() == objName
        """
    }

    void testExtendsObjectAndReferenceAMethodParameterWithinAGString() {
        assertScript """
            Object makeObj0(String name) {
                 new Object() {
                    String toString() { "My name is \${name}" }
                 }
            }

            assert makeObj0("Guillaume").toString() == "My name is Guillaume"
        """
    }

    void testExtendsObjectAndReferenceAGStringPropertyDependingOnAMethodParameter() {
        assertScript """
            Object makeObj1(String name) {
                 new Object() {
                    String objName = "My name is \${name}"

                    String toString() { objName }
                 }
            }

            assert makeObj1("Guillaume").toString() == "My name is Guillaume"
        """
    }

    void testUsageOfInitializerBlockWithinAnAIC () {
        assertScript """
            Object makeObj2(String name) {
                 new Object() {
                    String objName
                    // initializer block
                    {
                        objName = "My name is " + name
                    }

                    String toString() {
                        objName
                    }
                 }
            }

            assert makeObj2("Guillaume").toString() == "My name is Guillaume"
        """
    }

    void testStaticInnerClass() {
        assertScript """
            import java.lang.reflect.Modifier
        
            class A {
                static class B{}
            }
            def x = new A.B()
            assert x != null
            
            def mods = A.B.modifiers
            assert Modifier.isPublic(mods)
        """
        
        assertScript """
            class A {
                static class B{}
            }
            assert A.declaredClasses.length==1
            assert A.declaredClasses[0]==A.B
        """
    }

    void testNonStaticInnerClass_FAILS() {
        if (notYetImplemented()) return

        shouldNotCompile """
            class A {
                class B {}
            }
            def x = new A.B()
        """
    }

    void testAnonymousInnerClass() {
        assertScript """
            class Foo {}

            def x = new Foo(){
                def bar() { 1 }
            }
            assert x.bar() == 1
        """
    }

    void testLocalVariable() {
        assertScript """
            class Foo {}
            final val = 2
            def x = new Foo() {
              def bar() { val }
            }
            assert x.bar() == val
            assert x.bar() == 2
        """
    }

    void testConstructor() {
        shouldNotCompile """
            class Foo {}
            def x = new Foo() {
                Foo() {}
            }
        """
    }

    void testUsageOfOuterField() {
        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x = 1

                def foo() {
                    def runner = new Run() {
                        def run() { return x }
                    }
                    runner.run()
                }

                void x(y) { x = y }
            }
            def foo = new Foo()
            assert foo.foo() == 1
            foo.x(2)
            assert foo.foo() == 2
        """

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private static x = 1

                static foo() {
                    def runner = new Run() {
                        def run() { return x }
                    }
                    runner.run()
                }

                static x(y) { x = y }
            }
            assert Foo.foo() == 1
            Foo.x(2)
            assert Foo.foo() == 2
        """
    }

    void testUsageOfOuterFieldOverriden_FAILS() {
        if (notYetImplemented()) return

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x = 1
                def foo() {
                    def runner = new Run(){
                        def run() { return x }
                    }
                    runner.run()
                }
                void setX(y) { x=y }
            }
            class Bar extends Foo {
                def x = "string"
            }
            def bar = new Bar()
            assert bar.foo() == 1
            bar.x(2)
            assert bar.foo() == 2
            bar.x = "new string"
            assert bar.foo() == 2
        """

    //TODO: static part

    }

    void testUsageOfOuterMethod() {
        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x(){1}
                def foo() {
                    def runner = new Run(){
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        """

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private static x() {1}

                def foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            def foo = new Foo()
            assert foo.foo() == 1
        """
    }

    void testUsageOfOuterMethodOverriden() {
        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private x(){1}
                def foo() {
                    def runner = new Run(){
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            class Bar extends Foo{
                def x() { 2 }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        """

        assertScript """
            interface Run {
                def run()
            }
            class Foo {
                private static x() { 1 }

                static foo() {
                    def runner = new Run() {
                        def run() { return x() }
                    }
                    runner.run()
                }
            }
            class Bar extends Foo {
                static x() { 2 }
            }
            def bar = new Bar()
            assert bar.foo() == 1
        """
    }
    
    void testClassOutputOrdering() {
        // this does actually not do much, but before this
        // change the inner class was tried to be executed
        // because a class ordering bug. The main method 
        // makes the Foo class executeable, but Foo$Bar is 
        // not. So if Foo$Bar is returned, asserScript will
        // fail. If Foo is returned, asserScript will not
        // fail.
        assertScript """
            class Foo {
                static class Bar{}
                static main(args){}
            }
        """
    }
    
    void testInnerClassDotThisUsage() {
        assertScript """
            class A{
                int x = 0;
                class B{
                    int y = 2;
                    class C {
                        void foo() {
                          A.this.x  = 1
                          A.B.this.y = 2*B.this.y;
                        }
                    }
                }
            }
            def a = new A()
            def b = new A.B(a)
            def c = new A.B.C(b)
            c.foo()
            assert a.x == 1
            assert b.y == 4
        """
    }
    
    void testImplicitThisPassingWithNamedArguments() {
        def oc = new MyOuterClass4028()
        assert oc.foo().propMap.size() == 2
    }

    void testThis0 () {
        assertScript """
class A {
   static def field = 10
   void main (a) {
     new C ().r ()
   }

   class C {
      def r () {
        4.times {
          new B(it).u (it)
        }
      }
   }

   class B {
     def s
     B (s) { this.s = s}
     def u (i) { println i + s + field }
   }}"""
    }
    
    void testReferencedVariableInAIC() {
        assertScript """
            interface X{}
            
            final double delta = 0.1
            (0 ..< 1).collect { n ->
                new X () {
                    Double foo () {
                        delta
                    }
                }
            }
        """
        assertScript """
            interface X{}
            
            final double delta1 = 0.1
            final double delta2 = 0.1
            (0 ..< 1).collect { n ->
                new X () {
                    Double foo () {
                        delta1 + delta2
                    }
                }
            }
        """
    }

    // GROOVY-5679
    // GROOVY-5681
    void testEnclosingMethodIsSet() {
        new GroovyShell().evaluate '''import groovy.transform.ASTTest
        import static org.codehaus.groovy.control.CompilePhase.*
        import org.codehaus.groovy.ast.InnerClassNode
        import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.classgen.Verifier

        class A {
            int x

            /*@ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def cce = lookup('inner')[0].expression
                def icn = cce.type
                assert icn instanceof InnerClassNode
                assert icn.enclosingMethod == node
            })
            A() { inner: new Runnable() { void run() {} } }

            @ASTTest(phase=SEMANTIC_ANALYSIS, value={
                def cce = lookup('inner')[0].expression
                def icn = cce.type
                assert icn instanceof InnerClassNode
                assert icn.enclosingMethod == node
            })
            void foo() { inner: new Runnable() { void run() {} } }*/

            @ASTTest(phase=CLASS_GENERATION, value={
                def initialExpression = node.parameters[0].getNodeMetaData(Verifier.INITIAL_EXPRESSION)
                assert initialExpression instanceof ConstructorCallExpression
                def icn = initialExpression.type
                assert icn instanceof InnerClassNode
                assert icn.enclosingMethod != null
                assert icn.enclosingMethod.name == 'bar'
                assert icn.enclosingMethod.parameters.length == 0 // ensure the enclosing method is bar(), not bar(Object)
            })
            void bar(action=new Runnable() { void run() { x = 123 }}) {
                action.run()
            }

        }
        def a = new A()
        a.bar()
        assert a.x == 123
        '''
    }
} 

class MyOuterClass4028 {
    def foo() {
        new MyInnerClass4028(fName: 'Roshan', lName: 'Dawrani')
    }
    class MyInnerClass4028 {
        Map propMap
        def MyInnerClass4028(Map propMap) {
            this.propMap = propMap
        }
    }
}
