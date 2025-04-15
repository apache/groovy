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

import groovy.transform.CompileStatic
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

@CompileStatic
final class Groovy7996 {

    @Test
    void testFieldAccessFromClosure1() {
        def err = shouldFail '''
            class Foo {
                def build(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_FIRST) Closure block) {
                    this.with(block)
                }

                def propertyMissing(String name) {
                    'whatever'
                }
            }

            @groovy.transform.CompileStatic
            class Bar {
                protected List bar = []

                boolean doStuff() {
                    Foo foo = new Foo()
                    foo.build {
                        bar.isEmpty() // ClassCastException: java.lang.String cannot be cast to java.util.List
                    }
                }
            }

            new Bar().doStuff()
        '''

        assert err =~ /Cannot find matching method java.lang.Object#isEmpty\(\)/
    }

    @Test
    void testFieldAccessFromClosure2() {
        assertScript '''
            class Foo {
                def build(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_FIRST) Closure block) {
                    this.with(block)
                }

                def propertyMissing(String name) {
                    'whatever'
                }
            }

            @groovy.transform.CompileStatic
            class Bar {
                protected List bar = []

                boolean doStuff() {
                    Foo foo = new Foo()
                    foo.build {
                        owner.bar.isEmpty()
                    }
                }
            }

            assert new Bar().doStuff()
        '''
    }

    @Test
    void testFieldAccessFromClosure3() {
        assertScript '''
            class Foo {
                def build(@DelegatesTo(value=Foo, strategy=Closure.DELEGATE_FIRST) Closure block) {
                    this.with(block)
                }

                def propertyMissing(String name) {
                    'whatever'
                }
            }

            @groovy.transform.CompileStatic
            class Bar {
                protected List bar = []

                boolean doStuff() {
                    Foo foo = new Foo()
                    foo.build {
                        thisObject.bar.isEmpty()
                    }
                }
            }

            assert new Bar().doStuff()
        '''
    }

    @Test
    void testFieldAccessFromClosure4() {
        assertScript '''
            class Foo {
                def build(@DelegatesTo(value=Foo, strategy=Closure.OWNER_FIRST) Closure block) {
                    block.delegate = this
                    return block.call()
                }

                def propertyMissing(String name) {
                    'whatever'
                }
            }

            @groovy.transform.CompileStatic
            class Bar {
                protected List bar = []

                boolean doStuff() {
                    Foo foo = new Foo()
                    foo.build {
                        bar.isEmpty()
                    }
                }
            }

            assert new Bar().doStuff()
        '''
    }

    @Test // GROOVY-7687
    void testFieldAccessFromNestedClosure1() {
        assertScript '''
            @groovy.transform.CompileStatic
            class BugTest {
                static class Foo {
                    public List<String> messages = ['hello', 'world']
                }

                void interactions(Foo foo, @DelegatesTo(Foo) Closure closure) {
                    closure.delegate = foo
                    closure()
                }

                void execute() {
                    interactions(new Foo()) {
                        messages.each { it.contains('o') }
                    }
                }
            }
            new BugTest().execute()
        '''
    }

    @Test // GROOVY-8073
    void testDelegatePropertyAccessFromClosure1() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Main {
                static main(args) {
                    def map = [a: 1, b: 2]
                    map.with {
                        assert a == 1
                    }
                }
            }
        '''
    }

    @Test
    void testDelegatePropertyAccessFromClosure2() {
        assertScript '''
            import groovy.transform.*
            import org.codehaus.groovy.ast.DynamicVariable
            import org.codehaus.groovy.ast.expr.VariableExpression
            import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
            import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE
            import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.PROPERTY_OWNER

            class JSON {
                def get(String name) {
                    new JSON()
                }
            }

            class POGO {
                Number getAnswer() {
                    42
                }
                @CompileStatic
                void usage() {
                    new JSON().with {
                        @ASTTest(phase=CLASS_GENERATION, value={
                            def vexp = node.rightExpression
                            assert vexp instanceof VariableExpression
                            assert vexp.accessedVariable instanceof DynamicVariable
                            assert vexp.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                            assert vexp.getNodeMetaData(PROPERTY_OWNER).name == 'JSON'
                        })
                        def result = answer // "answer" accessed from JSON; "getAnswer()" invoked from POGO
                    }
                }
            }

            new POGO().usage()
        '''
    }
}
