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
package groovy.transform.stc

import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor

/**
 * Unit tests for static type checking : miscellaneous tests.
 */
class MiscSTCTest extends StaticTypeCheckingTestCase {

     void testFibonacci() {
         assertScript '''
            long sd = System.currentTimeMillis()
            int fib(int i) {
                i < 2 ? 1 : fib(i - 2) + fib(i - 1);
            }
            println fib(20)
            long dur = System.currentTimeMillis()-sd
            println "${dur}ms"
         '''
     }

    void testGreeter() {
        assertScript '''
            class Greet {
                def name

                Greet(String who) {
                    name = who[0].toUpperCase() +
                            who[1..-1]
                }

                def salute() { println "Hello $name!" }
            }

            def g = new Greet('world')  // create object
            g.salute()               // output "Hello World!"
        '''
    }

    void testClosureReturnTypeShouldNotBeTestedAgainstMethodReturnType() {
        assertScript '''
        void method() {
            def cl = { String it -> it.toUpperCase() }
            assert cl('test')=='TEST'
        }
        method()
        '''
    }

    void testPropertyOnClass() {
        assertScript '''
            Class clazz = String
            assert clazz.name=='java.lang.String'
        '''
    }

    void testDirectPropertyOnClass() {
        assertScript '''
            assert String.name=='java.lang.String'
        '''
    }

    void testMethodOnClass() {
        assertScript '''
            Class clazz = String
            clazz.getDeclaredFields()
        '''
    }

    void testDirectMethodOnClass() {
        assertScript '''
            String.getDeclaredFields()
        '''
    }

    void testFieldsFromClass() {
        assertScript '''
            String.class.fields
        '''
    }

    void testDirectFieldsFromClass() {
        assertScript '''
            String.fields
        '''
    }

    void testMissingSetter() {
        shouldFailWithMessages '''
            class Foo {
                String getName() { 'Hello' }
            }
            Foo foo = new Foo()
            foo.name = 'Error'
        ''', 'Cannot set read-only property: name'
    }

    void testMissingSetterThroughPath() {
        shouldFailWithMessages '''
            class Foo {
                String getName() { 'Hello' }
            }
            class Bar {
                Foo foo = new Foo()
            }
            Bar bar = new Bar()
            bar.foo.name = 'Error'
        ''', 'Cannot set read-only property: name'
    }

    void testMissingSetterAndWith() {
        shouldFailWithMessages '''
            class Foo {
                String getName() { 'Hello' }
            }
            Foo foo = new Foo()
            foo.with {
                name = 'Error'
            }
        ''', 'Cannot set read-only property: name'
    }

    void testFindMethodFromSameClass() {
        assertScript '''
        class Foo {
            int foo() {
                1
            }
            int bar(int x) {
                foo()
            }
        }
        new Foo().bar(2)
        '''
    }

    void testCallToSuperConstructor() {
        assertScript '''
            class MyException extends Exception {
                MyException(String message) { super(message) }
            }
            1
        '''
    }

    void testCallToThisConstructor() {
        assertScript '''
            class MyException extends Exception {
                MyException(int errNo, String message) {
                    this(message)
                }
                MyException(String message) { super(message) }
            }
            1
        '''
    }

    void testCompareEnumToNull() {
        assertScript '''
            enum MyEnum { a,b }
            MyEnum val = null
            if (val == null) {
                val = MyEnum.a
            }
        '''
    }

    // GROOVY-10197
    void testEnumMethodOverride() {
        assertScript '''
            enum E {
                CONST {
                    int getValue() { 1 }
                }
                int getValue() { -1 }
            }
            assert E.CONST.value == 1
        '''
        assertScript '''
            enum E {
                CONST {
                    final int value = 1
                }
                int getValue() { -1 }
            }
            assert E.CONST.value == 1
        '''
    }

