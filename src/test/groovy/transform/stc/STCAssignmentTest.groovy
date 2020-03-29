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

/**
 * Unit tests for static type checking : assignments.
 */
class STCAssignmentTest extends StaticTypeCheckingTestCase {

    void testAssignmentFailure() {
        shouldFailWithMessages '''
            int x = new Object()
        ''', 'Cannot assign value of type java.lang.Object to variable of type int'
    }

    void testAssignmentFailure2() {
        shouldFailWithMessages '''
            Set set = new Object()
        ''', 'Cannot assign value of type java.lang.Object to variable of type java.util.Set'
    }

    void testAssignmentFailure3() {
        shouldFailWithMessages '''
            Set set = new Integer(2)
        ''', 'Cannot assign value of type java.lang.Integer to variable of type java.util.Set'
    }

    void testIndirectAssignment() {
        shouldFailWithMessages '''
            def o = new Object()
            int x = o
        ''', 'Cannot assign value of type java.lang.Object to variable of type int'
    }

    void testIndirectAssignment2() {
        shouldFailWithMessages '''
            def o = new Object()
            Set set = o
        ''', 'Cannot assign value of type java.lang.Object to variable of type java.util.Set'
    }

    void testIndirectAssignment3() {
        shouldFailWithMessages '''
            int x = 2
            Set set = x
        ''', 'Cannot assign value of type int to variable of type java.util.Set'
    }

    void testAssignmentToEnum() {
        assertScript '''
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 'a' // string to enum is implicit
            e = "${'a'}" // gstring to enum is implicit too
        '''
    }

    void testAssignmentToEnumFailure() {
        shouldFailWithMessages '''
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 1
        ''', 'Cannot assign value of type int to variable of type MyEnum'
    }

    void testAssignmentToString() {
        assertScript '''
            String str = new Object()
        '''
    }

    void testAssignmentToBoolean() {
        assertScript '''
            boolean test = new Object()
        '''
    }

    void testAssignmentToBooleanClass() {
        assertScript '''
            Boolean test = new Object()
        '''
    }

    void testAssignmentToClass() {
        assertScript '''
            Class test = 'java.lang.String'
        '''
    }

    void testPlusEqualsOnInt() {
        assertScript '''
            int i = 0
            i += 1
        '''
    }

    void testMinusEqualsOnInt() {
        assertScript '''
            int i = 0
            i -= 1
        '''
    }

    void testIntPlusEqualsObject() {
        shouldFailWithMessages '''
            int i = 0
            i += new Object()
        ''', 'Cannot find matching method int#plus(java.lang.Object)'
    }

    void testIntMinusEqualsObject() {
        shouldFailWithMessages '''
            int i = 0
            i -= new Object()
        ''', 'Cannot find matching method int#minus(java.lang.Object)'
    }

    void testStringPlusEqualsString() {
        assertScript '''
            String str = 'test'
            str += 'test2'
        '''
    }

    void testPlusEqualsOnProperty() {
        assertScript '''
            class C {
                int i

                static main(args) {
                    def c = new C()
                    c.i = 5
                    c.i += 10
                    assert c.i == 15
                }
            }
        '''
    }

    // GROOVY-9385
    void testPlusEqualsOnPrivateField() {
        assertScript '''
            class C {
                private int i

                int test() {
                    { ->
                        i += 1
                    }.call()
                }
            }
            assert new C().test() == 1
        '''
    }

    // GROOVY-9385
    void testPrefixPlusPlusOnPrivateField() {
        assertScript '''
            class C {
                private int i

                int test() {
                    { ->
                        ++i
                    }.call()
                }
            }
            assert new C().test() == 1
        '''
    }

    // GROOVY-9385
    void testPostfixPlusPlusOnPrivateField() {
        assertScript '''
            class C {
                private int i

                int test() {
                    { ->
                        i++
                    }.call()
                }
            }
            assert new C().test() == 0
        '''
    }

