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
 * GROOVY-11042: STC must select {@code TreeMap#subMap(K, K)} rather than
 * DGM {@code Map#subMap(K[])} when the key type is {@code String[]}
 * (historically the extension method won on distance and the call was
 * rejected).
 */
final class Groovy11042 {

    @Test
    void testTreeMapSubMapWithArrayKeysSelectsTheJdkMethod() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Main {
                static SortedMap<String[], Number> test() {
                    String[] p1 = ['a'] as String[]
                    String[] p2 = ['z'] as String[]
                    def x = new TreeMap<String[], Number>({ String[] a, String[] b -> a[0] <=> b[0] } as Comparator<String[]>)
                    x.put(p1, 1)
                    x.put(p2, 2)
                    x.subMap(p1, p2)
                }
            }
            def r = Main.test()
            assert r.size() == 1
            assert r.values().contains(1)
        '''
    }
}
