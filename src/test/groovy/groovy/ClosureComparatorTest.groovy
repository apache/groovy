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
package groovy

import groovy.test.GroovyTestCase

/**
 * Tests for ClosureComparator
 */
class ClosureComparatorTest extends GroovyTestCase {

    void testClosureComparatorForGroovyObjects() {

        def comparator = new ClosureComparator({ one, another ->
            one.greaterThan(another)
        })

        def one = new ComparableFoo(5)
        def another = new ComparableFoo(-5)

        assertEquals(10, comparator.compare(one, another))
        assertEquals(0, comparator.compare(one, one))
        assertEquals(-10, comparator.compare(another, one))

    }

    void testClosureComparatorForNumericTypes() {

        def comparator = new ClosureComparator({ one, another ->
            one - another
        })

        assertEquals(1, comparator.compare(Integer.MAX_VALUE, Integer.MAX_VALUE - 1))
        assertEquals(0, comparator.compare(Double.MIN_VALUE, Double.MIN_VALUE))
        assertEquals(-1, comparator.compare(Long.MIN_VALUE, Long.MIN_VALUE + 1))
    }

}

class ComparableFoo {
    long value

    ComparableFoo(long theValue) {
        this.value = theValue
    }

    def greaterThan(anotherFoo) {
        return (this.value - anotherFoo.value)
    }
}
