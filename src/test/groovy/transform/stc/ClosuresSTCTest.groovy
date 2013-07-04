/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

/**
 * Unit tests for static type checking : closures.
 *
 * @author Cedric Champeau
 */
class ClosuresSTCTest extends StaticTypeCheckingTestCase {

    void testClosureWithoutArguments() {
        assertScript '''
        def clos = { println "hello!" }

        println "Executing the Closure:"
        clos() //prints "hello!"
        '''
    }

    void testClosureWithArguments() {
        assertScript '''
            def printSum = { int a, int b -> print a+b }
            printSum( 5, 7 ) //prints "12"
        '''

        shouldFailWithMessages '''
            def printSum = { int a, int b -> print a+b }
            printSum( '5', '7' ) //prints "12"
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, java.lang.String]'
    }

    void testClosureWithArgumentsAndNoDef() {
        assertScript '''
            { int a, int b -> print a+b }(5,7)
        '''
    }

    void testClosureWithArgumentsNoDefAndWrongType() {
        shouldFailWithMessages '''
            { int a, int b -> print a+b }('5',7)
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, int]'
    }

    void testClosureReturnTypeInferrence() {
        assertScript '''
            def closure = { int x, int y -> return x+y }
            int total = closure(2,3)
        '''

        shouldFailWithMessages '''
            def closure = { int x, int y -> return x+y }
            int total = closure('2',3)
        ''', 'Closure argument types: [int, int] do not match with parameter types: [java.lang.String, int]'
    }

    void testClosureReturnTypeInferrenceWithoutDef() {
        assertScript '''
            int total = { int x, int y -> return x+y }(2,3)
        '''
    }

    void testClosureReturnTypeInference() {
        shouldFailWithMessages '''
            def cl = { int x ->
                if (x==0) {
                    1L
                } else {
                    x // int
                }
            }
            byte res = cl(0) // should throw an error because return type inference should be a long
        ''', 'Possible loose of precision from long to byte'
    }

    void testClosureWithoutParam() {
        assertScript '''
            { -> println 'Hello' }()
        '''
    }

    // GROOVY-5145
    void testCollect() {
        assertScript '''
            List<String> strings = [1,2,3].collect { it.toString() }
        '''
    }

    // GROOVY-5145
    void testCollectWithSubclass() {
        assertScript '''
            class StringClosure extends Closure<String> {
                StringClosure() { super(null,null) }
                void doCall(int x) { x }
            }
            List<String> strings = [1,2,3].collect(new StringClosure())
        '''
    }

    void testClosureShouldNotChangeInferredType() {
        assertScript '''
            def x = '123';
            { -> x = new StringBuffer() }
            x.charAt(0)
        '''
    }

    void testClosureSharedVariableWithIncompatibleType() {
        shouldFailWithMessages '''
            def x = '123';
            { -> x = 1 }
            x.charAt(0)
        ''', 'A closure shared variable [x] has been assigned with various types and the method [charAt(int)] does not exist in the lowest upper bound'
    }

    void testClosureCallAsAMethod() {
        assertScript '''
            Closure cl = { 'foo' }
            assert cl() == 'foo'
        '''
    }

    void testClosureCallWithOneArgAsAMethod() {
        assertScript '''
            Closure cl = { int x -> "foo$x" }
            assert cl(1) == 'foo1'
        '''
    }

    void testRecurseClosureCallAsAMethod() {
        assertScript '''
            Closure<Integer> cl
            cl = { int x-> x==0?x:1+cl(x-1) }
        '''
    }

    void testFibClosureCallAsAMethod() {
        assertScript '''
            Closure<Integer> fib
            fib = { int x-> x<1?x:fib(x-1)+fib(x-2) }
            fib(2)
        '''
    }

    void testFibClosureCallAsAMethodFromWithinClass() {
        assertScript '''
            class FibUtil {
                private Closure<Integer> fibo
                FibUtil() {
                    fibo = { int x-> x<1?x:fibo(x-1)+fibo(x-2) }
                }

                int fib(int n) { fibo(n) }
            }
            FibUtil fib = new FibUtil()
            fib.fib(2)
        '''
    }
    
    void testClosureRecursionWithoutClosureTypeArgument() {
        shouldFailWithMessages '''
            Closure fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
        ''', 'Cannot find matching method java.lang.Object#plus(java.lang.Object)'
    }

    void testClosureRecursionWithDef() {
        shouldFailWithMessages '''
            def fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
        ''',
                'Cannot find matching method java.lang.Object#plus(java.lang.Object)',
                'Cannot find matching method java.lang.Object#call(int)',
                'Cannot find matching method java.lang.Object#call(int)'
    }

    void testClosureRecursionWithClosureTypeArgument() {
        assertScript '''
            Closure<Integer> fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
        '''
    }

    void testClosureMemoizeWithClosureTypeArgument() {
        assertScript '''
            Closure<Integer> fib
            fib = { int n -> n<2?n:fib(n-1)+fib(n-2) }
            def memoized = fib.memoizeAtMost(2)
            assert fib(5) == memoized(5)
        '''
    }

    // GROOVY-5639
    void testShouldNotThrowClosureSharedVariableError() {
        assertScript '''
        Closure<Void> c = {
            List<String> list = new ArrayList<String>()
            String s = "foo"
            10.times {
                list.add(s)
            }
        }
        '''
    }
    // a case in Grails
    void testShouldNotThrowClosureSharedVariableError2() {
        assertScript '''
            class AntPathMatcher {
                boolean match(String x, String y) { true }
            }
            private String relativePath() { '' }
            def foo() {
                AntPathMatcher pathMatcher = new AntPathMatcher()
                def relPath = relativePath()
                def cl = { String it ->
                    pathMatcher.match(it, relPath)
                }
                cl('foo')
            }

            foo()
        '''
    }

    // GROOVY-5693
    void testClosureArgumentCheckWithFlowTyping() {
        assertScript '''
            Closure a = {
                int i ->
                println "First closure "+ i
            }
            Closure b = {
                String s ->
                println "Second closure "+ s
            }
            a(5)
            Closure c = a
            a=b
            a("Testing!")
            a = c
            a(5)
            a=a
            a(5)
            a = b
            a('Testing!')
        '''
    }

    // GROOVY-5705
    void testNPEWhenCallingClosureFromAField() {
        assertScript '''
            import groovy.transform.*

            class Test {
                Closure c = { it }

                @TypeChecked
                void test() {
                    c("123")
                }
            }

            new Test().test()
        '''
    }

    // GROOVY-6219
    void testShouldFailBecauseClosureReturnTypeDoesnMatchMethodSignature() {
        shouldFailWithMessages '''
            void printMessage(Closure<String> messageProvider) {
                println "Received message : ${messageProvider()}"
            }

            void testMessage() {
                printMessage { int x, int y -> x+y }
            }
        ''', 'Cannot find matching method'
    }

    // GROOVY-6238
    void testDirectMethodCallOnClosureExpression() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def dit = node.getNodeMetaData(INFERRED_TYPE)
                def irt = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert irt == CLOSURE_TYPE
                assert dit == CLOSURE_TYPE
            })
            def cl = { it }.curry(42)
            def val = cl.call()
            assert val == 42
        '''
    }

}

