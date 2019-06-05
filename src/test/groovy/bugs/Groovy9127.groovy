/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package groovy.bugs

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.CompilationUnit

import static org.codehaus.groovy.control.Phases.CLASS_GENERATION

@CompileStatic
final class Groovy9127 extends GroovyTestCase {

    void testReadOnlyPropertyAssignment1() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                protected String field = 'foo'
                String getField() { return field }
            }
            @groovy.transform.CompileStatic
            class Bar extends Foo {
                void changeField() {
                    field = 'bar' // GROOVY-9127: [Static type checking] - Cannot set read-only property: field
                }
                @Override
                String getField() { return 'value' }
            }
            def bar = new Bar()
            bar.changeField()
            assert bar.field == 'value'
        '''
    }

    void testReadOnlyPropertyAssignment2() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                public String field = 'foo'
                String getField() { return field }
            }
            @groovy.transform.CompileStatic
            class Bar extends Foo {
                void changeField() {
                    field = 'bar'
                }
                @Override
                String getField() { return 'value' }
            }
            def bar = new Bar()
            bar.changeField()
            assert bar.field == 'value'
        '''
    }

    void testReadOnlyPropertyAssignment3() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                @groovy.transform.PackageScope String field = 'foo'
                String getField() { return field }
            }
            @groovy.transform.CompileStatic
            class Bar extends Foo {
                void changeField() {
                    field = 'bar'
                }
                @Override
                String getField() { return 'value' }
            }
            def bar = new Bar()
            bar.changeField()
            assert bar.field == 'value'
        '''
    }

    void testReadOnlyPropertyAssignment4() {
        new CompilationUnit().with {
            addSource 'Foo.groovy', '''
                package foo

                @groovy.transform.CompileStatic
                class Foo {
                    @groovy.transform.PackageScope String field = 'foo'
                    String getField() { return field }
                }
            '''

            addSource 'Bar.groovy', '''
                package bar

                @groovy.transform.CompileStatic
                class Bar extends foo.Foo {
                    void changeField() {
                        field = 'bar'
                    }
                    @Override
                    String getField() { return 'value' }
                }
            '''

            def err = shouldFail CompilationFailedException, {
                compile CLASS_GENERATION
            }
            assert err =~ /\[Static type checking\] - Cannot set read-only property: field/
        }
    }

    void testReadOnlyPropertyAssignment5() {
        def err = shouldFail CompilationFailedException, '''
            @groovy.transform.CompileStatic
            class Foo {
                private String field = 'foo'
                String getField() { return field }
            }
            @groovy.transform.CompileStatic
            class Bar extends Foo {
                void changeField() {
                    field = 'bar'
                }
                @Override
                String getField() { return 'value' }
            }
        '''
        assert err =~ /\[Static type checking\] - Cannot set read-only property: field/
    }

    void testAttributeAssignmentVariation() {
        assertScript '''
            @groovy.transform.CompileStatic
            class Foo {
                protected String field = 'foo'
                String getField() { return field }
            }
            @groovy.transform.CompileStatic
            class Bar extends Foo {
                void changeField() {
                    this.@field = 'bar'
                }
                @Override
                String getField() { return 'value' }
            }
            def bar = new Bar()
            bar.changeField()
            assert bar.field == 'value'
        '''
    }
}
