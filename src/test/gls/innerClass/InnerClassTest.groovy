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

    void testExtendsObjectAndAccessAFinalVariableInScope() {
        assertScript """
            final String objName = "My name is Guillaume"

            assert new Object() {
                String toString() { objName }
            }.toString() == objName
        """
    }

    void testExtendsObjectAndReferenceAMethodParameterWithinAGString_FAILS() {
        if (notYetImplemented()) return
        
        assertScript """
            Object makeObj0(String name) {
                 new Object() {
                    String toString() { "My name is ${name}" }
                 }
            }

            assert makeObj0("Guillaume").toString() == objName
        """
    }

    void testExtendsObjectAndReferenceAGStringPropertyDependingOnAMethodParameter_FAILS() {
        if (notYetImplemented()) return

        assertScript """
            Object makeObj1(String name) {
                 new Object() {
                    String objName = "My name is ${name}"

                    String toString() { objName }
                 }
            }

            assert makeObj1("Guillaume").toString() == objName
        """
    }

    void testUsageOfInitializerBlockWithinAnAIC_FAILS() {
        if (notYetImplemented()) return
        
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

            assert makeObj2("Guillaume").toString() == objName
        """
    }

    void testStaticInnerClass() {
        assertScript """
            class A {
                static class B{}
            }
            def x = new A.B()
            assert x != null
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
                def run() {}
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
                def run() {}
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
                def run(){}
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
                def run(){}
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
                def run() {}
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
                def run(){}
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
                def run() {}
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

} 
