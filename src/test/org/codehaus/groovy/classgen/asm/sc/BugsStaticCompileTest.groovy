/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.BugsSTCTest

/**
 * Unit tests for static type checking : bugs.
 */
class BugsStaticCompileTest extends BugsSTCTest implements StaticCompilationTestSupport {

    void testGroovy5498PropertyAccess() {
        assertScript '''
            class Test {

              List getListVar() {
                new ArrayList()
              }

              void someMethod() {
                 def t = new Object()
                 t = this

                 t.getListVar()     //No error here
                 t.listVar          //error is being reported here
                 assert t.listVar == t.getListVar()
              }
            }
            new Test().someMethod()
        '''
    }

    // GROOVY-5512
    void testCreateRangeInInnerClass() {
        def shell = new GroovyShell()
        shell.evaluate '''
            class Outer {
                static class Inner {
                    @groovy.transform.CompileStatic
                    int m() {
                        int x = 0
                        for (int i in 1..10) {x++}
                        x
                    }
                }
            }
            assert new Outer.Inner().m() == 10
        '''
    }

    // GROOVY-5526
    void testAssertEqualsShouldNotThrowVerifyError() {
        assertScript '''
            import static org.junit.Assert.*;
            import groovy.transform.CompileStatic;

            class CompilerBugs {

              public static void main(String[] args) {
                int expected = 0
                assertEquals(expected, args.length)
              }

            }
        '''
    }

    // GROOVY-5529
    void testStaticCompilationOfClosureWhenSingleMethodAnnotated() {
        new GroovyShell().evaluate '''import groovy.transform.ASTTest
        import static org.codehaus.groovy.control.CompilePhase.*

        interface Row {
            int getKey()
        }

        class RowImpl implements Row {
            int getKey() { 1 }
        }

        @groovy.transform.CompileStatic
        def test() {
            def rows = [new RowImpl(), new RowImpl(), new RowImpl()]

            rows.each { Row row ->
                println row.key
            }
        }

        test()
        '''
    }

    // GROOVY-5536
    void testShouldNotThrowVerifyErrorWithNullDereferenceInIf() {
        assertScript '''
                boolean getDescriptorForPlugin(File pluginDir) {
                    if (pluginDir?.exists()) { true } else { false }
                }
                assert getDescriptorForPlugin(null) == false
        '''
    }

    // GROOVY-
    void testPowerShouldNotThrowVerifyError() {
        assertScript '''int squarePlusOne(int num) {
                num ** num + 1
            }
            assert squarePlusOne(2) == 5
        '''
    }

    // GROOVY-5570
    void testShouldNotThrowVerifyErrorRegisterContainsWrongType() {
        assertScript '''
                void foo() {
                boolean idx = false
                def cl = { idx }
                }
            '''
        assertScript '''
                void foo() {
                int idx = 0
                def cl = { idx }
                }
            '''
    }

    // GROOVY-5572
    void testTernaryOperatorWithNull() {
        assertScript '''
            assert (true ? null : true) == null
        '''
    }

    // GROOVY-5564
    void testSkipStaticCompile() {
        new GroovyShell().evaluate '''import groovy.transform.CompileStatic
            import static groovy.transform.TypeCheckingMode.SKIP

            @CompileStatic
            class A {
                @CompileStatic(SKIP)
                String toString(Object o) { o }
            }

            def a = new A()
            assert a.toString('foo')=='foo'
            assert a.toString(1) == '1'
            '''
    }

    // GROOVY-5586
    void testCanonicalInInnerClass() {
        new GroovyShell().evaluate '''import groovy.transform.*
            @CompileStatic
            class CanonicalStaticTest extends GroovyTestCase {
              @Canonical class Thing {
                String stuff
              }

              Thing testCanonical() {
                new Thing()
              }
            }
            assert new CanonicalStaticTest().testCanonical().toString() == 'CanonicalStaticTest$Thing(null)'
        '''
    }

