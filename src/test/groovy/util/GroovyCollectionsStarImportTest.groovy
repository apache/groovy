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

import static groovy.util.GroovyCollections.*

/**
* Tests GroovyCollections
*/
class GroovyCollectionsStarImportTest extends GroovyTestCase {

    void testCombinations() {
        // use Sets because we don't care about order
        Set expected = [['a', 1], ['a', 2], ['b', 1], ['b', 2]]
        assert combinations(['a', 'b'], [1, 2]) as Set == expected
    }

    void testTranspose() {
        assert transpose(['a', 'b'], [1, 2, 3]) == [['a', 1], ['b', 2]]
    }

    void testMin() {
        assert min('a', 'b') == 'a'
    }

    void testMax() {
        assert max(1, 2, 3) == 3
    }

    void testSum() {
        assert sum(1, 2, 3, 4) == 10
    }

}