    void testPossibleLooseOfPrecision() {
        shouldFailWithMessages '''
            long a = Long.MAX_VALUE
            int b = a
        ''', 'Possible loss of precision from long to int'
    }

    void testPossibleLooseOfPrecision2() {
        assertScript '''
            int b = 0L
        '''
    }

    void testPossibleLooseOfPrecision3() {
        assertScript '''
            byte b = 127
        '''
    }

    void testPossibleLooseOfPrecision4() {
        shouldFailWithMessages '''
            byte b = 128 // will not fit in a byte
        ''', 'Possible loss of precision from int to byte'
    }

    void testPossibleLooseOfPrecision5() {
        assertScript '''
            short b = 128
        '''
    }

    void testPossibleLooseOfPrecision6() {
        shouldFailWithMessages '''
            short b = 32768 // will not fit in a short
        ''', 'Possible loss of precision from int to short'
    }

    void testPossibleLooseOfPrecision7() {
        assertScript '''
            int b = 32768L // mark it as a long, but it fits into an int
        '''
    }

    void testPossibleLooseOfPrecision8() {
        assertScript '''
            int b = 32768.0f // mark it as a float, but it fits into an int
        '''
    }

    void testPossibleLooseOfPrecision9() {
        assertScript '''
            int b = 32768.0d // mark it as a double, but it fits into an int
        '''
    }

    void testPossibleLooseOfPrecision10() {
        shouldFailWithMessages '''
            int b = 32768.1d
        ''', 'Possible loss of precision from double to int'
    }

    void testCastIntToShort() {
        assertScript '''
            short s = (short) 0
        '''
    }

    void testCastIntToFloat() {
        assertScript '''
            float f = (float) 1
        '''
    }

    void testCompatibleTypeCast() {
        assertScript '''
        String s = 'Hello'
        ((CharSequence) s)
        '''
    }

    void testIncompatibleTypeCast() {
        shouldFailWithMessages '''
            String s = 'Hello'
            ((Set) s)
        ''', 'Inconvertible types: cannot cast java.lang.String to java.util.Set'
    }

    void testIncompatibleTypeCastWithAsType() {
        // If the user uses explicit type coercion, there's nothing we can do
        assertScript '''
            String s = 'Hello'
            s as Set
        '''
    }

    void testIncompatibleTypeCastWithTypeInference() {
        shouldFailWithMessages '''
            def s = 'Hello'
            s = 1
            ((Set) s)
        ''', 'Inconvertible types: cannot cast int to java.util.Set'
    }

    void testArrayLength() {
        assertScript '''
            String[] arr = [1,2,3]
            int len = arr.length
        '''
    }

    void testMultipleAssignment1() {
        assertScript '''
            def (x,y) = [1,2]
            assert x == 1
            assert y == 2
        '''
    }

    void testMultipleAssignmentWithExplicitTypes() {
        assertScript '''
            int x
            int y
            (x,y) = [1,2]
            assert x == 1
            assert y == 2
        '''
    }

    void testMultipleAssignmentWithIncompatibleTypes() {
        shouldFailWithMessages '''
            List x
            List y
            (x,y) = [1,2]
        ''', 'Cannot assign value of type int to variable of type java.util.List'
    }

    void testMultipleAssignmentWithoutEnoughArgs() {
        shouldFailWithMessages '''
            int x
            int y
            (x,y) = [1]
        ''', 'Incorrect number of values. Expected:2 Was:1'
    }

    void testMultipleAssignmentTooManyArgs() {
        assertScript '''
            int x
            int y
            (x,y) = [1,2,3]
            assert x == 1
            assert y == 2
        '''
    }

    void testMultipleAssignmentFromVariable() {
        shouldFailWithMessages '''
            def list = [1,2,3]
            def (x,y) = list
        ''', 'Multiple assignments without list expressions on the right hand side are unsupported in static type checking mode'
    }

    void testAssignmentToInterface() {
        assertScript '''
            Serializable ser = 'Hello'
        '''
    }

    void testAssignmentToIncompatibleInterface() {
        shouldFailWithMessages '''
            Collection ser = 'Hello'
        ''', 'Cannot assign value of type java.lang.String to variable of type java.util.Collection'
    }