    // GROOVY-5607
    // duplicate of GROOVY-5573, see ArraysAndCollectionsSTCTest#testArrayNewInstance()
    void testStaticNewInstanceMethodClash() {
        assertScript '''
            class Sql {
                static Sql newInstance(String s1, String s2, String s3, String s4) {
                    new Sql()
                }
            }

            @groovy.transform.CompileStatic
            class Main {
                void test() {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value= {
                        assert node.rightExpression.getNodeMetaData(INFERRED_TYPE).nameWithoutPackage == 'Sql'
                    })
                    def sql = Sql.newInstance("a", "b", "c", "d")
                }
            }

            new Main().test()
        '''
    }

    void testCompileStaticTwiceShouldNotBeAProblem() {
        new GroovyShell().evaluate '''import groovy.transform.CompileStatic
        @CompileStatic
        class Tool {
            @CompileStatic // annotated too, even if class is already annotated
            String relativePath(File relbase, File file) {
                def pathParts = []
                def currentFile = file
                while (currentFile != null && currentFile != relbase) {
                    pathParts += currentFile.name
                    currentFile = currentFile.parentFile
                }
                pathParts.reverse().join('/')
            }
        }
        File a = new File('foo')
        File b = new File(new File(a, 'bar'), 'baz')
        assert new Tool().relativePath(a,b) == 'bar/baz'
        '''
    }

    void testCompileStaticTwiceShouldNotBeAProblemUsingCustomizer() {
        assertScript '''import groovy.transform.CompileStatic
        @CompileStatic
        class Tool {
            @CompileStatic // annotated too, even if class is already annotated
            String relativePath(File relbase, File file) {
                def pathParts = []
                def currentFile = file
                while (currentFile != null && currentFile != relbase) {
                    pathParts += currentFile.name
                    currentFile = currentFile.parentFile
                }
                pathParts.reverse().join('/')
            }
        }
        File a = new File('foo')
        File b = new File(new File(a, 'bar'), 'baz')
        assert new Tool().relativePath(a,b) == 'bar/baz'
        '''
    }

    // GROOVY-5613
    void testNullSafeAssignment() {
        assertScript '''
        class A {
            int x = -1
        }
        A a = new A()
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
        })
        def x = a?.x
        '''
    }

    void testNullSafeAssignmentWithLong() {
        assertScript '''
        class A {
            long x = -1
        }
        A a = new A()
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == Long_TYPE
        })
        def x = a?.x
        '''
    }

    void testNullSafeAssignmentWithChar() {
        assertScript '''
        class A {
            char x = 'a'
        }
        A a = new A()
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == Character_TYPE
        })
        def x = a?.x
        assert x == 'a'
        '''
    }

    void testCallStaticallyImportedMethodWithNullSafeArgument() {
        assertScript '''import static java.lang.Math.abs
        class A {
            int x = -1
        }
        def a = new A()
        def x = a?.x
        assert abs(a?.x) == 1
        '''
    }

    void testClosureAsInterfaceArgument() {
        assertScript '''
                Closure c = { Integer x, Integer y -> x <=> y }
                def list = [ 3,1,5,2,4 ]
                assert ((Collection)list).sort(c) == [1,2,3,4,5]
            '''
    }

    void testInferredTypeForInteger() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            Integer x = 1
        '''
    }

    // GROOVY-5671
    void testPostfixIncPrimitiveInteger() {
        assertScript '''
            int x = 0
            x++
            x++
            assert x == 2
            assert x++ == 2
            assert x == 3
        '''
    }

    void testPostfixIncInteger() {
        assertScript '''
                Integer x = 0
                x++
                x++
                assert x == 2
                assert x++ == 2
                assert x == 3
            '''
    }

    void testPostfixDecInt() {
        assertScript '''
                int x = 0
                x--
                x--
                assert x == -2
                assert x-- == -2
                assert x == -3
            '''
    }

    void testPostfixDecInteger() {
        assertScript '''
                Integer x = 0
                x--
                x--
                assert x == -2
                assert x-- == -2
                assert x == -3
            '''
    }

    void testPrefixIncPrimitiveInteger() {
        assertScript '''
            int x = 0
            ++x
            ++x
            assert x == 2
            assert ++x == 3
            assert x == 3
        '''
    }

    void testPrefixIncInteger() {
        assertScript '''
                Integer x = 0
                ++x
                ++x
                assert x == 2
                assert ++x == 3
                assert x == 3
            '''
    }

    void testPrefixDecInt() {
        assertScript '''
                int x = 0
                --x
                --x
                assert --x == -3
                assert x == -3
            '''
    }

    void testPrefixDecInteger() {
        assertScript '''
                Integer x = 0
                --x
                --x
                assert --x == -3
                assert x == -3
            '''
    }

    void testShouldSkipSpreadOperator() {
        new GroovyShell().evaluate '''import groovy.transform.TypeCheckingMode
            import groovy.transform.CompileStatic

