/*
 * Copyright 2003-2007 the original author or authors.
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
package groovy.util

import static Collections.combinations

/**
 * Tests Collections
 *
 * @author Paul King
 */
public class CollectionsTest extends GroovyTestCase {

    void testCombinations() {
        Set expected = [
                        ['a', 1], ['a', 2], ['a', 3],
                        ['b', 1], ['b', 2], ['b', 3]
                ]
        Collection input = [['a', 'b'], [1, 2, 3]]

        // normal varargs versions should match Object[]
        assert Collections.combinations(['a', 'b'], [1, 2, 3]) as Set == expected
        assert combinations(['a', 'b'], [1, 2, 3]) as Set == expected

        // spread versions should match Object[]
        assert Collections.combinations(*input) as Set == expected
        assert combinations(*input) as Set == expected

        // collection versions should match Collection
        assert Collections.combinations(input) as Set == expected
        assert combinations(input) as Set == expected
    }

    void testTranspose() {
        // varargs versions
        assert Collections.transpose(['a', 'b'], [1, 2, 3]) == [['a', 1], ['b', 2]]
        assert Collections.transpose([1, 2, 3], [4, 5, 6]) == [[1, 4], [2, 5], [3, 6]]
        assert Collections.transpose([1, 2, 3], [4, 5], [9], [6, 7, 8]) == [[1, 4, 9, 6]]

        // collection versions
        assert Collections.transpose([[1, 2, 3]]) == [[1], [2], [3]]
        assert Collections.transpose([]) == []
    }

}