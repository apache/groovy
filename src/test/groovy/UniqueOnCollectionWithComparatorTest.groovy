/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy

/**
 * @author Michael Baehr
 * @author Paul King
 */
class UniqueOnCollectionWithComparatorTest extends GroovyTestCase {

    void testUniqueOnIterator() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator({a, b -> Math.abs(a) <=> Math.abs(b)})
        def it = list.iterator().unique(comparator)
        assert it instanceof Iterator
        def result = it.toList()
        assert result == [-1, 0]
    }

    void testUniqueWithComparatorList() {
        def list = [-1, 0, 1, 1, 0, -1]
        def comparator = new ClosureComparator({a, b -> Math.abs(a) <=> Math.abs(b)})
        assert list.unique(comparator) == [-1, 0]
    }

    void testUniqueWithComparatorSet() {
        def set = [-1, 0, 1] as Set
        def comparator = new ClosureComparator({a, b -> Math.abs(a) <=> Math.abs(b)})
        assert set.unique(comparator).size() == 2
    }
}