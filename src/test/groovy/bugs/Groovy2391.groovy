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

final class Groovy2391 {

    @Test
    void testOverrideExtensionMethod() {
        assertScript '''
            ArrayList.metaClass.asType = { Class c ->
                if (c.isInstance(delegate[1])) {
                    return delegate[1]
                }
                assert false
            }
            ArrayList.metaClass.initialize()
            try {
                String result = [1,"Two",3] as String
                assert result == "Two"
            } finally {
                GroovySystem.metaClassRegistry.removeMetaClass(ArrayList)
            }
        '''
    }

    @Test // GROOVY-3493
    void testOverrideOverriddenMethod() {
        assertScript '''
            interface I {
                def m()
            }
            class C implements I {
                def m() { 'C' }
            }

            x = new C()
            assert x.m() == 'C'
            x.metaClass.m = { -> 'Override' }
            assert x.m() == 'Override'
        '''
    }
}