    void testTernaryOperatorAssignmentShouldFailBecauseOfIncompatibleGenericTypes() {
        shouldFailWithMessages '''
            List<Integer> foo = true?new LinkedList<String>():new LinkedList<Integer>();
        ''', 'Incompatible generic argument types. Cannot assign java.util.LinkedList <? extends java.io.Serializable <? extends java.io.Serializable>> to: java.util.List <Integer>'
    }

    void testCastStringToChar() {
        assertScript '''
            char c = 'a'
        '''
    }

    void testCastStringLongerThan1CharToChar() {
        shouldFailWithMessages '''
            char c = 'aa'
        ''','Cannot assign value of type java.lang.String to variable of type char'
    }

    void testCastNullToChar() {
        shouldFailWithMessages '''
            char c = null
        ''', 'Cannot assign value of type java.lang.Object to variable of type char'
    }

    // GROOVY-6577
    void testCastNullToBoolean() {
        assertScript '''
            boolean c = null
            assert c == false
        '''
    }

    // GROOVY-6577
    void testCastNullToBooleanWithExplicitCast() {
        assertScript '''
            boolean c = (boolean) null
            assert c == false
        '''
    }

    void testCastStringToCharacter() {
        assertScript '''
            Character c = 'a'
        '''
    }

    void testCastStringLongerThan1CharToCharacter() {
        shouldFailWithMessages '''
            Character c = 'aa'
        ''','Cannot assign value of type java.lang.String to variable of type java.lang.Character'
    }

    void testAssignNullToCharacter() {
        assertScript '''
            Character c = null
        '''
    }

    void testCastStringToCharWithCast() {
        assertScript '''
            def c = (char) 'a'
        '''
    }

    void testCastCharToByte() {
        assertScript '''
            void foo(char c) {
                byte b = (byte) c
            }
        '''
    }

    void testCastCharToInt() {
        assertScript '''
            void foo(char c) {
                int b = (int) c
            }
        '''
    }

    void testCastStringLongerThan1ToCharWithCast() {
        shouldFailWithMessages '''
            def c = (char) 'aa'
        ''', 'Inconvertible types: cannot cast java.lang.String to char'
    }

    void testCastNullToCharWithCast() {
        shouldFailWithMessages '''
            def c = (char) null
        ''', 'Inconvertible types: cannot cast java.lang.Object to char'
    }

    void testCastStringToCharacterWithCast() {
        assertScript '''
            def c = (Character) 'a'
        '''
    }

    void testCastStringLongerThan1ToCharacterWithCast() {
        shouldFailWithMessages '''
            def c = (Character) 'aa'
        ''', 'Inconvertible types: cannot cast java.lang.String to java.lang.Character'
    }

    void testCastNullToCharacterWithCast() {
        assertScript '''
            def c = (Character) null
        '''
    }

    void testCastObjectToSubclass() {
        assertScript '''
            Object o = null
            try {
                ((Integer)o).intValue()
            } catch (NullPointerException e) {
            }
        '''
    }

    void testIfElseBranch() {
        shouldFailWithMessages '''
            def x
            def y = 'foo'
            if (y) {
                x = new HashSet()
            } else {
                x = '123'
            }
            x.toInteger()
        ''', 'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testIfElseBranchParameter() {
        shouldFailWithMessages '''
            def foo(x) {
                def y = 'foo'
                if (y) {
                    x = new HashSet()
                } else {
                    x = '123'
                }
                x.toInteger()
            }
            foo('bar')
        ''', 'Cannot find matching method java.lang.Object#toInteger()'
    }

    void testIfOnly() {
        shouldFailWithMessages '''
            def x = '123'
            def y = 'foo'
            if (y) {
                x = new HashSet()
            }
            x.toInteger()
        ''', 'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testIfOnlyParameter() {
        shouldFailWithMessages '''
            def foo(x) {
                def y = 'foo'
                if (y) {
                    x = new HashSet()
                    assert x.isEmpty()
                }
                x.toInteger()
            }
            foo('123')
        ''', 'Cannot find matching method java.lang.Object#toInteger()'
    }

