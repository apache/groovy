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

import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript

/**
 * Anonymous inner class initializers must resolve bareword references to
 * enclosing-class fields lexically. A dynamic property read on {@code this}
 * is wrong when {@code this} is a {@code Map} or {@code List}: the MOP
 * treats the name as a key (or spreads it) and the enclosing field is lost.
 */
final class Groovy12063 {

    private static CompilerConfiguration nonIndy() {
        CompilerConfiguration config = new CompilerConfiguration()
        config.optimizationOptions.put('indy', false)
        return config
    }

    private static void assertBoth(String script) {
        assertScript(script)
        new GroovyShell(nonIndy()).evaluate(script)
    }

    private static void assertModes(String body) {
        for (String header : ['', '@groovy.transform.CompileStatic']) {
            assertBoth("""
                $header
                $body
            """)
        }
    }

    @Test
    void testMapInitializerStaticField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final Map<String, String> MAP = new HashMap<String, String>() {
                    {
                        put('literalKey', VALUE)
                    }
                }
            }
            assert Holder.MAP.get('literalKey') == 'expected'
        '''
    }

    @Test
    void testMapInitializerStaticFieldAsKeyAndValue() {
        assertModes '''
            class Holder {
                static final String CODE = 'literalKey'
                static final String MESSAGE = 'expected'
                static final Map<String, String> MAP = new HashMap<String, String>() {
                    {
                        put(CODE, MESSAGE)
                    }
                }
            }
            assert Holder.MAP.get('literalKey') == 'expected'
            assert Holder.MAP.size() == 1
        '''
    }

    @Test
    void testMapInitializerInstanceField() {
        assertModes '''
            class Holder {
                final String value = 'expected'
                final Map<String, String> map = new HashMap<String, String>() {
                    {
                        put('k', value)
                    }
                }
            }
            assert new Holder().map.get('k') == 'expected'
        '''
    }

    @Test
    void testMapInitializerInstanceFieldFromMethod() {
        assertModes '''
            class Holder {
                final String value = 'expected'
                Map<String, String> make() {
                    new HashMap<String, String>() {
                        {
                            put('k', value)
                        }
                    }
                }
            }
            assert new Holder().make().get('k') == 'expected'
        '''
    }

    @Test
    void testListInitializerStaticField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final List<String> LIST = new ArrayList<String>() {
                    {
                        add(VALUE)
                    }
                }
            }
            assert Holder.LIST == ['expected']
        '''
    }

    @Test
    void testNonMapInitializerStaticField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final Object box = new Object() {
                    String s = VALUE
                    @Override String toString() { s }
                }
            }
            assert Holder.box.toString() == 'expected'
        '''
    }

    @Test
    void testAnonymousFieldInitializerReadsEnclosingField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final Map<String, String> MAP = new HashMap<String, String>() {
                    private final String captured = VALUE;
                    {
                        put('k', captured)
                    }
                }
            }
            assert Holder.MAP.get('k') == 'expected'
        '''
    }

    @Test
    void testInterfaceInitializerStaticField() {
        assertModes '''
            interface Holder {
                String VALUE = 'expected'
                Map<String, String> MAP = new HashMap<String, String>() {
                    {
                        put('literalKey', VALUE)
                    }
                }
            }
            assert Holder.MAP.get('literalKey') == 'expected'
        '''
    }

    @Test
    void testNestedClassInitializerReadsOuterStaticField() {
        assertModes '''
            class Outer {
                static final String VALUE = 'expected'
                static class Mid {
                    static final Map<String, String> MAP = new HashMap<String, String>() {
                        {
                            put('k', VALUE)
                        }
                    }
                }
            }
            assert Outer.Mid.MAP.get('k') == 'expected'
        '''
    }

    @Test
    void testInnerClassInitializerReadsOuterInstanceField() {
        assertModes '''
            class Outer {
                String value = 'expected'
                class Mid {
                    Map<String, String> map = new HashMap<String, String>() {
                        {
                            put('k', value)
                        }
                    }
                }
            }
            assert new Outer.Mid(new Outer()).map.get('k') == 'expected'
        '''
    }

    @Test
    void testGStringInInitializerReadsEnclosingField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final Map<String, String> MAP = new HashMap<String, String>() {
                    {
                        put('k', "$VALUE".toString())
                    }
                }
            }
            assert Holder.MAP.get('k') == 'expected'
        '''
    }

    @Test
    void testClosureInsideInitializerReadsEnclosingField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final Map<String, String> MAP = new HashMap<String, String>() {
                    {
                        put('k', { -> VALUE }.call())
                    }
                }
            }
            assert Holder.MAP.get('k') == 'expected'
        '''
    }

    @Test
    void testAnonymousClassInsideClosureReadsEnclosingFields() {
        assertModes '''
            class Holder {
                static final String CODE = 'k'
                final String value = 'expected'
                Map<String, String> make() {
                    def factory = {
                        new HashMap<String, String>() {
                            {
                                put(CODE, value)
                            }
                        }
                    }
                    factory.call()
                }
            }
            assert new Holder().make().get('k') == 'expected'
        '''
    }

    @Test
    void testLocalVariableShadowsEnclosingField() {
        assertModes '''
            class Holder {
                static final String VALUE = 'outer'
                static Map<String, String> make() {
                    String VALUE = 'local'
                    new HashMap<String, String>() {
                        {
                            put('k', VALUE)
                        }
                    }
                }
            }
            assert Holder.make().get('k') == 'local'
        '''
    }

    @Test
    void testQualifiedEnclosingFieldStillWorks() {
        assertModes '''
            class Holder {
                static final String VALUE = 'expected'
                static final Map<String, String> MAP = new HashMap<String, String>() {
                    {
                        put('literalKey', Holder.VALUE)
                    }
                }
            }
            assert Holder.MAP.get('literalKey') == 'expected'
        '''
    }

    // Self-property precedence (GROOVY-10985) still applies to method bodies.
    @Test
    void testMapMethodBodyStillPrefersSelfProperty() {
        assertModes '''
            class Outer {
                static final String VALUE = 'outer'
                static Object read() {
                    def map = new HashMap() {
                        def read() { VALUE }
                    }
                    map.put('VALUE', 'fromMap')
                    return map.read()
                }
            }
            assert Outer.read() == 'fromMap'
        '''
    }
}
