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
 * Boolean comparisons with a NaN are false. {@code <=>}, identity, containment
 * and Groovy truth keep their existing results.
 */
final class Groovy3309 {

    @Test
    void testNaNComparisonsDynamic() {
        assertScript '''
            Double nan = Double.NaN
            Double other = Double.valueOf(Double.NaN)
            Float fnan = Float.NaN
            Double zero = 0.0d
            Float fzero = 0.0f
            Integer iz = 0
            Long lz = 0L
            BigDecimal bz = 0.0
            Character ch = (char) 'x'

            def deny = { left, right ->
                assert !(left == right) && left != right
                assert !(left < right) && !(left <= right)
                assert !(left > right) && !(left >= right)
                assert !(right == left) && right != left
                assert !(right < left) && !(right <= left)
                assert !(right > left) && !(right >= left)
            }

            deny(Double.NaN, 0)
            deny(nan, nan)
            deny(nan, other)
            deny(nan, zero)
            deny(nan, fzero)
            deny(nan, iz)
            deny(nan, lz)
            deny(nan, bz)
            deny(nan, fnan)
            deny(fnan, fnan)
            deny(fnan, iz)
            deny(nan, ch)
            deny(nan, 'x')
            deny(nan, "${'x'}")

            assert nan.is(nan) && !nan.is(other)
            assert nan === nan && !(nan === other)
            assert (nan <=> iz) == 1
            assert (iz <=> nan) == -1
            assert (nan <=> other) == 0
            assert (nan <=> nan) == 0
            assert (fnan <=> nan) == 0
            assert (nan <=> null) == 1
            assert (null <=> nan) == -1

            assert !nan && !fnan
            Number asNumber = nan
            assert !asNumber

            assert nan > null && !(null > nan)
            assert null < nan && !(nan < null)
            assert !(nan == null) && !(null == nan)

            def failed = { Closure body ->
                try {
                    body()
                    return false
                } catch (IllegalArgumentException expected) {
                    return true
                }
            }
            assert failed { nan > 'ab' }
            assert failed { nan > "${'xy'}" }
            assert failed { (1.0d) > 'ab' }
            assert !((1.0d) > 'x')

            Double one = 1.0d
            Double two = 2.0d
            Float fone = 1.0f
            Float ftwo = 2.0f
            assert one == one && one == 1.0d && one == 1 && one == two - 1
            assert fone == fone && fone == 1.0f && fone == ftwo - 1
            assert one < two && one <= one && two > one && two >= one && !(two < one) && !(one > two)
            assert one != two && !(one != one)
            assert fone < ftwo && fone <= fone && ftwo > fone && ftwo >= fone && !(ftwo < fone) && !(fone > ftwo)
            assert one > iz && one >= one && one <= one && !(one < iz) && !(iz > one)
            // different classes, so <= and >= use the compareTo fallback for each result
            Integer oneInt = Integer.valueOf(1)
            assert one <= Integer.valueOf(2) && one <= oneInt && !(two <= iz)
            assert two >= iz && one >= oneInt && !(iz >= two)

            Integer n1 = Integer.valueOf(200)
            Integer n2 = Integer.valueOf(200)
            Integer n3 = Integer.valueOf(201)
            Long m1 = Long.valueOf(200)
            Long m2 = Long.valueOf(201)
            BigDecimal d1 = new BigDecimal('1.0')
            BigDecimal d2 = new BigDecimal('1.00')
            BigDecimal d3 = new BigDecimal('2.0')
            BigInteger k1 = new BigInteger('1')
            BigInteger k2 = new BigInteger('2')
            String a = new String('a')
            String b = new String('b')
            def g1 = "${'a'}"
            def g2 = "${'a'}"
            assert n1 == n2 && n1 < n3 && n1 <= n1 && n3 > n1 && n3 >= n1 && !(n3 < n1)
            assert !(n1 > n3) && !(n3 <= n1) && !(n1 >= n3)
            assert m1 == Long.valueOf(200) && m1 < m2 && m1 <= m1 && m2 > m1 && m2 >= m1
            assert !(m2 < m1) && !(m1 > m2) && !(m2 <= m1) && !(m1 >= m2)
            assert d1 == d2 && d1 < d3 && d1 <= d1 && d3 > d1 && d3 >= d1 && !(d3 < d1)
            assert !(d1 == d3) && !(d1 > d3) && !(d3 <= d1) && !(d1 >= d3)
            assert k1 == new BigInteger('1') && k1 < k2 && k1 <= k1 && k2 > k1 && k2 >= k1
            assert !(k1 == k2) && !(k2 < k1) && !(k1 > k2) && !(k2 <= k1) && !(k1 >= k2)
            assert a == new String('a') && a < b && a <= a && b > a && b >= a
            assert !(two <= one) && !(one >= two) && !(ftwo <= fone) && !(fone >= ftwo)
            assert g1 == g2 && g1 <= g1 && !(g1 > g2)
            assert 1 == 1 && 1 < 2 && 2 > 1 && 1 <= 1 && 2 >= 1
            assert 0.0 == 0
            assert Double.valueOf(-0.0d) != Double.valueOf(0.0d)
            assert Double.POSITIVE_INFINITY > one && one < Double.POSITIVE_INFINITY

            double primitive = Double.NaN
            float primitiveFloat = Float.NaN
            assert !(primitive > 0) && !(primitive >= 0) && !(primitive < 0) && !(primitive <= 0)
            assert !(primitive == primitive) && primitive != primitive
            assert !(primitiveFloat > 0f) && !(primitiveFloat == primitiveFloat) && primitiveFloat != primitiveFloat
            assert (primitive <=> 0) == 1

            double captured = Double.NaN
            int capturedZero = 0
            assert !({ captured > capturedZero }())
            assert !({ captured == captured }())
            assert ({ captured != captured }())

            assert nan in [other]
            assert !(iz in [nan])
            boolean matched = false
            switch (nan) {
                case other:
                    matched = true
            }
            assert matched
        '''
    }