    void testIfWithCommonInterface() {
        assertScript '''
            interface Foo { void foo() }
            class A implements Foo { void foo() { println 'A' } }
            class B implements Foo { void foo() { println 'B' } }
            def x = new A()
            def y = 'foo'
            if (y) {
                x = new B()
            }
            x.foo()
        '''
    }

    void testForLoopWithNewAssignment() {
        shouldFailWithMessages '''
            def x = '123'
            for (int i=0; i<5;i++) { x = new HashSet() }
            x.toInteger()
        ''', 'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testWhileLoopWithNewAssignment() {
        shouldFailWithMessages '''
            def x = '123'
            while (false) { x = new HashSet() }
            x.toInteger()
        ''', 'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testTernaryWithNewAssignment() {
        shouldFailWithMessages '''
            def x = '123'
            def cond = false
            cond?(x = new HashSet()):3
            x.toInteger()
        ''', 'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testFloatSub() {
        assertScript '''
            float x = 1.0f
            float y = 1.0f
            float z = x-y
        '''
    }

    void testDoubleMinusInt() {
        assertScript '''
            double m() {
                double a = 10d
                int b = 1
                double c = a-b
            }
            assert m()==9d
        '''
    }

    void testDoubleMinusFloat() {
        assertScript '''
            double m() {
                double a = 10d
                float b = 1f
                double c = a-b
            }
            assert m()==9d
        '''
    }

    void testBigDecimalSub() {
        assertScript '''
            BigDecimal m() {
                BigDecimal a = 10
                BigDecimal b = 10
                BigDecimal c = a-b
            }
            assert m()==0
            assert m().getClass() == BigDecimal
        '''
    }

    void testBigDecimalMinusDouble() {
        assertScript '''
            BigDecimal m() {
                BigDecimal a = 10
                double b = 10d
                BigDecimal c = a-b
            }
            assert m()==0
            assert m().getClass() == BigDecimal
        '''
    }

    void testFloatSum() {
        assertScript '''
            float x = 1.0f
            float y = 1.0f
            float z = x+y
        '''
    }

    void testDoublePlusInt() {
        assertScript '''
            double m() {
                double a = 10d
                int b = 1
                double c = a+b
            }
            assert m()==11d
        '''
    }

    void testDoublePlusFloat() {
        assertScript '''
            double m() {
                double a = 10d
                float b = 1f
                double c = a+b
            }
            assert m()==11d
        '''
    }

    void testBigDecimalSum() {
        assertScript '''
            BigDecimal m() {
                BigDecimal a = 10
                BigDecimal b = 10
                BigDecimal c = a+b
            }
            assert m()==20
            assert m().getClass() == BigDecimal
        '''
    }

    void testBigDecimalPlusDouble() {
        assertScript '''
            BigDecimal m() {
                BigDecimal a = 10
                double b = 10d
                BigDecimal c = a+b
            }
            assert m()==20
            assert m().getClass() == BigDecimal
        '''
    }

    void testBigIntegerAssignment() {
        assertScript '''
            BigInteger bigInt = 6666666666666666666666666666666666666
            assert bigInt.toString()=='6666666666666666666666666666666666666'
            assert bigInt.class == BigInteger
        '''
    }

    void testBigIntegerSum() {
        assertScript '''
            BigInteger a = 6666666666666666666666666666666666666
            BigInteger b = 6666666666666666666666666666666666666
            BigInteger c = a + b
            assert c.toString()=='13333333333333333333333333333333333332'
            assert c.class == BigInteger
        '''
    }

    void testBigIntegerSub() {
        assertScript '''
            BigInteger a = 6666666666666666666666666666666666666
            BigInteger b = 6666666666666666666666666666666666666
            BigInteger c = a - b
            assert c.toString()=='0'
            assert c.class == BigInteger
        '''
    }

