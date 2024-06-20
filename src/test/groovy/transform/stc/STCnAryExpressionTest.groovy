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
 * Unit tests for static type checking : unary and binary expressions.
 */
class STCnAryExpressionTest extends StaticTypeCheckingTestCase {

    void testBinaryStringPlus() {
        assertScript '''
            String str = 'a'
            String str2 = 'b'
            str+str2
        '''
    }

    void testBinaryStringPlusInt() {
        assertScript '''
            String str = 'a'
            int num = 2
            str+num
        '''
    }

    void testBinaryObjectPlusInt() {
        shouldFailWithMessages '''
            def obj = new Object()
            int num = 2
            obj+num
        ''',
        'Cannot find matching method java.lang.Object#plus(int)'
    }

    void testBinaryIntPlusObject() {
        shouldFailWithMessages '''
            def obj = new Object()
            int num = 2
            num+obj
        ''',
        'Cannot find matching method java.lang.Integer#plus(java.lang.Object)'
    }

    // GROOVY-10818
    void testBinaryTimeDurationPlus() {
        assertScript '''import groovy.time.*
            TimeDuration td1 = new TimeDuration(0, 1, 20, 43, 0)
            TimeDuration td2 = new TimeDuration(0, 0, 20, 17, 0)
            Duration d = td1 + td2
        '''
    }

    void testPrimitiveComparison() {
        assertScript '''
            1<2
        '''
        assertScript '''
            1>2
        '''
        assertScript '''
            1<=2
        '''
        assertScript '''
            1>=2
        '''
        assertScript '''
            1==2
        '''
    }

    void testBoxedTypeComparison() {
        assertScript '''
            1<new Integer(2)
        '''
        assertScript '''
            1>new Integer(2)
        '''
        assertScript '''
            1<=new Integer(2)
        '''
        assertScript '''
            1>=new Integer(2)
        '''
        assertScript '''
            1==new Integer(2)
        '''
    }

    void testOtherTypeComparison() {
        shouldFailWithMessages '''
            def that = new Object()
            def test = (that >= this)
            test.booleanValue() // no error
        ''',
        'Cannot find matching method java.lang.Object#compareTo'
    }

    void testShiftOnPrimitives() {
        assertScript '''
            1 << 8
        '''
        assertScript '''
            1 >> 8
        '''
        assertScript '''
            1 >>> 8
        '''
    }

    void testShiftOnPrimitivesThroughVariables() {
        assertScript '''
            int x = 1
            int y = 8
            x << y
        '''
        assertScript '''
            int x = 1
            int y = 8
            x >> y
        '''
        assertScript '''
            int x = 1
            int y = 8
            x >>> y
        '''
    }

    // GROOVY-5644
    void testSpaceshipOperatorNoAmbiguousError() {
        assertScript '''
            Integer x = 3
            Integer y = 4
            assert (x <=> y) == -1
        '''
    }

    // GROOVY-6137, GROOVY-7473, GROOVY-10909
    void testInOperatorImplicitNullSafetyChecks() {
        assertScript '''
            class C {
                int i = 0
                int getA() { i++ }
                boolean isCase(obj) { true }
                boolean isNotCase(obj) { false }
            }

            new C().with { c ->
                assert !(a !in c)
                assert i == 1
                assert a in c
                assert i == 2
            }
        '''
    }

    // GROOVY-10915
    void testInNotInAndUnaryNotOperatorConsistent() {
        assertScript '''
            class C {
                boolean isCase(obj) { true }
            }

            def c = new C()
            assert 0 in c
            assert !!(0 in c)
            assert !(0 !in c)
            assert  (0 !in c) == false
        '''
    }

    // GROOVY-7473
    void testInOperatorShouldEvaluateOperandsOnce() {
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 0
            int getA() { i++ }
            int getB() { j++ }

            assert a in b
            assert i == 1
            assert j == 1
        '''
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 0
            def getA() { i++; null }
            def getB() { j++ }

            assert !(a in b)
            assert i == 1
            assert j == 1
        '''
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 0
            def getA() { i++ }
            def getB() { j++; null }

            assert !(a in b)
            assert i == 1
            assert j == 1
        '''
    }

    // GROOVY-6137, GROOVY-7473, GROOVY-10383
    void testInOperatorShouldEvaluateOperandsOnce2() {
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 0
            int getA() { i++ }
            int getB() { j++ }

            assert !(a !in b)
            assert i == 1
            assert j == 1
        '''
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 0
            def getA() { i++; null }
            def getB() { j++ }

            assert a !in b
            assert i == 1
            assert j == 1
        '''
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 0
            def getA() { i++ }
            def getB() { j++; null }

            assert a !in b
            assert i == 1
            assert j == 1
        '''
    }

    // GROOVY-10394
    void testUfoOperatorShouldEvaluateOperandsOnce() {
        assertScript '''
            import groovy.transform.Field

            @Field int i = 0
            @Field int j = 1
            Integer getA() { i++ }
            Integer getB() { j++ }

            assert (a <=> b) < 0
            assert i == 1
            assert j == 2
        '''
    }

    void testComparisonOperatorCheckWithIncompatibleTypesOkIfComparableNotImplemented() {
        shouldFailWithMessages '''
            [] < 1
        ''',
        'Cannot find matching method java.util.ArrayList#compareTo(int)'
    }

    void testComparisonOperatorCheckWithIncompatibleTypesFailsIfComparableImplemented() {
        shouldFailWithMessages '''
           'abc' < 1
        ''',
        'Cannot call java.lang.String#compareTo(java.lang.String) with arguments [int]'
    }

    void testCompareToCallCheckWithIncompatibleTypesAlsoFailsIfComparableImplemented() {
        shouldFailWithMessages '''
           'abc'.compareTo(1)
        ''',
        'Cannot call java.lang.String#compareTo(java.lang.String) with arguments [int]'
    }
}
