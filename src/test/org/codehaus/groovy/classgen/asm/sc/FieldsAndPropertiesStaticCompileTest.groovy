/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.FieldsAndPropertiesSTCTest

@Mixin(StaticCompilationTestSupport)
class FieldsAndPropertiesStaticCompileTest extends FieldsAndPropertiesSTCTest {
    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }
    
    void testMapGetAt() {
        assertScript '''
            Map map = [a: 1, b:2]
            String key = 'b'
            assert map['a'] == 1
            assert map[key] == 2
        '''
    }

    void testGetAtFromStaticMap() {
        assertScript '''
            class Foo {
                public static Map CLASSES = [:]
            }
            String foo = 'key'
            Foo.CLASSES[foo]
        '''
    }

    // GROOVY-5561
    void testShouldNotThrowAccessForbidden() {
        assertScript '''
        class Test {
            def foo() {
                def bar = createBar()
                bar.foo
            }

            Bar createBar() { new Bar() }
        }
        class Bar {
            List<String> foo = ['1','2']
        }
        assert new Test().foo() == ['1','2']
        '''
    }

}
