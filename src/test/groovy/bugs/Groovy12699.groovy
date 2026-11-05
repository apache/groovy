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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * {@code ==} on a {@code Number} that is not {@code Comparable} compares
 * numerically, as {@code <=>} and {@code <} do.
 */
final class Groovy12699 {

    private static final String FRAC = '''
        class Frac extends Number { // a Number, but not Comparable
            final BigInteger n, d
            Frac(long n, long d) { this.n = n; this.d = d }
            int intValue()       { (n / d) as int }
            long longValue()     { (n / d) as long }
            float floatValue()   { doubleValue() as float }
            double doubleValue() { n.doubleValue() / d.doubleValue() }
            String toString()    { "$n/$d" }
        }
    '''

    @Test
    void testNonComparableNumberEquality() {
        assertScript FRAC + '''
            def f = new Frac(1, 4)
            assert (f <=> 0.25) == 0
            assert f == 0.25
            assert f == 0.25d
            assert f == 0.25f
            assert 0.25 == f
            assert f != 0.5
            assert !(f == 1)
            assert new Frac(4, 1) == 4
            assert new Frac(4, 1) == 4L
            assert new Frac(4, 1) == 4G
            assert f == new Frac(2, 8)
            assert f != new Frac(1, 2)
        '''
    }

    @Test
    void testNonComparableNumberAndNonNumber() {
        assertScript FRAC + '''
            def f = new Frac(1, 4)
            assert f != 'hello'
            assert f != [0.25]
            assert f != null
            assert null != f
            assert f == f
        '''
    }

    @Test
    void testNonComparableNumberEqualityCompileStatic() {
        assertScript FRAC + '''
            @groovy.transform.CompileStatic
            class CS {
                static boolean eq(Number f)  { f == 0.25G }
                static boolean eqD(Number f) { f == 0.25d }
                static int cmp(Frac f)       { f <=> 0.25G }
                static boolean rev(Frac f)   { 0.25G == f }
                static boolean ne(Number f)  { f != 0.25G }
            }
            def f = new Frac(1, 4)
            assert CS.eq(f)
            assert CS.eqD(f)
            assert CS.cmp(f) == 0
            assert CS.rev(f)
            assert !CS.ne(f)
        '''
    }
}
