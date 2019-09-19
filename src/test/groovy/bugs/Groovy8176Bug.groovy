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
package groovy.bugs

import groovy.test.GroovyTestCase

class Groovy8176Bug extends GroovyTestCase {
    void testTernaryWithTap() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            static <M extends Map> M merge(M to, Map from) {
                !from ? to : to.tap {
                    one = from['one']
                    two = from['two']
                }
            }

            def orig = [:]
            def result = merge(orig, [one: 1, two: 2.0])
            assert result == [one: 1, two: 2.0]
            assert result.is(orig)
        '''
    }
}