    @Test
    void testNaNComparisonsCheckedAndStatic() {
        for (mode in ['@groovy.transform.TypeChecked', '@groovy.transform.CompileStatic']) {
            assertScript """
                $mode
                void test() {
                    Double nan = Double.NaN
                    Double other = Double.valueOf(Double.NaN)
                    Float fnan = Float.NaN
                    Double zero = 0.0d
                    Integer iz = 0
                    Long lz = 0L
                    BigDecimal bz = 0.0

                    assert !(nan == 0) && !(nan < 0) && !(nan > 0) && nan != nan
                    assert !(nan == iz) && !(nan < iz) && !(nan <= iz) && !(nan > iz) && !(nan >= iz)
                    assert !(iz == nan) && !(iz < nan) && !(iz <= nan) && !(iz > nan) && !(iz >= nan)
                    assert !(nan == other) && nan != other
                    assert !(nan == zero) && !(nan > zero) && !(zero < nan) && !(nan >= zero) && !(zero <= nan)
                    assert !(nan == lz) && !(nan > lz) && !(lz <= nan)
                    assert !(nan == bz) && !(nan > bz) && !(bz < nan)
                    assert !(fnan == fnan) && fnan != fnan
                    assert !(fnan > iz) && !(iz < fnan) && !(fnan >= nan) && !(nan <= fnan)
                    assert nan.is(nan) && !nan.is(other)
                    assert nan === nan && !(nan === other)
                    assert (nan <=> zero) == 1
                    assert (zero <=> nan) == -1
                    assert (nan <=> other) == 0
                    assert (nan <=> nan) == 0
                    assert !nan && !fnan

                    double primitive = Double.NaN
                    double primitiveZero = 0.0d
                    float primitiveFloat = Float.NaN
                    assert !(primitive > 0) && !(primitive >= 0) && !(primitive < 0) && !(primitive <= 0)
                    assert !(primitive == primitive) && primitive != primitive
                    assert !(primitiveFloat > 0f) && !(primitiveFloat == primitiveFloat)
                    assert (primitive <=> primitiveZero) == 1
                    assert (primitive <=> primitive) == 0

                    Double one = 1.0d
                    assert one == 1 && one == one && one > iz && one >= one && !(one < iz)
                    assert Double.POSITIVE_INFINITY > one
                }
                test()
            """
        }
    }
}
