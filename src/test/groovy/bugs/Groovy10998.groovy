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

import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy10998 {

    @Test
    void testTypeParamCycle1() {
        def err = shouldFail '''
            def <T extends T> void test() {}
        '''
        assert err =~ /Cycle detected: the type T cannot extend.implement itself or one of its own member types/
    }

    @Test
    void testTypeParamCycle2() {
        def err = shouldFail '''
            def <T extends U, U extends T> void test() {}
        '''
        assert err =~ /Cycle detected: the type T cannot extend.implement itself or one of its own member types/
    }

    @Test
    void testTypeParamNoCycle() {
        assertScript '''
            def <T, U extends T> String test(T t, U u) {
                "$t,$u"
            }
            assert this.<Number, Integer>test(1, 2) == "1,2"
        '''
    }
}
