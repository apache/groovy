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
package groovy.util

import groovy.test.GroovyTestCase
import org.codehaus.groovy.util.ListBufferedIterator;
import org.codehaus.groovy.util.IteratorBufferedIterator;

/**
 * Test class for BufferedIterators.
 */
class BufferedIteratorTest extends GroovyTestCase {

    void testHeadOnEmptyShouldFail() {
        def bufferedIterator1 = new IteratorBufferedIterator([].iterator())
        def bufferedIterator2 = new ListBufferedIterator([])

        shouldFail(NoSuchElementException) {
            bufferedIterator1.head()
        }
        shouldFail(NoSuchElementException) {
            bufferedIterator2.head()
        }
    }

    void testHeadShouldntConsumeFirstElement() {
        def bufferedIterator1 = new IteratorBufferedIterator([1,2,3,4].iterator())
        def bufferedIterator2 = new ListBufferedIterator([1,2,3,4])

        assert bufferedIterator1.head() == 1
        assert bufferedIterator1.toList() == [1,2,3,4]
        assert bufferedIterator2.head() == 1
        assert bufferedIterator2.toList() == [1,2,3,4]
    }

    void testHeadShouldntConsumeMiddleElement() {
        def bufferedIterator1 = new IteratorBufferedIterator([1,2,3,4].iterator())
        def bufferedIterator2 = new ListBufferedIterator([1,2,3,4])

        assert bufferedIterator1.next() == 1
        assert bufferedIterator1.head() == 2
        assert bufferedIterator1.toList() == [2,3,4]
        assert bufferedIterator2.next() == 1
        assert bufferedIterator2.head() == 2
        assert bufferedIterator2.toList() == [2,3,4]
    }

    void testHeadShouldntConsumeLastElement() {
        def bufferedIterator1 = new IteratorBufferedIterator([1,2,3,4].iterator())
        def bufferedIterator2 = new ListBufferedIterator([1,2,3,4])

        assert bufferedIterator1.next() == 1
        assert bufferedIterator1.next() == 2
        assert bufferedIterator1.next() == 3
        assert bufferedIterator1.head() == 4
        assert bufferedIterator1.toList() == [4]
        assert bufferedIterator2.next() == 1
        assert bufferedIterator2.next() == 2
        assert bufferedIterator2.next() == 3
        assert bufferedIterator2.head() == 4
        assert bufferedIterator2.toList() == [4]
    }

    void testHeadTwiceShouldReturnSameElement() {
        def bufferedIterator1 = new IteratorBufferedIterator([1,2,3,4].iterator())
        def bufferedIterator2 = new ListBufferedIterator([1,2,3,4])

        assert bufferedIterator1.next() == 1
        assert bufferedIterator1.head() == 2
        assert bufferedIterator1.head() == 2
        assert bufferedIterator1.toList() == [2,3,4]
        assert bufferedIterator2.next() == 1
        assert bufferedIterator2.head() == 2
        assert bufferedIterator2.head() == 2
        assert bufferedIterator2.toList() == [2,3,4]
    }

    void testHeadShouldWorkMultipleTimesOnSameIterator() {
        def bufferedIterator1 = new IteratorBufferedIterator([1,2,3,4].iterator())
        def bufferedIterator2 = new ListBufferedIterator([1,2,3,4])

        assert bufferedIterator1.next() == 1
        assert bufferedIterator1.head() == 2
        assert bufferedIterator1.next() == 2
        assert bufferedIterator1.head() == 3
        assert bufferedIterator1.toList() == [3,4]
        assert bufferedIterator2.next() == 1
        assert bufferedIterator2.head() == 2
        assert bufferedIterator2.next() == 2
        assert bufferedIterator2.head() == 3
        assert bufferedIterator2.toList() == [3,4]
    }

    void testIteratorBufferedIteratorSucceedIfHeadNotCalled() {
        def list = [1,2,3,4]
        def bufferedIterator = new IteratorBufferedIterator(list.iterator())

        bufferedIterator.next()
        bufferedIterator.remove()

        assert list == [2,3,4]
    }

    void testIteratorBufferedIteratorRemoveShouldFailIfHeadCalled() {
        def list = [1,2,3,4]
        def bufferedIterator = new IteratorBufferedIterator(list.iterator())

        shouldFail(IllegalStateException) {
            bufferedIterator.next()
            bufferedIterator.head()
            bufferedIterator.remove()
        }
    }

    void testIteratorBufferedIteratorRemoveShouldSucceedIfNextCalledAfterHead() {
        def list = [1,2,3,4]
        def bufferedIterator = new IteratorBufferedIterator(list.iterator())

        bufferedIterator.next()
        bufferedIterator.head()
        bufferedIterator.next()
        bufferedIterator.remove()

        assert list == [1,3,4]
    }

    void testListBufferedIteratorRemoveShouldRemoveLastReturnedByNext() {
        def list = [1,2,3,4]
        def bufferedIterator = new ListBufferedIterator(list)

        assert bufferedIterator.next() == 1
        assert bufferedIterator.head() == 2
        assert bufferedIterator.next() == 2
        assert bufferedIterator.head() == 3

        bufferedIterator.remove()

        assert list == [1,3,4]
    }

}