    // GROOVY-10845
    void testEnumConstructorChecks() {
        shouldFailWithMessages '''
            enum E {
                CONST()
                E(String s) { }
            }
        ''',
        'Cannot find matching constructor E()'

        shouldFailWithMessages '''
            enum E {
                CONST(new Object())
                E(String s) { }
            }
        ''',
        'Cannot find matching constructor E(java.lang.Object)'

        shouldFailWithMessages '''
            enum E {
                CONST(new Object())
            }
        ''',
        'Cannot find matching constructor E(java.lang.Object)'

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def init = node.getDeclaredMethod("<clinit>").code.statements[0] // this.CONST = $INIT("CONST",0,"xx")
                def dmct = init.expression.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert dmct != null
                def type = init.expression.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == node
            })
            enum E {
                CONST('xx')
                E(String s) { assert s == 'xx' }
            }
            E.CONST
        '''

        assertScript '''import groovy.transform.stc.SimpleType
            enum E {
                CONST({ assert it.toLowerCase() == 'const' })
                E(@ClosureParams(value=SimpleType, options='java.lang.String') Closure c) {
                    c.call(name())
                }
            }
            E.CONST
        '''
    }

    void testMethodReturnTypeInferenceShouldNotWorkBecauseNotSameSourceUnit() {
        shouldFailWithMessages '''
            import groovy.transform.stc.MiscSTCTest.MiscSTCTestSupport as A
            A.foo().toInteger()
        ''',
        'Cannot find matching method java.lang.Object#toInteger()'
    }

    void testClassLiteralAsArgument() {
        assertScript '''
            void lookup(Class clazz) { }
            lookup(Date)
        '''
    }

    // GROOVY-5922
    void testUnwrapPrimitiveLongType() {

        assertScript '''
            long[] data = [0] as long[]

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            long mask = 0 | 0x1L
        '''

        assertScript '''
            long[] data = [0] as long[]
            data[0] = 0x1L

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            def value = data[0]
        '''

        assertScript '''
            def c = { -> 42L }

            long[] data = [0] as long[]

            data[0] = c()

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == long_TYPE
            })
            def value = data[0]
        '''

        assertScript '''
            def c = { -> 42L }

            Long[] data = [0] as Long[]

            data[0] = c()

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Long_TYPE
            })
            def value = data[0]
        '''
    }

    // GROOVY-6165 && GROOVY-6196
    void testPropertyNameFromMethodName() {
        // too bad we're not using Spock!

        def tests = [
                ['get','getName', 'name'],
                ['get','getFullName', 'fullName'],
                ['get','getname', null],
                ['is', 'isFlag', 'flag'],
                ['is', 'isflag', null],
                ['is', 'is', null],
                ['is', 'i', null],
                ['get', 'getXYZ', 'XYZ'],
                ['get', 'get_foo', '_foo'],
                [null, 'foo', null],
                ['foo', null, null],
                [null,null,null],
                ['get', 'getaList', 'aList']
        ]
        tests.each { prefix, methodName, expectation ->
            assert StaticTypeCheckingVisitor.extractPropertyNameFromMethodName(prefix, methodName) == expectation
        }
    }

    static class MiscSTCTestSupport {
        static def foo() { '123' }
    }

    void testTernaryParam() {
        assertScript '''
            Date ternaryParam(Object input) {
                input instanceof Date ? input : null
            }
            def d = new Date()
            assert ternaryParam(42) == null
            assert ternaryParam('foo') == null
            assert ternaryParam(d) == d
        '''
    }

    void testTernaryLocalVar() {
        assertScript '''
            Date ternaryLocalVar(Object input) {
                Object copy = input
                copy instanceof Date ? copy : null
            }
            def d = new Date()
            assert ternaryLocalVar(42) == null
            assert ternaryLocalVar('foo') == null
            assert ternaryLocalVar(d) == d
        '''
    }

    void testIfThenElseParam() {
        assertScript '''
            Date ifThenElseParam(Object input) {
                if (input instanceof Date) {
                    input
                } else {
                    null
                }
            }
            def d = new Date()
            assert ifThenElseParam(42) == null
            assert ifThenElseParam('foo') == null
            assert ifThenElseParam(d) == d
        '''
    }

    void testIfThenElseLocalVar() {
        assertScript '''
            Date ifThenElseLocalVar(Object input) {
                Date result
                if (input instanceof Date) {
                    result = input
                } else {
                    result = null
                }
                result
            }
            def d = new Date()
            assert ifThenElseLocalVar(42) == null
            assert ifThenElseLocalVar('foo') == null
            assert ifThenElseLocalVar(d) == d
        '''
    }

    void testIfThenElseLocalVar2() {
        assertScript '''
            class FooBase {}
            class FooChild extends FooBase{}
            FooChild ifThenElseLocalVar2(FooBase input) {
                FooChild result
                if (input instanceof FooChild) {
                    result = input
                } else {
                    result = null
                }
                result
            }
            def fc = new FooChild()
            assert ifThenElseLocalVar2(fc) == fc
            assert ifThenElseLocalVar2(new FooBase()) == null
        '''
    }

    // GROOVY-8325
    void testNumericCoercion() {
        assertScript '''
            class Foo {
                Long val
                static Foo newInstance(Long val) {
                    return new Foo(val: val)
                }
            }
            class FooFactory {
                static Foo create() {
                    Foo.newInstance(123)
                }
            }
            assert FooFactory.create().val == 123
        '''
    }

    void testNumericCoercionWithCustomNumber() {
        shouldFailWithMessages '''
            class CustomNumber extends Number {
                @Delegate Long delegate = 42L
            }
            class Foo {
                Integer val
                static Foo newInstance2(Integer val) {
                    return new Foo(val: val)
                }
            }
            class FooFactory {
                static Foo create() {
                    Foo.newInstance2(new CustomNumber())
                }
            }
        ''',
        'Cannot find ',' static method Foo#newInstance2(CustomNumber)'
    }

    // GROOVY-8380
    void testBitOperatorsWithNumbers() {
        assertScript '''
            def method() {
                Long wl = 2L
                long pl = 3L
                assert new Long(wl & 3L) == 2
                assert new Long(pl & pl) == 3
                assert new Long(6L & 3L) == 2
                assert new Long(-2L & 3L) == 2
                Integer wi = 2
                int pi = 2
                assert new Integer(wi & 34) == 2
                assert new Integer(pi & pi) == 2
                assert new Integer(6 & 3) == 2
                assert new Integer(-2 & 3) == 2
            }
            method()
        '''
    }

    // GROOVY-8384
    void testIntdiv() {
        assertScript '''
            def method() {
                assert new Long(7L.multiply(3)) == 21
                assert new Long(7L.plus(3)) == 10
                assert new Long(7L.leftShift(3)) == 56
                assert new Long(7L.rightShift(1)) == 3
                assert new Long(7L.mod(3)) == 1
                assert new Long(7L.remainder(3)) == 1
                assert new Long(7L.intdiv(3)) == 2
                assert new Integer((-8).intdiv(-4)) == 2
                Integer x = 9
                Integer y = 5
                assert new Integer(x.intdiv(y)) == 1
            }
            method()
        '''
    }
}
