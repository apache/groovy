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

class Groovy7970Bug extends GroovyTestCase {

    private static final String getScriptAIC(String visibility, boolean cs) {
        """
        ${ cs ? '@groovy.transform.CompileStatic' : ''}
        class Bar {
            $visibility String renderTemplate(String arg) { "dummy\$arg" }
            def foo() {
                new Object() {
                    String bar() {
                       'A'.with{ renderTemplate(it) }
                    }
                }
            }
        }
        assert new Bar().foo().bar() == 'dummyA'
        """
    }

    private static final String getScriptNestedAIC(String visibility, boolean cs) {
        """
        ${ cs ? '@groovy.transform.CompileStatic' : ''}
        class Bar {
            $visibility String renderTemplate(String arg) { "dummy\$arg" }
            def foo() {
                new Object() {
                    def bar() {
                       new Object() { String xxx() { 'B'.with{ renderTemplate(it) } }}
                    }
                }
            }
        }
        assert new Bar().foo().bar().xxx() == 'dummyB'
        """
    }

    private static final String getScriptInner(String visibility, boolean cs) {
        """
        ${ cs ? '@groovy.transform.CompileStatic' : ''}
        class Bar {
            $visibility String renderTemplate(String arg) { "dummy\$arg" }
            class Inner {
                String baz() {
                    'C'.with{ renderTemplate(it) }
                }
            }
        }
        def b = new Bar()
        assert new Bar.Inner(b).baz() == 'dummyC'
        """
    }

    private static final String getScriptNestedInner(String visibility, boolean cs) {
        """
        ${ cs ? '@groovy.transform.CompileStatic' : ''}
        class Bar {
            $visibility String renderTemplate(String arg) { "dummy\$arg" }
            class Inner {
                class InnerInner {
                    String xxx() { 'D'.with{ renderTemplate(it) } }
                }
            }
        }
        def b = new Bar()
        def bi = new Bar.Inner(b)
        assert new Bar.Inner.InnerInner(bi).xxx() == 'dummyD'
        """
    }

    void testMethodAccessFromClosureWithinInnerClass() {
        assertScript getScriptInner('public', false)
        assertScript getScriptInner('protected', false)
        assertScript getScriptInner('private', false)
        assertScript getScriptNestedInner('public', false)
        assertScript getScriptNestedInner('protected', false)
        assertScript getScriptNestedInner('private', false)
    }

    void testMethodAccessFromClosureWithinInnerClass_CS() {
        assertScript getScriptInner('public', true)
        assertScript getScriptInner('protected', true)
        assertScript getScriptInner('private', true)
        assertScript getScriptNestedInner('public', true)
        assertScript getScriptNestedInner('protected', true)
        assertScript getScriptNestedInner('private', true)
    }

    void testMethodAccessFromClosureWithinAIC() {
        assertScript getScriptAIC('public', false)
        assertScript getScriptAIC('protected', false)
        assertScript getScriptAIC('private', false)
        assertScript getScriptNestedAIC('public', false)
        assertScript getScriptNestedAIC('protected', false)
        assertScript getScriptNestedAIC('private', false)
    }

    void testMethodAccessFromClosureWithinAIC_CS() {
        assertScript getScriptAIC('public', true)
        assertScript getScriptAIC('protected', true)
        assertScript getScriptAIC('private', true)
        assertScript getScriptNestedAIC('public', true)
        assertScript getScriptNestedAIC('protected', true)
        assertScript getScriptNestedAIC('private', true)
    }
}
