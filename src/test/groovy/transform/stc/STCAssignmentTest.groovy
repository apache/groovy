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

    void testAssignmentFailure1() {
        shouldFailWithMessages '''
            int x = new Object()
        ''',
        'Cannot assign value of type java.lang.Object to variable of type int'
    }

    void testAssignmentFailure2() {
        shouldFailWithMessages '''
            Set set = new Object()
        ''',
        'Cannot assign value of type java.lang.Object to variable of type java.util.Set'
    }

    void testAssignmentFailure3() {
        shouldFailWithMessages '''
            Set set = new Integer(2)
        ''',
        'Cannot assign value of type java.lang.Integer to variable of type java.util.Set'
    }

    void testAssignmentFailure4() {
        shouldFailWithMessages '''
            def o = new Object()
            int x = o
        ''',
        'Cannot assign value of type java.lang.Object to variable of type int'
    }

    void testAssignmentFailure5() {
        shouldFailWithMessages '''
            def o = new Object()
            Set set = o
        ''',
        'Cannot assign value of type java.lang.Object to variable of type java.util.Set'
    }

    void testAssignmentFailure6() {
        shouldFailWithMessages '''
            int x = 2
            Set set = x
        ''',
        'Cannot assign value of type int to variable of type java.util.Set'
    }

    // GROOVY-11289
    void testAssignmentFailure7() {
        shouldFailWithMessages '''
            class C {
                void setP(java.util.regex.Matcher matcher) {}
                void setP(java.util.regex.Pattern pattern) {}
            }
            new C(p: new Object())
        ''',
        'Cannot assign value of type java.lang.Object to variable of type java.util.regex.Matcher or java.util.regex.Pattern'
    }

    void testAssignmentToEnum() {
        assertScript '''
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 'a' // string to enum is implicit
            e = "${'a'}" // gstring to enum is implicit too
        '''
        shouldFailWithMessages '''
            enum MyEnum { a, b, c }
            MyEnum e = MyEnum.a
            e = 1
        ''',
        'Cannot assign value of type int to variable of type MyEnum'
    }

    void testAssignmentToClass() {
        assertScript '''
            Class test = 'java.lang.String'
        '''
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

    // GROOVY-10744
    void testAssignmentToSerializable() {
        // Number implements Serializable
        assertScript '''
            Serializable x = 0
            Serializable y = 1.2
            Serializable z = Math.PI
            assert x.class.name == 'java.lang.Integer'
            assert y.class.name == 'java.math.BigDecimal'
            assert z.class.name == 'java.lang.Double'
        '''
        // Boolean implements Serializable and Comparable<Boolean>
        assertScript '''
            Serializable x = true
            Comparable<Boolean> y = false
            assert x.class.name == 'java.lang.Boolean'
            assert y.class.name == 'java.lang.Boolean'
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
        ''',
        'Cannot find matching method java.lang.Integer#plus(java.lang.Object)'
    }

    void testIntMinusEqualsObject() {
        shouldFailWithMessages '''
            int i = 0
            i -= new Object()
        ''',
        'Cannot find matching method java.lang.Integer#minus(java.lang.Object)'
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
            }
            def c = new C(i: 5)
            def ret = c.i += 10

            assert c.i == 15
            assert ret == 15
        '''
    }

    // GROOVY-5746
    void testPlusEqualsAndSubscript() {
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            int getIndex() { i++ }
            def list = ['x','y','z']

            assert (list[index] += '!') == 'x!'
            assert (list[index] += '!') == 'y!'
            assert list.toString() == '[x!, y!, z]'
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

    void testPossibleLossOfPrecision1() {
        shouldFailWithMessages '''
            long a = Long.MAX_VALUE
            int b = a
        ''',
        'Possible loss of precision from long to int'
    }

    void testPossibleLossOfPrecision2() {
        assertScript '''
            int b = 0L
        '''
    }

    void testPossibleLossOfPrecision3() {
        assertScript '''
            byte b = 127
        '''
    }

    void testPossibleLossOfPrecision4() {
        shouldFailWithMessages '''
            byte b = 128 // will not fit in a byte
        ''',
        'Possible loss of precision from int to byte'
    }

    void testPossibleLossOfPrecision5() {
        assertScript '''
            short b = 128
        '''
    }

    void testPossibleLossOfPrecision6() {
        shouldFailWithMessages '''
            short b = 32768 // will not fit in a short
        ''',
        'Possible loss of precision from int to short'
    }

    void testPossibleLossOfPrecision7() {
        assertScript '''
            int b = 32768L // mark it as a long, but it fits into an int
        '''
    }

    void testPossibleLossOfPrecision8() {
        assertScript '''
            int b = 32768.0f // mark it as a float, but it fits into an int
        '''
    }

    void testPossibleLossOfPrecision9() {
        assertScript '''
            int b = 32768.0d // mark it as a double, but it fits into an int
        '''
    }

    void testPossibleLossOfPrecision10() {
        shouldFailWithMessages '''
            int b = 32768.1d
        ''',
        'Possible loss of precision from double to int'
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
        ''',
        'Inconvertible types: cannot cast java.lang.String to java.util.Set'
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
        ''',
        'Inconvertible types: cannot cast int to java.util.Set'
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
        ''',
        'Cannot assign value of type int to variable of type java.util.List'
    }

    void testMultipleAssignmentWithoutEnoughArgs() {
        shouldFailWithMessages '''
            int x
            int y
            (x,y) = [1]
        ''',
        'Incorrect number of values. Expected:2 Was:1'
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
        ''',
        'Multiple assignments without list or tuple on the right-hand side are unsupported in static type checking mode'
    }

    // GROOVY-8223, GROOVY-8887, GROOVY-10063
    void testMultipleAssignmentFromTupleTypes() {
        assertScript '''
            def (String string) = Tuple.tuple('answer')
            assert string == 'answer'
        '''

        assertScript '''
            def (String string, Integer number) = Tuple.tuple('answer', 42)
            assert string == 'answer'
            assert number == 42
        '''

        shouldFailWithMessages '''
            def (String string, Integer number) = Tuple.tuple('answer', '42')
        ''',
        'Cannot assign value of type java.lang.String to variable of type java.lang.Integer'

        assertScript '''
            def (int a, int b, int c, int d, int e, int f, int g, int h, int i, int j, int k, int l, int m, int n, int o, int p) = Tuple.tuple(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16)
            assert a == 1
            assert b == 2
            assert c == 3
            assert d == 4
            assert e == 5
            assert f == 6
            assert g == 7
            assert h == 8
            assert i == 9
            assert j == 10
            assert k == 11
            assert l == 12
            assert m == 13
            assert n == 14
            assert o == 15
            assert p == 16
        '''

        assertScript '''
            def (String string, Integer number) = new Tuple2<String, Integer>('answer', 42)
            assert string == 'answer'
            assert number == 42
        '''

        assertScript '''
            Tuple2<String, Integer> m() {
                new Tuple2<>('answer', 42)
            }

            def (String string, Integer number) = m()
            assert string == 'answer'
            assert number == 42
        '''

        assertScript '''
            Tuple2<String, Integer> m() {
                new Tuple2<>('answer', 42)
            }

            def tuple = m()
            def (String string, Integer number) = tuple
            assert string == 'answer'
            assert number == 42
        '''

        assertScript '''
            static Tuple2<String, Integer> m() {
                new Tuple2<>('answer', 42)
            }

            def (String string, Integer number) = m()
            assert string == 'answer'
            assert number == 42
        '''

        assertScript '''
            class C {
                static Tuple2<String, Integer> m() {
                    new Tuple2<>('answer', 42)
                }
            }

            def (String string, Integer number) = C.m()
            assert string == 'answer'
            assert number == 42
        '''

        assertScript '''
            class C {
                Tuple2<String, Integer> getM() {
                    new Tuple2<>('answer', 42)
                }
            }

            def (String string, Integer number) = new C().m
            assert string == 'answer'
            assert number == 42
        '''
    }

    void testAssignmentToInterface() {
        assertScript '''
            Comparable<String> x = 'x'
            CharSequence y = 'y'
        '''
        shouldFailWithMessages '''
            Collection z = 'z'
        ''',
        'Cannot assign value of type java.lang.String to variable of type java.util.Collection'
    }

    void testTernaryOperatorAssignmentShouldFailBecauseOfIncompatibleGenericTypes() {
        shouldFailWithMessages '''
            List<Integer> foo = true ? new LinkedList<String>() : new LinkedList<Integer>()
        ''',
        'Incompatible generic argument types. Cannot assign java.util.LinkedList<? extends java.io.Serializable & java.lang.Comparable','> to: java.util.List<java.lang.Integer>'
    }

    void testCastStringToChar() {
        assertScript '''
            char c = 'a'
        '''
    }

    void testCastStringLongerThan1CharToChar() {
        shouldFailWithMessages '''
            char c = 'aa'
        ''',
        'Cannot assign value of type java.lang.String to variable of type char'
    }

    void testCastNullToChar() {
        shouldFailWithMessages '''
            char c = null
        ''',
        'Cannot assign value of type java.lang.Object to variable of type char'
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
        ''',
        'Cannot assign value of type java.lang.String to variable of type java.lang.Character'
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
        ''',
        'Inconvertible types: cannot cast java.lang.String to char'
    }

    void testCastNullToCharWithCast() {
        shouldFailWithMessages '''
            def c = (char) null
        ''',
        'Inconvertible types: cannot cast java.lang.Object to char'
    }

    void testCastStringToCharacterWithCast() {
        assertScript '''
            def c = (Character) 'a'
        '''
    }

    void testCastStringLongerThan1ToCharacterWithCast() {
        shouldFailWithMessages '''
            def c = (Character) 'aa'
        ''',
        'Inconvertible types: cannot cast java.lang.String to java.lang.Character'
    }

    void testCastNullToCharacterWithCast() {
        assertScript '''
            def c = (Character) null
        '''
    }

    void testCastObjectToSubclass() {
        assertScript '''
            Object o = null
            ((Integer) o)?.intValue()
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
        ''',
        'Cannot find matching method java.io.Serializable#toInteger()'
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
        ''',
        'Cannot find matching method java.lang.Object#toInteger()'
    }

    void testIfOnly() {
        shouldFailWithMessages '''
            def x = '123'
            def y = 'foo'
            if (y) {
                x = new HashSet()
            }
            x.toInteger()
        ''',
        'Cannot find matching method java.io.Serializable#toInteger()'
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
        ''',
        'Cannot find matching method java.lang.Object#toInteger()'
    }

    void testIfWithCommonInterface() {
        assertScript '''
            interface I {
                def m()
            }
            class A implements I {
                def m() { 'A' }
            }
            class B implements I {
                def m() { 'B' }
            }

            def x = new A()
            def y = true
            if (y) {
                x = new B()
            }
            assert x.m() == 'B'
        '''
    }

    // GROOVY-5502
    void testIfElseWithCommonSuperclass() {
        for (val in ['null', 'new A()', 'new B()', 'new C()'/*TODO:, 'new Object()'*/]) {
            assertScript """
                class A {
                    def m() { 'A' }
                }
                class B extends A {
                }
                class C extends A {
                }

                def var = $val
                if (true) {
                    var = new B()
                } else {
                    var = new C()
                }
                assert var.m() == 'A' // Cannot find matching method Object#m()
            """
        }
    }

    // GROOVY-9786
    void testIfElseIfWithCommonInterface() {
        for (it in ['I', 'def', 'var', 'Object']) {
            assertScript """
                interface I {
                    def m()
                }
                class A implements I {
                    def m() { 'A' }
                }
                class B implements I {
                    def m() { 'B' }
                }

                $it x
                def y = false
                def z = true
                if (y) {
                    x = new A()
                } else if (z) {
                    x = new B()
                }
                assert x.m() == 'B'
            """
        }
    }

    void testForLoopWithAssignment() {
        shouldFailWithMessages '''
            def x = '123'
            for (int i = 0; i < -1; i += 1) {
                x = new HashSet()
            }
            x.toInteger()
        ''',
        'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testWhileLoopWithAssignment() {
        shouldFailWithMessages '''
            def x = '123'
            while (false) {
                x = new HashSet()
            }
            x.toInteger()
        ''',
        'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testTernaryInitWithAssignment() {
        shouldFailWithMessages '''
            def x = '123'
            def y = (false ? (x = new HashSet()) : 42)
            x.toInteger()
        ''',
        'Cannot find matching method java.io.Serializable#toInteger()'
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
        ''',
        'Cannot assign value of type java.math.BigDecimal to variable of type java.math.BigInteger'
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
        ''',
        'Cannot assign value of type java.lang.Integer to variable of type java.lang.Character'
    }

    void testPostfixOnObject() {
        shouldFailWithMessages '''
            Object o = new Object()
            o++
        ''',
        'Cannot find matching method java.lang.Object#next()'
        shouldFailWithMessages '''
            Object o = new Object()
            o--
        ''',
        'Cannot find matching method java.lang.Object#previous()'
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
        ''',
        'Cannot find matching method java.lang.Object#next()'
        shouldFailWithMessages '''
            Object o = new Object()
            --o
        ''',
        'Cannot find matching method java.lang.Object#previous()'
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
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    void testIncompatibleToArray() {
        shouldFailWithMessages '''
            (Set[]) ['a','b','c'].toArray(new String[3])
        ''',
        'Inconvertible types: cannot cast java.lang.String[] to java.util.Set[]'
    }

    // GROOVY-5535, GROOVY-10623
    void testAssignToNullInsideIf() {
        ['Date', 'def', 'var'].each {
            assertScript """
                Date test() {
                    $it x = new Date()
                    if (true) {
                        x = null
                        Date y = x
                    }
                    Date z = x
                    return x
                }
                assert test() == null
            """
        }
    }

    // GROOVY-10294
    void testAssignToNullInsideIf2() {
        assertScript '''
            CharSequence test() {
                def x = 'works'
                if (false) {
                    x = null
                }
                x
            }
            assert test() == 'works'
        '''
    }

    // GROOVY-10308
    void testAssignToNullAfterCall() {
        assertScript '''
            class C<T> {
                T p
            }
            def x = { -> new C<String>() }
            def y = x()
            def z = y.p // false positive: field access error
            y = null
        '''
    }

    // GROOVY-10623
    void testAssignToNullAfterInit() {
        assertScript '''
            class C {
            }
            def x = new C()
            x = null
            C c = x
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
        ''',
        'Cannot assign value of type java.lang.Object to variable of type int[]'
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

    // GROOVY-10953
    void testMultiAssign2() {
        assertScript '''
            def m() {
                def (x, y, z) = 1..4
                assert "$x $y $z" == '1 2 3'
                (x, y, z) = -5..-6
                assert "$x ${y.intValue()} $z" == '-5 -6 null'
                def (a, _, c) = 'a'..'c'
                assert "${a.toUpperCase()} ${c.endsWith('c')}" == 'A true'
            }
            m()
        '''
    }

    // GROOVY-8220
    void testFlowTypingParameterTempTypeAssignmentTracking1() {
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
    }

    // GROOVY-10943
    void testMultiAssignUnderscorePlaceholder1() {
        assertScript '''
            def m() {
                def (x, _, y, _, z) = [1, 2, 3, 4, 5]
                assert "$x $y $z" == '1 3 5'
            }
            m()
        '''
    }

    // GROOVY-10943
    void testMultiAssignUnderscorePlaceholder2() {
        shouldFailWithMessages '''
            def m() {
                def (x, _, y, _, z) = [1, 2, 3, 4, 5]
                assert _
            }

            m()
        ''',
            'The variable [_] is undeclared'
    }

    // GROOVY-10943
    void testClosureUnderscorePlaceholder() {
        shouldFailWithMessages '''
            def m() {
                def g = { String a, _, _ -> a + _ }
            }

            m()
        ''',
            'The variable [_] is undeclared'
    }

    // GROOVY-10943
    void testLambdaUnderscorePlaceholder() {
        shouldFailWithMessages '''
            def m() {
                def h = (String a, _, _) -> a + _
            }

            m()
        ''',
            'The variable [_] is undeclared'
    }

    // GROOVY-8237
    void testFlowTypingParameterTempTypeAssignmentTracking2() {
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

    void testFlowTypingParameterTempTypeAssignmentTracking3() {
        assertScript '''
            class M {
                Map<String, List<Object>> mvm = new HashMap<String, List<Object>>()
                void setProperty(String name, value) {
                    if (value instanceof File) {
                        value = new File(value, 'bar.txt')
                    } else if (value instanceof URL) {
                        value = value.toURI()
                    } else if (value instanceof InputStream) {
                        value = new BufferedInputStream(value)
                    } else if (value instanceof GString) {
                        value = value.toString()
                    }
                    mvm.computeIfAbsent(name, k -> [] as List<Object>).add(value)
                }
            }
            new M().setProperty('foo', 'bar')
        '''
    }

    void testNarrowingConversion() {
        assertScript '''
            interface A {
            }
            interface B extends A {
            }
            class C implements B {
            }
            def m(B b) {
                C c = (C) b
            }
        '''
    }

    void testFinalNarrowingConversion() {
        shouldFailWithMessages '''
            interface I {
            }
            interface B extends I {
            }
            final class C implements I {
            }
            def m(B b) {
                C c = (C) b
            }
        ''',
        'Inconvertible types: cannot cast B to C'
    }

    // GROOVY-10419
    void testElvisAssignmentAndSetter1() {
        assertScript '''
            class C {
                def p
                void setP(p) {
                    this.p = p
                }
            }
            def c = new C()
            c.p ?= 'x'
            assert c.p == 'x'
            c.with {
                p ?= 'y'
            }
            assert c.p == 'x'
        '''
    }

    // GROOVY-10628
    void testElvisAssignmentAndSetter2() {
        assertScript '''
            class C {
                String getFoo() {
                }
                void setFoo(String foo) {
                }
            }
            new C().foo ?= 'bar'
        '''
    }

    void testElvisAssignmentMismatched() {
        shouldFailWithMessages '''
            class C {
                Number foo
            }
            new C().foo ?= 'bar'
        ''',
        'Cannot assign value of type java.io.Serializable to variable of type java.lang.Number'
    }
}
