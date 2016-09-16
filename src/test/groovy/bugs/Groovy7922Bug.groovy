package groovy.bugs;

import gls.CompilableTestSupport;

public class Groovy7922Bug extends CompilableTestSupport {

    void testMethodSelection() {
        def message = shouldNotCompile '''
            import groovy.transform.CompileStatic

            interface FooA {}
            interface FooB {}
            class FooAB implements FooA, FooB {}
            @CompileStatic
            class TestGroovy {
                static void test() { println new TestGroovy().foo(new FooAB()) }
                def foo(FooB x) { 43 }
                def foo(FooA x) { 42 }
            }

            TestGroovy.test()
        ''';

        assert message.contains("ambiguous")

        shouldCompile '''
            import groovy.transform.CompileStatic

            interface FooA {}
            interface FooB {}
            class FooAB implements FooA, FooB {}
            @CompileStatic
            class TestGroovy {
                static void test() { println new TestGroovy().foo((FooA)null) }
                def foo(FooB x) { 43 }
                def foo(FooA x) { 42 }
            }

            TestGroovy.test()
        '''
    }
}
