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
package groovy.lang

import junit.framework.TestCase

/**
 * Provides unit tests for the <code>NumberRange</code> class.
 */
public class NumberRangeTest extends TestCase {

    public void testStep() {
        Range n = new NumberRange(1, 3)
        assert n.step(1) == [1, 2, 3]
        assert n.size() == 3

        n = new NumberRange(1, 3, false)
        assert n.step(1) == [1, 2]
        assert n.size() == 2

        n = new NumberRange(1, 10).by(3)
        assert n.step(1) == [1, 4, 7, 10]
        assert n.step(2) == [1, 7]
        assert n.size() == 4

        n = new NumberRange(1, 10, false).by(3)
        assert n.step(1) == [1, 4, 7]
        assert n.stepSize == 3
        assert n.size() == 3

        n = new NumberRange(1, 3G, 0.25)
        assert n.step(1) == [1, 1.25, 1.50, 1.75, 2.00, 2.25, 2.50, 2.75, 3.00]
        assert n.step(2) == [1, 1.50, 2.00, 2.50, 3.00]
        assert n.size() == 9

        n = new NumberRange(0, 1, 0.1, false)
        assert n.step(1) == [0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9]
        assert n.size() == 10
        assert n.stepSize == 0.1

        n = new NumberRange(1L, Integer.MAX_VALUE)
        def other = new NumberRange(1L, Integer.MAX_VALUE)
        assert n.fastEquals(other)
//        assert n.fastHashCode() == other.fastHashCode()

        n = new NumberRange(0, 3)
        other = new NumberRange(0.0, 3.0)
        assert n == other
        assert n.hashCode() != other.hashCode() // not desired but reflects Groovy's extra eagerness for friendly equality
//        assert n.canonicalHashCode() == other.canonicalHashCode()
        assert n.fastEquals(other)

        // integer overflow cases
        assert Integer.MAX_VALUE == new NumberRange(0L, Integer.MAX_VALUE).size()
        assert Integer.MAX_VALUE == new NumberRange(Long.MIN_VALUE, Long.MAX_VALUE).size()
        assert Integer.MAX_VALUE == new NumberRange(new BigInteger("-10"), new BigInteger(Long.toString((long) Integer.MAX_VALUE) + 1L)).size()
    }

}
