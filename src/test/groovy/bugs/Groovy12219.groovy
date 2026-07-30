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

import static groovy.test.GroovyAssert.shouldFail

/**
 * The lazy iterators produced by {@code toUnique()} and {@code dropRight()} read ahead
 * from their delegate before {@code next()} returns, so delegating {@code remove()} to the
 * underlying iterator removed the wrong (already-advanced-past) element. They now make
 * {@code remove()} throw {@link UnsupportedOperationException}, matching the other lazy
 * iterators in DefaultGroovyMethods.
 */
final class Groovy12219 {

    @Test
    void testToUniqueIteratorRemoveIsUnsupported() {
        def source = new LinkedList([1, 1, 2, 3])
        def iter = source.iterator().toUnique()
        assert iter.next() == 1
        shouldFail(UnsupportedOperationException) { iter.remove() }
        assert source == [1, 1, 2, 3] // nothing (least of all the wrong element) removed
        // iteration values remain correct
        assert new LinkedList([1, 1, 2, 3, 3]).iterator().toUnique().toList() == [1, 2, 3]
    }

    @Test
    void testDropRightIteratorRemoveIsUnsupported() {
        def source = new LinkedList([1, 2, 3, 4, 5])
        def iter = source.iterator().dropRight(2)
        assert iter.next() == 1
        shouldFail(UnsupportedOperationException) { iter.remove() }
        assert source == [1, 2, 3, 4, 5] // nothing removed
        // iteration values remain correct
        assert new LinkedList([1, 2, 3, 4, 5]).iterator().dropRight(2).toList() == [1, 2, 3]
    }
}
