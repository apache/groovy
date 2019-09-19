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
package org.codehaus.groovy.transform

import groovy.test.GroovyShellTestCase

class ReadWriteLockTransformTest extends GroovyShellTestCase {
    void testSingleton() {
        assertScript """
            import groovy.transform.*

            @CompileStatic
            class Counters {
                public final Map<String,Integer> map = [:].withDefault { 0 }

                @WithReadLock
                int get(String id) {
                    map.get(id)
                }

                @WithWriteLock
                void add(String id, int num) {
                    Thread.sleep(100) // emulate long computation
                    map.put(id, map.get(id)+num)
                }
            }

            def counters = new Counters()
            assert counters.get('a') == 0
            assert counters.get('b') == 0

            10.times { cpt ->
                Thread.start { counters.add('a', 1) }
                def t = Thread.start {
                    Thread.sleep(20)
                    assert counters.get('a') == cpt+1
                }
                t.join(250)
            }
        """
    }
}