    void testBigIntegerMult() {
        assertScript '''
            BigInteger a = 6666666666666666666666666666666666666
            BigInteger b = 2
            BigInteger c = a * b
            assert c.toString()=='13333333333333333333333333333333333332'
            assert c.class == BigInteger
        '''
    }

    void testBigIntegerMultDouble() {
       assertScript '''
            BigInteger a = 333
            double b = 2d
            BigDecimal c = a * b
            assert c == 666
            assert c.getClass() == BigDecimal
        '''

        shouldFailWithMessages '''
            BigInteger a = 333
            double b = 2d
            BigInteger c = a * b
        ''', 'Cannot assign value of type java.math.BigDecimal to variable of type java.math.BigInteger'
    }

    void testBigIntegerMultInteger() {
        assertScript '''
            BigInteger a = 333
            int b = 2
            BigDecimal c = a * b
            assert c == 666
            assert c.getClass() == BigDecimal
        '''
    }

    //GROOVY-6435
    void testBigDecAndBigIntSubclass() {
        assertScript '''
            class MyDecimal extends BigDecimal {
              public MyDecimal(String s) {super(s)}
            }
            class MyInteger extends BigInteger {
              public MyInteger(String s) {super(s)}
            }

            BigDecimal d = new MyDecimal('3.0')
            BigInteger i = new MyInteger('3')
        '''
    }

    void testPostfixOnInt() {
        assertScript '''
            int i = 0
            i++
        '''
        assertScript '''
            int i = 0
            i--
        '''
    }

    void testPostfixOnDate() {
        assertScript '''
            Date d = new Date()
            d++
        '''
        assertScript '''
            Date d = new Date()
            d--
        '''
    }

    // GROOVY-9389
    void testPostfixOnNumber() {
        assertScript '''
            class Pogo {
                Integer integer = 0
                Integer getInteger() { return integer }
                void setInteger(Integer i) { integer = i }
            }
            new Pogo().integer++
        '''
        shouldFailWithMessages '''
            class Pogo {
                Integer integer = 0
                Integer getInteger() { return integer }
                void setInteger(Character c) { integer = (c as int) }
            }
            new Pogo().integer++
        ''', 'Cannot assign value of type java.lang.Integer to variable of type java.lang.Character'
    }

    void testPostfixOnObject() {
        shouldFailWithMessages '''
            Object o = new Object()
            o++
        ''', 'Cannot find matching method java.lang.Object#next()'
        shouldFailWithMessages '''
            Object o = new Object()
            o--
        ''', 'Cannot find matching method java.lang.Object#previous()'
    }

    void testPrefixOnInt() {
        assertScript '''
            int i = 0
            ++i
        '''
        assertScript '''
            int i = 0
            --i
        '''
    }

    void testPrefixOnDate() {
        assertScript '''
            Date d = new Date()
            ++d
        '''
        assertScript '''
            Date d = new Date()
            --d
        '''
    }

    void testPrefixOnObject() {
        shouldFailWithMessages '''
            Object o = new Object()
            ++o
        ''', 'Cannot find matching method java.lang.Object#next()'
        shouldFailWithMessages '''
            Object o = new Object()
            --o
        ''', 'Cannot find matching method java.lang.Object#previous()'
    }

    void testAssignArray() {
        assertScript '''
            String[] src = ['a','b','c']
            Object[] arr = src
        '''
    }

    void testCastArray() {
        assertScript '''
            List<String> src = ['a','b','c']
            (String[]) src.toArray(src as String[])
        '''
    }

