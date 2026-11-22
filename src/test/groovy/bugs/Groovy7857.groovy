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
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-7857: a two-dimensional array of a generic collection type
 * was mis-parsed so that {@code new List<>[7][12]} produced an
 * ArrayList instead of an array. Diamond generic array creation is
 * now a compile error; raw {@code new List[7][12]} yields a real array.
 */
final class Groovy7857 {

    @Test
    void testTwoDimensionalArrayOfListIsArray() {
        assertScript '''
            List[][] grid1 = new List[7][12]
            assert grid1.class.name == '[[Ljava.util.List;'
            assert grid1.class.array
            assert !(grid1 instanceof List)

            List<String>[][] grid2 = new List[7][12]
            assert grid2.class.name == '[[Ljava.util.List;'
            assert grid2.class.array
            assert !(grid2 instanceof List)

            List<Map<String,String>>[][] grid3 = new List[7][12]
            assert grid3.class.name == '[[Ljava.util.List;'
            assert grid3.class.array
            assert !(grid3 instanceof List)
            assert !(grid3 instanceof ArrayList)
        '''
    }

    @Test
    void testDiamondGenericArrayCreationIsCompileError() {
        def err2 = shouldFail '''
            List<String>[][] grid2 = new List<>[7][12]
        '''
        assert err2.message.contains('generic array creation')

        def err3 = shouldFail '''
            List<Map<String,String>>[][] grid3 = new List<>[7][12]
        '''
        assert err3.message.contains('generic array creation')
    }
}