            @CompileStatic // top level must be @CS
            class Foo {
                @CompileStatic(TypeCheckingMode.SKIP)
                static void foo(fun, args) {
                    new Runnable() { // create an anonymous class which should *not* be visited
                        void run() {
                            fun(*args) // spread operator is disallowed with STC/SC, but SKIP should prevent from an error
                        }
                    }
                }
            }
            new Foo()
        '''
    }

    // GROOVY-5672
    void testTypeCheckedPlusCompileStatic() {
        new GroovyShell().evaluate '''import groovy.transform.CompileStatic
        import groovy.transform.TypeChecked

        @TypeChecked
        @CompileStatic
        class SampleClass {
            def a = "some string"
            def b = a.toString()
        }
        new SampleClass()
        '''
    }

    void testSubclassShouldNotThrowArrayIndexOutOfBoundsException() {
        assertScript '''
            // The error only shows up if the subclass is compiled *before* the superclass
            class Subclass extends Z {
                public Subclass(double x, double y, double z) {
                    super(x,y,z)
                }
            }
            class Z {
               double x, y, z

                public Z(double x, double y, double z) { this.x = x; this.y = y; this.z = z }

                public Z negative() { return new Z(-x, -y, -z) }
            }
            new Subclass(0,0,0)
        '''
    }

    void testIncrementOperatorOnInt() {
        assertScript '''
                int incInt(int n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt(5) == 7'''
    }

    void testIncrementOperatorOnShort() {
        assertScript '''
                short incInt(short n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt((short)5) == 7'''
    }

    void testIncrementOperatorOnByte() {
        assertScript '''
                byte incInt(byte n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt((byte)5) == 7'''
    }

    void testIncrementOperatorOnLong() {
        assertScript '''
                long incInt(long n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt(5) == 7'''
    }

    void testIncrementOperatorOnFloat() {
        assertScript '''
                float incInt(float n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt(5) == 7'''
    }

    void testIncrementOperatorOnDouble() {
        assertScript '''
                double incInt(double n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt(5) == 7'''
    }

    void testIncrementOperatorOnChar() {
        assertScript '''
                char incInt(char n) {
                    def result = n
                    ++result
                    result++
                    return result
                }
                assert  incInt((char)'a') == (char)('c')'''
    }

    void testIncrementField() {
        assertScript '''
            class Foo {
                int x
            }
            def f = new Foo()
            f.x++
            assert f.x == 1
            assert f.x++ == 1
            assert f.x == 2
        '''
    }

    // GROOVY-5789
    void testLoopWithIncrement() {
        assertScript '''
        int execute() {
            // using a list, so that if the loop is endless, the test eventually fails with OOM
            List<Integer> list = new LinkedList<Integer>()
            for (def i = 0; i < 4; ++i) { println i; list << i }
            list.size()
        }
        assert execute() == 4
        '''
    }

    // GROOVY-5800
    void testInOperator() {
        assertScript '''
            boolean m( Integer i ) {
              i in [ 1, 2, 3 ]
            }
            assert m(1) == true
            assert m(4) == false
            '''
    }

    // GROOVY-5814
    void testCompileStaticImmutable() {
        assertScript '''
            import groovy.transform.*

            @Immutable
            class Test {
              int a
              String b
            }

            new Test( 1, 'tim' )
        '''
    }

    // GROOVY-5738
    void testAccessFieldFromGStringAfterInstanceOf() {
        new GroovyShell().evaluate '''
            class Greeting { String who }

            @groovy.transform.CompileStatic
            class GreetingActor {

              def receive = {
                if(it instanceof Greeting) {
                    println "Hello ${it.who}"
                }
              }

            }
            new GreetingActor().receive(new Greeting(who:'cedric'))
        '''
    }

    // GROOVY-5738
    void testAccessMethodFromGStringAfterInstanceOf() {
        new GroovyShell().evaluate '''
            class Greeting {
                String who
                String whoAmI() { who }
            }

            @groovy.transform.CompileStatic
            class GreetingActor {

              def receive = {
                if(it instanceof Greeting) {
                    println "Hello ${it.whoAmI()}"
                }
              }

            }
            new GreetingActor().receive(new Greeting(who:'cedric'))
        '''
    }

    // GROOVY-5804
    void testNegateSharedBooleanInClosure() {
        assertScript '''
                boolean x = false
                def cl = {
                    if (!x) {
                        assert true
                    } else {
                        assert false
                    }
                }
                cl()
            '''
    }

    void testCallClosureInInnerClass() {
        assertScript '''
            class A {
                static class B { // bug doesn't occur if not wrapped into an inner class
                    static void foo() {
                        def cl = { -> println 'ok' }
                        cl()
                    }
                }
            }
            A.B.foo()
        '''
    }

    // GROOVY-5890
    void testIsCaseWithClassLiteral() {
        assertScript '''
            assert 'a' in String
        '''
    }

    // GROOVY-5887
    void testSpreadCallWithArray() {
        assertScript '''
            def list = 'a,b,c'.split(/,/)*.trim()
            assert list == ['a','b','c']
        '''
    }

    // GROOVY-5919
    void testPrivateAccessorsWithSubClass() {
        assertScript '''
            class Top {
                private int foo = 666
                private class InnerTop {
                    int foo() { foo }
                }
            }
            class Bottom extends Top {
                private int bar = 666
                private class InnerBottom {
                    int bar() { bar } // name clash for fpaccess$0
                }
            }
            new Bottom()
        '''
    }

    void testSuperMethodCallInSkippedSection() {
        assertScript '''import groovy.transform.CompileStatic
import groovy.transform.TypeCheckingMode
            class Top {
                public int foo() { 123 }
            }
            class Bottom extends Top {
                @CompileStatic(TypeCheckingMode.SKIP)
                public int bar() {
                    foo()
                }
            }
            def obj = new Bottom()
            assert obj.bar() == 123
        '''
    }

    void testArrayGetOnObject() {
        shouldFailWithMessages '''
            def foo(Object o) {
                o[0]
            }
        ''', 'Cannot find matching method java.lang.Object#getAt(int)'
    }

    void testStaticCompileWithPattern() {
        shouldFailWithMessages '''
            def fieldMatcher = '[x=1] [a=b] [foo=bar]' =~ ~"\\\\[([^=\\\\[]+)=([^\\\\]]+)\\\\]"
            assert fieldMatcher instanceof java.util.regex.Matcher
            @ASTTest(phase=INSTRUCTION_SELECTION, value = {
                assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
            })
            def value = fieldMatcher[0]
            // should not pass
            def str = value[0]
        ''', 'Cannot find matching method java.lang.Object#getAt(int)'
    }

    void testChainedNullSafePropertyOnMap() {
        assertScript '''
        Map<String, Map<String,Map<String,Integer>>> m=[:]
        // this is ok
        assert m?.a == null
        assert m?.a?.b == null
        assert m?.a?.b?.c == null
        assert m?.a?.b?.c?.intValue() == null
        '''
    }

    void testNullSafePropertyOnList() {
        assertScript '''
        List<Class> classes = null
        // this is ok
        assert classes?.name == null
        '''
    }

    // GROOVY-6061
    void testShouldCallPutAt() {
        assertScript '''
            def swapUseStatic(List<Integer> a, int i, int j) {
                int temp = a[i]
                a[i] = a[j]  //BUG HERE, a[i] = ... doesn't call "putAt"
                a[j] = temp
            }
            def list = [1,2]
            swapUseStatic(list, 0, 1)
            assert list == [2,1]
        '''
    }

    // GROOVY-6101
    void testShouldNotGenerateInvalidClassWithNullSafeInvocationOnMethodReturningPrimitiveType() {
        assertScript '''
        class Piece {
            int x() { 333 }
        }

        void foo() {
            Piece[] pieces = [new Piece(), null] as Piece[]
            int sum = 0
            for (int i=0;i<pieces.length;i++) {
                if (pieces[i]?.x()) {
                    sum += pieces[i].x()
                }
            }
            assert sum == 333
        }
        foo()
        '''
    }

    // GROOVY-6101 fix side effect
    void testMakeSureOptimizationKeepsSideEffect() {
        assertScript '''
            class Foo {
                boolean hasSideEffect = false
                int x() { hasSideEffect=true; 333 }
            }
            Foo foo = new Foo()
            if (foo.x()==null) {
                println 'ok'
            }
            assert foo.hasSideEffect
         '''
    }

    void testShouldNotThrowNPEBecauseLHSIsNull() {
        assertScript '''
            class A {
                int foo() { 123 }
            }
            A a = null
            def bar = a?.foo() == 123 // will evaluate to false
            assert bar == false
        '''
    }

    // GROOVY-5921
    void testShouldHandleLongArray() {
        assertScript '''
            class Groovy5921 {
              long[] data = [1L]

              void doSomething() {
                data[ 0 ] |= 0x7ffffffl
              }
            }

            def b = new Groovy5921()
            b.doSomething()

        '''
    }

    void testShouldHandleLongArrayWithUnboxing() {
        assertScript '''
            class Groovy5921 {
              long[] data = [1L]

              void doSomething() {
                data[ 0 ] |= Long.valueOf(0x7ffffffl)
              }
            }

            def b = new Groovy5921()
            b.doSomething()

        '''
    }

    void testShouldHandleBoxedLongArray() {
        assertScript '''
            class Groovy5921 {
              Long[] data = [1L]

              void doSomething() {
                data[ 0 ] |= Long.valueOf(0x7ffffffl)
              }
            }

            def b = new Groovy5921()
            b.doSomething()

        '''
    }

    void testShouldHandleBoxedLongArrayWithBoxing() {
        assertScript '''
            class Groovy5921 {
              Long[] data = [1L]

              void doSomething() {
                data[ 0 ] |= 0x7ffffffl
              }
            }

            def b = new Groovy5921()
            b.doSomething()

        '''
    }

    // GROOVY-6113
    void testCallObjectVargsMethodWithPrimitiveIntConstant() {
        try {
            assertScript '''
                int sum(Object... elems) {
                     (Integer)elems.toList().sum()
                }
                int x = sum(Closure.DELEGATE_FIRST)
                assert x == Closure.DELEGATE_FIRST
            '''
        } finally {
//            println astTrees
        }
    }

    // GROOVY-6095
    void testServletError() {
        def shell = new GroovyShell()
        shell.evaluate '''
            @Grab('javax.servlet:javax.servlet-api:3.0.1')
            import groovy.transform.CompileStatic

            import javax.servlet.ServletContext
            import javax.servlet.ServletRegistration

            /**
             * author: Richard Vowles - http://gplus.to/RichardVowles
             */
            @CompileStatic
            class ServletExample {

              public void myMethod(ServletContext ctx) {
                ctx.getServletRegistrations().each { String name, ServletRegistration sr ->
                  println name
                }

              }
            }
            new ServletExample()
        '''
    }

    // GROOVY-6137
    void testIsCaseShouldBeNullSafe() {
        assertScript '''
        def isCaseNullCS(a, b) {
            a in b
        }
        assert isCaseNullCS(1, null) == false
        assert isCaseNullCS(null, 1) == false
        assert isCaseNullCS(1,1) == true
        assert isCaseNullCS(1,2) == false
        assert isCaseNullCS(2,1) == false
        '''
    }

    // GROOVY-6242
    void testGetAtBigInt() {
        assertScript '''
class Sequence {
    public Sequence() {}
    static BigInteger getAt(final int index) { 1G }
    static BigInteger getAt(final BigInteger index) { getAt(index as int) }
}

class Iterator implements java.util.Iterator {
    private BigInteger currentIndex = 0G
    private final Sequence sequence
    Iterator(final Sequence s) { sequence = s }
    boolean hasNext() { return true }
    @ASTTest(phase=INSTRUCTION_SELECTION,value={
        def expr = node.code.statements[0].expression
        def indexType = expr.rightExpression.getNodeMetaData(INFERRED_TYPE)
        assert indexType == make(BigInteger)
    })
    BigInteger next() { sequence[currentIndex++] }
    void remove() { throw new UnsupportedOperationException() }
}

def it = new Iterator(new Sequence())
assert it.next() == 1G
'''
    }

    // GROOVY-6243
    void testSwapUsingMultipleAssignment() {
        assertScript '''
        def swap(result,next) {
            print "($result,$next) -> "
            (result, next) =  [next, result]
            println "($result,$next)"
            [result, next]
        }

        assert swap(0,1) == [1,0]
        assert swap('a','b') == ['b','a']
        assert swap('a', 1) == [1, 'a']
        def o1 = new Object()
        def o2 = new Date()
        assert swap(o1,o2) == [o2, o1]
        '''
    }

    // GROOVY-5882
    void testMod() {
        assertScript """
            int foo(Map<Integer, Object> markers, int i) {
                int res = 0
                for (e in markers.entrySet()) {
                    res += i % e.key
                }
                return res
            }
            assert foo([(1):null,(2):null,(3):null],2)==2
        """

        assertScript """
            int foo(Map<Integer, Object> markers, int i) {
                int res = 0
                for (e in markers.entrySet()) {
                    int intKey = e.key
                    res += i % intKey
                }
                return res
            }
            assert foo([(1):null,(2):null,(3):null],2)==2
        """
        assertScript """
            int foo(Map<Integer, Object> markers, int i) {
                int res = 0
                for (e in markers.entrySet()) {
                    res += i % e.key.intValue()
                }
                return res
            }
            assert foo([(1):null,(2):null,(3):null],2)==2
        """
    }

    void testSuperCallShouldBeDirect() {
        try {
            assertScript '''
                class TwoException extends Exception {
                    @ASTTest(phase=INSTRUCTION_SELECTION,value={
                        def superCall = node.code.statements[0].expression
                        assert superCall.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)!=null
                    })
                    public TwoException(Throwable t) {
                        super(t)
                    }
                }
                def e = new TwoException(null) // will not throw an exception
            '''
        } finally {
            assert !astTrees.TwoException.contains('selectConstructorAndTransformArguments')
        }
    }

    void testNullSafeOperatorShouldNotCallMethodTwice() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicLong

            class Sequencer {
              private final AtomicLong sequenceNumber = new AtomicLong(0)

              public Long getNext() {
                return sequenceNumber.getAndIncrement()
              }
            }

            final seq = new Sequencer()
            (1..5).each {
              println seq.next?.longValue()
            }
            assert seq.next == 5
'''
    }

    void testNullSafeOperatorShouldNotCallMethodTwiceWithPrimitive() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicLong

            class Sequencer {
              private final AtomicLong sequenceNumber = new AtomicLong(0)

              public long getNext() {
                sequenceNumber.getAndIncrement()
              }
            }

            final seq = new Sequencer()
            (1..5).each {
              println seq.next?.longValue()
            }
            assert seq.next == 5
'''
    }

    void testNullSafeOperatorShouldNotCallMethodTwice1Arg() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicLong

            class Sequencer {
              private final AtomicLong sequenceNumber = new AtomicLong(0)

              public Long getNext(int factor) {
                factor*sequenceNumber.getAndIncrement()
              }
            }

            final seq = new Sequencer()
            (1..5).each {
              println seq.getNext(2)?.longValue()
            }
            assert seq.getNext(2) == 10
'''
    }

    void testNullSafeOperatorShouldNotCallMethodTwiceWithPrimitive1Arg() {
        assertScript '''
            import java.util.concurrent.atomic.AtomicLong

            class Sequencer {
              private final AtomicLong sequenceNumber = new AtomicLong(0)

              public long getNext(int factor) {
                factor*sequenceNumber.getAndIncrement()
              }
            }

            final seq = new Sequencer()
            (1..5).each {
              println seq.getNext(2)?.longValue()
            }
            assert seq.getNext(2) == 10
'''
    }

    void testShouldAllowSubscriptOperatorOnSet() {
        assertScript '''
            def map = new LinkedHashMap<>([a:1,b:2])
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(Set)
                assert ift.isUsingGenerics()
                assert ift.genericsTypes[0].type==STRING_TYPE
            })
            def set = map.keySet()
            def key = set[0]
            assert key=='a'
        '''
        assertScript '''
            def map = new LinkedHashMap([a:1,b:2])
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(Set)
                assert ift.isUsingGenerics()
                assert ift.genericsTypes[0].name=='K'
            })
            def set = map.keySet()
            def key = set[0]
            assert key=='a'
        '''
    }

    // GROOVY-6552
    void testShouldNotThrowClassCastException() {
        assertScript '''import java.util.concurrent.Callable

    String text(Class clazz) {
        new Callable<String>() {
            String call() throws Exception {
                new Callable<String>() {
                    String call() throws Exception {
                        clazz.getName()
                    }
                }.call()
            }
        }.call()
    }

    assert text(String) == 'java.lang.String'
    '''
    }

    // GROOVY-6851
    void testShouldNotThrowNPEIfElvisOperatorIsUsedInDefaultArgumentValue() {
        assertScript '''import org.codehaus.groovy.ast.expr.MethodCallExpression

class GrailsHomeWorkspaceReader {
    @ASTTest(phase=INSTRUCTION_SELECTION,value={
        def defaultValue = node.parameters[0].initialExpression
        assert defaultValue instanceof MethodCallExpression
        def target = defaultValue.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
        assert target != null
    })
    GrailsHomeWorkspaceReader(String grailsHome = System.getProperty('grails.home')) {
    }
}
new GrailsHomeWorkspaceReader()
'''
        assertScript '''
class GrailsHomeWorkspaceReader {
    GrailsHomeWorkspaceReader(String grailsHome = System.getProperty('grails.home') ?: System.getenv('GRAILS_HOME')) {
    }
}
new GrailsHomeWorkspaceReader()
'''
    }

    // GROOVY-6342
    void testShouldNotThrowNPEIfElvisOperatorIsUsedInsideTernary() {
        assertScript '''class Inner {
    int somestuff
}
Inner inner = null
int someInt = inner?.somestuff ?: 0
println someInt

'''
    }

    void testAccessOuterClassMethodFromInnerClassConstructor() {
        assertScript '''
    class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        String a

        private class Inner extends Parent {
           Inner() { super(getA()) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer(a:'ok')
    assert o.test() == 'ok'
    '''
    }

    void testAccessOuterClassMethodFromInnerClassConstructorUsingExplicitOuterThis() {
        assertScript '''
    class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        String a

        private class Inner extends Parent {
           Inner() { super(Outer.this.getA()) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer(a:'ok')
    assert o.test() == 'ok'
    '''
    }

    void testAccessOuterClassMethodFromInnerClassConstructorUsingExplicitOuterThisAndProperty() {
        assertScript '''
    class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        String a

        private class Inner extends Parent {
           Inner() { super(Outer.this.a) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer(a:'ok')
    assert o.test() == 'ok'
    '''
    }

    void testAccessOuterClassStaticMethodFromInnerClassConstructor() {
        assertScript '''
    class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        static String a

        private class Inner extends Parent {
           Inner() { super(getA()) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer()
    Outer.a = 'ok'
    assert o.test() == 'ok'
    '''
    }

    void testStaticMethodFromInnerClassConstructor() {
        assertScript '''
    class Parent {
        String str
        Parent(String s) { str = s }
    }
    class Outer {
        private class Inner extends Parent {
           static String a = 'ok'
           Inner() { super(getA()) }
        }

        String test() { new Inner().str }
    }
    def o = new Outer()
    assert o.test() == 'ok'
    '''
    }

    // GROOVY-6876
    void testSetterOfPrimitiveType() {
        assertScript '''
            class Foo {
                long bar
                def method() {
                    setBar(-1L)
                    bar
                }
            }
            assert new Foo().method() == -1L
            '''

        assertScript '''
            class Foo {
                long bar
                def method(Long v) {
                    setBar(v)
                    bar
                }
            }
            assert new Foo().method(-1L) == -1L
        '''

        assertScript '''
            class Foo {
                long rankOrderingOrId
                void setRankOrderingOrId(long rankOrderingOrId) {
                    this.rankOrderingOrId = rankOrderingOrId < 0 ? -1 : rankOrderingOrId
                }
            }
            def f = new Foo()
            f.setRankOrderingOrId(1L)
            assert f.getRankOrderingOrId() == 1L
            assert f.rankOrderingOrId == 1L
            f.rankOrderingOrId = 2L
            assert f.getRankOrderingOrId() == 2L
            assert f.rankOrderingOrId == 2L
        '''
    }

    // GROOVY-6921
    void testSubscriptOnClosureSharedVariable() {
        assertScript '''
            def versionRanges = [['1.7', 3]]
            def versions = versionRanges.collect { versionRange -> (0..versionRange[1]).collect { "${versionRange[0]}.${it}" } }.flatten()
            assert versions == ['1.7.0','1.7.1','1.7.2','1.7.3']
        '''
    }

    // GROOVY-6924
    void testShouldNotThrowIncompatibleClassChangeError() {
        try {
            assertScript '''import org.codehaus.groovy.classgen.asm.sc.Groovy6924Support
            class Test {
                static void foo() {
                    Groovy6924Support bean = new Groovy6924Support()
                    bean.with {
                        foo = 'foo'
                        bar = 'bar'
                    }
                    String val = "$bean.foo and $bean.bar"
                    assert val == 'foo and bar'
                }
            }
            Test.foo()
        '''
        } finally {
            assert astTrees['Test$_foo_closure1'][1].contains('INVOKEVIRTUAL org/codehaus/groovy/classgen/asm/sc/Groovy6924Support.setFoo (Ljava/lang/String;)V')
        }
    }

    // GROOVY-7381
    void testNonVoidSetterCalls(){
        assertScript '''
            class Foo {
                int num
                String name

                Foo setNum(int num){
                    this.num = num
                    this
                }

                Foo setName(String name){
                    this.name = name
                    this
                }
            }
            Foo foo = new Foo().setNum(1).setName('fluent')
            assert foo.num == 1
            assert foo.name == 'fluent'
        '''
    }

    // GROOVY-7610
    void testNullSafeIsCallConditionShouldNotThrowVerifyError() {
        assertScript '''
            class A {
                void ifCondition(Object x, Object y) {
                    if (x?.is(y))
                        return
                }

                void ternaryCondition(Object x, Object y) {
                    x?.is(y) ? 'foo' : 'bar'
                }
            }
            new A()
        '''
    }

    // GROOVY-7631
    void testPrimitiveNotEqualNullShouldReturnTrue() {
        assertScript '''
            assert false != null
            assert true != null
            assert (byte) 1 != null
            assert (short) 1 != null
            assert 0 != null
            assert 1 != null
            assert 1L != null
            assert 1f != null
            assert 1d != null
            assert (char) 1 != null
        '''
    }

    // GROOVY-7639
    void testComparisonOfPrimitiveWithNullSafePrimitivePropertyExpression() {
        assertScript '''
            class Foo {
                int bar
            }
            Foo foo = null
            assert !(foo?.bar == 7)
            assert !(foo?.bar > 7)
            assert foo?.bar < 7
        '''
    }

    // GROOVY-7841
    void testAssertionOfNonZeroPrimitiveEdgeCases() {
        assertScript '''
            assert 4294967296
            assert 0.1f
            assert 0.1d
        '''
    }

    // GROOVY-7784
    void testWithSamAndVarArgs() {
        assertScript '''
            class Foo {
                static foo(Integer x, Iterable y, String... z) { [*y.toList(), *z].join('-') }
            }

            class Groovy7784 {
                static emptyVarArgs() { Foo.foo(42, { ['foo', 'bar'].iterator() }) }
                static singleVarArgs() { Foo.foo(42, { ['foo', 'bar'].iterator() }, 'baz') }
                static multiVarArgs() { Foo.foo(42, { ['foo', 'bar'].iterator() }, 'baz1', 'baz2') }
            }

            assert Groovy7784.emptyVarArgs() == 'foo-bar'
            assert Groovy7784.singleVarArgs() == 'foo-bar-baz'
            assert Groovy7784.multiVarArgs() == 'foo-bar-baz1-baz2'
        '''
    }

    // GROOVY-7160
    void testGenericsArrayPlaceholder() {
        assertScript '''
            import static java.nio.file.AccessMode.*

            @groovy.transform.CompileStatic
            class Dummy {
                static main() {
                    // more than 5 to match `of(E first, E[] rest)` variant
                    EnumSet.of(READ, WRITE, EXECUTE, READ, WRITE, EXECUTE)
                }
            }

            assert Dummy.main() == [READ, WRITE, EXECUTE].toSet()
        '''
    }
}
