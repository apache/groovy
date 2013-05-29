/*
 * Copyright 2003-2010 the original author or authors.
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
package groovy.bugs

import gls.CompilableTestSupport

class Groovy4418Bug extends CompilableTestSupport {
    void testStaticFieldAccess() {
        assertScript '''
            class Base {
                static String field = 'foo'
            }
            class Subclass extends Base {
                static method() {
                    field
                }
            }
            assert new Subclass().method() == 'foo'
        '''
    }

    // additional test for GROOVY-6183
    void testStaticAttributeAccess() {
        assertScript '''
        class A {
            static protected int x
            public static void reset() { this.@x = 2 }
        }
        assert A.x == 0
        assert A.@x == 0
        A.reset()
        assert A.x == 2
        assert A.@x == 2
        '''
    }
}
