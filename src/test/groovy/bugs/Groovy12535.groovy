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
 * Property-style writes into a {@code Map<String,String>} must store a
 * {@code String} when the right-hand side is a {@code GString}, the same
 * conversion {@code String s = gString} already performs. Subscript {@code putAt}
 * and {@code Map.put} stay rejected.
 */
final class Groovy12535 {

    @Test
    void propertyAssignmentConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                m.k = "$n"
                ((Map) m).values()*.getClass()*.simpleName
            }
            assert go() == ['String']
        '''
    }

    @Test
    void propertyAssignmentWithClosureConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                m.with { k = "$n" }
                ((Map) m).values()*.getClass()*.simpleName
            }
            assert go() == ['String']
        '''
    }

    @Test
    void propertyAssignmentOnItConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                m.with { it.k = "$n" }
                ((Map) m).get('k')
            }
            def value = go()
            assert value instanceof String
            assert value == 'ab'
        '''
    }

    @Test
    void typeCheckedPropertyAssignmentConvertsGString() {
        assertScript '''
            @groovy.transform.TypeChecked
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                m.k = "$n"
                ((Map) m).get('k')
            }
            def value = go()
            assert value instanceof String
            assert value == 'ab'
        '''
    }

    @Test
    void safePropertyAssignmentConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                m?.k = "$n"
                ((Map) m).get('k')
            }
            def value = go()
            assert value instanceof String
            assert value == 'ab'
        '''
    }

    @Test
    void chainedPropertyAssignmentConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                m.a = m.b = "$n"
                [((Map) m).get('a'), ((Map) m).get('b')]
            }
            def values = go()
            assert values.every { it instanceof String && it == 'ab' }
        '''
    }

    @Test
    void conditionalGStringOrStringConverts() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go(boolean flag) {
                def n = 'ab'
                Map<String,String> m = [:]
                m.k = flag ? "$n" : 'cd'
                ((Map) m).get('k')
            }
            assert go(true) == 'ab'
            assert go(true) instanceof String
            assert go(false) == 'cd'
            assert go(false) instanceof String
        '''
    }

    @Test
    void declaredGStringVariableConverts() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                GString g = "$n"
                Map<String,String> m = [:]
                m.k = g
                ((Map) m).get('k')
            }
            def value = go()
            assert value instanceof String
            assert value == 'ab'
        '''
    }

    @Test
    void assignmentExpressionValueIsString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,String> m = [:]
                def stored = (m.k = "$n")
                [((Map) m).get('k'), stored]
            }
            def values = go()
            assert values[0] instanceof String
            assert values[1] instanceof String
            assert values == ['ab', 'ab']
        '''
    }

    @Test
    void hashMapSubclassConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            class M extends HashMap<String,String> {
                def go() {
                    def n = 'ab'
                    this.a = "$n"
                    this.b = "x${n}"
                    [((Map) this).get('a'), ((Map) this).get('b')]
                }
            }
            def values = new M().go()
            assert values[0] instanceof String && values[0] == 'ab'
            assert values[1] instanceof String && values[1] == 'xab'
        '''
    }

    @Test
    void extendsStringWildcardConvertsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String, ? extends String> m = [:]
                m.k = "$n"
                ((Map) m).get('k')
            }
            def value = go()
            assert value instanceof String
            assert value == 'ab'
        '''
    }

    @Test
    void objectValueMapKeepsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,Object> m = [:]
                m.k = "$n"
                ((Map) m).get('k')
            }
            assert go() instanceof GString
        '''
    }

    @Test
    void charSequenceValueMapKeepsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String,CharSequence> m = [:]
                m.k = "$n"
                ((Map) m).get('k')
            }
            assert go() instanceof GString
        '''
    }

    @Test
    void superStringWildcardKeepsGString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                def n = 'ab'
                Map<String, ? super String> m = [:]
                m.k = "$n"
                ((Map) m).get('k')
            }
            assert go() instanceof GString
        '''
    }

    @Test
    void stringLiteralIsStoredAsString() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                Map<String,String> m = [:]
                m.k = 'ab'
                ((Map) m).get('k')
            }
            def value = go()
            assert value instanceof String
            assert value == 'ab'
        '''
    }

    @Test
    void nullValueIsStoredAsNull() {
        assertScript '''
            @groovy.transform.CompileStatic
            def go() {
                Map<String,String> m = [:]
                m.k = null
                ((Map) m).containsKey('k') ? ((Map) m).get('k') : 'missing'
            }
            assert go() == null
        '''
    }

    @Test
    void beanStringPropertyStillConverts() {
        assertScript '''
            class C {
                String p
            }
            @groovy.transform.CompileStatic
            def go() {
                def c = new C()
                c.p = "v${1}"
                c.p
            }
            def value = go()
            assert value instanceof String
            assert value == 'v1'
        '''
    }

    @Test
    void putAndPutAtStillRejectGString() {
        def put = shouldFail '''
            @groovy.transform.CompileStatic
            void m() {
                def n = 'ab'
                Map<String,String> map = [:]
                map.put('k', "$n")
            }
        '''
        assert put.message.contains('put(java.lang.String, java.lang.String)')
        assert put.message.contains('GString')

        def putAt = shouldFail '''
            @groovy.transform.CompileStatic
            void m() {
                def n = 'ab'
                Map<String,String> map = [:]
                map['k'] = "$n"
            }
        '''
        assert putAt.message.contains('putAt')
        assert putAt.message.contains('GString')
    }
}