    void testIncompatibleCastArray() {
        shouldFailWithMessages '''
            String[] src = ['a','b','c']
            (Set[]) src
        ''', 'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    void testIncompatibleToArray() {
        shouldFailWithMessages '''
            (Set[]) ['a','b','c'].toArray(new String[3])
        ''', 'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    // GROOVY-5535
    void testAssignToNullInsideIf() {
        assertScript '''
            Date foo() {
                Date result = new Date()
                if (true) {
                    result = null
                }
                return result
            }
            assert foo() == null
        '''
    }

    // GROOVY-5798
    void testShouldNotThrowConversionError() {
        assertScript '''
            char m( int v ) {
              char c = (char)v
              c
            }

            println m( 65 )
        '''
    }

    // GROOVY-6575
    void testAssignmentOfObjectToArrayShouldFail() {
        shouldFailWithMessages '''
            Object o
            int[] array = o
        ''', 'Cannot assign value of type java.lang.Object to variable of type int[]'
    }

    // GROOVY-7015
    void testAssingmentToSuperclassFieldWithDifferingGenerics() {
        assertScript '''
            class Base {}
            class Derived extends Base {
                public String sayHello() { 'hello' }
            }

            class GBase<T extends Base> {
                T myVar;
            }
            class GDerived extends GBase<Derived> {
                GDerived() { myVar = new Derived(); }
                public String method() {myVar.sayHello()}
            }

            GDerived d = new GDerived();
            assert d.method() == 'hello'
        '''
    }

    //GROOVY-8157
    void testFlowTypingAfterParameterAssignment() {
        assertScript '''
            class A {}
            class B extends A { def bbb() { 42 } }

            def fooParameterAssignment(A a) {
                a = new B()
                a.bbb()
            }
            assert fooParameterAssignment(null) == 42
        '''
    }

    void testIntegerArraySmartType() {
        assertScript '''
        def m() {
            def a  = 1
            Integer[] b = [a]
        }
        '''
    }

    void testIntegerSecondDimArraySmartType() {
        assertScript '''
        def m() {
            def a = new int[5]
            int[][] b = [a]
        }
        '''
    }

    void testMultiAssign() {
        assertScript '''
        def m() {
            def row = ['', '', '']
            def (left, right) = [row[0], row[1]]
            left.toUpperCase()
        }
        '''
    }

    // GROOVY-8220
    void testFlowTypingParameterTempTypeAssignmentTracking() {
        assertScript '''
            class Foo {
                CharSequence makeEnv( env, StringBuilder result = new StringBuilder() ) {
                    if (env instanceof File) {
                        env = env.toPath()
                    }
                    if (env instanceof String && env.contains('=')) {
                        result << 'export ' << env << ';'
                    }
                    return result.toString()
                }
            }
            assert new Foo().makeEnv('X=1') == 'export X=1;'
        '''
        // GROOVY-8237
        assertScript '''
            class Foo {
                String parse(Reader reader) {
                    if (reader == null)
                        reader = new BufferedReader(reader)
                    int i = reader.read()
                    return (i != -1) ? 'bar' : 'baz'
                }
            }
            assert new Foo().parse(new StringReader('foo')) == 'bar'
        '''
    }

    void testFlowTypingParameterTempTypeAssignmentTrackingWithGenerics() {
        assertScript '''
            class M {
                Map<String, List<Object>> mvm = new HashMap<String, List<Object>>()
                void setProperty(String name, value) {
                    if (value instanceof File) {
                        value = new File(value, 'bar.txt')
                    }
                    else if (value instanceof URL) {
                        value = value.toURI()
                    }
                    else if (value instanceof InputStream) {
                        value = new BufferedInputStream(value)
                    }
                    else if (value instanceof GString) {
                        value = value.toString()
                    }
                    if (mvm[name]) {
                        mvm[name].add value
                    } else {
                        mvm.put(name, [value])
                    }
                }
            }
            new M().setProperty('foo', 'bar')
        '''
    }

    void testNarrowingConversion() {
        assertScript '''
        interface A1{}
        interface A2 extends A1{}

        class C1 implements A1{}

        def m(A2 a2) {
            C1 c1 = (C1) a2
        }
        '''
    }

    void testFinalNarrowingConversion() {
        shouldFailWithMessages '''
        interface A1{}
        interface A2 extends A1{}

        final class C1 implements A1{}

        def m(A2 a2) {
            C1 c1 = (C1) a2
        }
        ''', 'Inconvertible types: cannot cast A2 to C1'
    }
}
