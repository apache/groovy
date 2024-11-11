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

import org.codehaus.groovy.control.CompilationUnit
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.codehaus.groovy.control.Phases.CLASS_GENERATION

final class Groovy9074 {

    @Test
    void testWildcardExtends() {
        new CompilationUnit().with {
            addSource 'Main.groovy', '''
                class Factory {
                    def <T> T make(Class<T> type, ... args) {}
                }

                @groovy.transform.TypeChecked
                void test(Factory fact, Rule rule) {
                    Type bean = fact.make(rule.type)
                }
            '''

            addSource 'Rule.groovy', '''
                class Rule {
                  Class<? extends Type> getType() {
                  }
                }
            '''

            addSource 'Type.groovy', '''
                interface Type {
                }
            '''

            compile CLASS_GENERATION
        }
    }

    @Test
    void testWildcardSuper() {
        new CompilationUnit().with {
            addSource 'Main.groovy', '''
                class Factory {
                    def <T> T make(Class<T> type, ... args) {}
                }

                @groovy.transform.TypeChecked
                void test(Factory fact, Rule rule) {
                    Type bean = fact.make(rule.type) // can't assign "? super Type" to "Type"
                }
            '''

            addSource 'Rule.groovy', '''
                class Rule {
                  Class<? super Type> getType() {
                  }
                }
            '''

            addSource 'Type.groovy', '''
                interface Type {
                }
            '''

            def err = shouldFail {
                compile CLASS_GENERATION
            }
            assert err =~ 'Cannot assign value of type java.lang.Object to variable of type Type'
        }
    }
}
