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
package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy6240Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testGroovyAllowsIteratingOnMapDirectlyWithForEachLoop() {
        assertScript '''
            Map<String, Integer> map = [foo: 123, bar: 456]
            for (entry in map) {
                assert entry.key.reverse() in ['oof','rab']
                assert entry.value * 2 in [246, 912]
            }
        '''
    }

    void testGroovyAllowsIteratingOnMapDirectlyWithForEachLoopCustomMapType() {
        assertScript '''
            class MyMap extends LinkedHashMap<String,Integer>{}
            def map = new MyMap([foo: 123, bar: 456])
            for (entry in map) {
                assert entry.key.reverse() in ['oof','rab']
                assert entry.value * 2 in [246, 912]
            }
        '''
    }

    // GROOVY-6123
    void testGroovyAllowsIteratingOnEnumerationDirectlyWithForEachLoop() {
        assertScript '''
            Vector<String> v = new Vector<>()
            v.add('ooo')
            def en = v.elements()
            for (e in en) {
                assert e.toUpperCase() == 'OOO'
            }
            v.add('groovy')
            en = v.elements()
            for (e in en) {
                assert e.toUpperCase() == 'OOO'
                break
            }

            en = v.elements()
            for (e in en) {
                assert e.toUpperCase() in ['OOO','GROOVY']
                if (e=='ooo') continue
            }
        '''
    }
}
