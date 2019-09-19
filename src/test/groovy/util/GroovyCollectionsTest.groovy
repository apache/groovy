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

import static GroovyCollections.min
import static GroovyCollections.max
import static GroovyCollections.combinations
import static GroovyCollections.transpose
import static GroovyCollections.sum

/**
* Tests GroovyCollections
*/
class GroovyCollectionsTest extends GroovyTestCase {

    void testCombinations() {
        // use Sets because we don't care about order
        Set expected = [
                        ['a', 1], ['a', 2], ['a', 3],
                        ['b', 1], ['b', 2], ['b', 3]
                ]
        Collection input = [['a', 'b'], [1, 2, 3]]

        // normal varargs versions should match Object[]
        assert GroovyCollections.combinations(['a', 'b'], [1, 2, 3]) as Set == expected
        assert combinations(['a', 'b'], [1, 2, 3]) as Set == expected

        // spread versions should match Object[]
        assert GroovyCollections.combinations(*input) as Set == expected
        assert combinations(*input) as Set == expected

        // collection versions should match Collection
        assert GroovyCollections.combinations(input) as Set == expected
        assert combinations(input) as Set == expected

        // an empty iterable should result in no combination
        assert combinations([[]] + input).isEmpty()
        assert combinations(input + [[]]).isEmpty()
        assert combinations(input + [[]] + input).isEmpty()
    }

    void testTranspose() {
        // normal varargs versions should match Object[]
        assert GroovyCollections.transpose(['a', 'b'], [1, 2, 3]) == [['a', 1], ['b', 2]]
        assert transpose(['a', 'b'], [1, 2, 3]) == [['a', 1], ['b', 2]]
        assert GroovyCollections.transpose([1, 2, 3], [4, 5, 6]) == [[1, 4], [2, 5], [3, 6]]
        assert transpose([1, 2, 3], [4, 5, 6]) == [[1, 4], [2, 5], [3, 6]]
        assert GroovyCollections.transpose([1, 2, 3], [4, 5], [9], [6, 7, 8]) == [[1, 4, 9, 6]]
        assert transpose([1, 2, 3], [4, 5], [9], [6, 7, 8]) == [[1, 4, 9, 6]]

        // collection versions
        assert GroovyCollections.transpose([[1, 2, 3]]) == [[1], [2], [3]]
        assert transpose([[1, 2, 3]]) == [[1], [2], [3]]
        assert GroovyCollections.transpose([]) == []
        assert transpose([]) == []
    }

    void testMin() {
        // normal varargs versions should match Object[]
        assert GroovyCollections.min('a', 'b') == 'a'
        assert min('a', 'b') == 'a'
        assert GroovyCollections.min(1, 2, 3) == 1
        assert min(1, 2, 3) == 1

        // collection versions
        assert GroovyCollections.min(['a', 'b']) == 'a'
        assert min(['a', 'b']) == 'a'
        assert GroovyCollections.min([1, 2, 3]) == 1
        assert min([1, 2, 3]) == 1
    }

    void testMax() {
        // normal varargs versions should match Object[]
        assert GroovyCollections.max('a', 'b') == 'b'
        assert max('a', 'b') == 'b'
        assert GroovyCollections.max(1, 2, 3) == 3
        assert max(1, 2, 3) == 3

        // collection versions
        assert GroovyCollections.max(['a', 'b']) == 'b'
        assert max(['a', 'b']) == 'b'
        assert GroovyCollections.max([1, 2, 3]) == 3
        assert max([1, 2, 3]) == 3
    }

    void testSum() {
        // normal varargs versions should match Object[]
        assert GroovyCollections.sum('a', 'b') == 'ab'
        assert sum('a', 'b') == 'ab'
        assert GroovyCollections.sum(1, 2, 3) == 6
        assert sum(1, 2, 3) == 6

        // collection versions
        assert GroovyCollections.sum(['a', 'b']) == 'ab'
        assert sum(['a', 'b']) == 'ab'
        assert GroovyCollections.sum([1, 2, 3]) == 6
        assert sum([1, 2, 3]) == 6
    }

    // GROOVY-7267
    void testHashCodeCollisionInMinus() {
        assert ([[1:2],[2:3]]-[["b":"a"]]) == [[1:2],[2:3]]
    }
}
