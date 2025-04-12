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
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit

final class Groovy11609 {
    @Test
    void testVariableCache() {
        def cu = new CompilationUnit()
        cu.addSource('t.groovy', '''
            class BubbleSort {
                public static void bubbleSort(int[] a) {
                    for (int i = 0, n = a.length; i < n - 1; i++) {
                        for (int j = 0; j < n - i - 1; j++) {
                            if (a[j] > a[j + 1]) {
                                int temp = a[j]
                                a[j] = a[j + 1]
                                a[j + 1] = temp
                            }
                        }
                    }
                }
            }
        ''')

        cu.addPhaseOperation({ SourceUnit source, GeneratorContext context, ClassNode cn ->
            def visitor = new VariableScopeVisitor(source)
            visitor.visitClass(cn)
            assert visitor.variableCache.entrySet().stream().anyMatch(e -> e.value.accessedCount > 1)
        } as CompilationUnit.IPrimaryClassNodeOperation, Phases.CANONICALIZATION)

        cu.compile(Phases.CANONICALIZATION)
    }

    @Test
    void testClassMemberCache() {
        def cu = new CompilationUnit()
        cu.addSource('t.groovy', '''
            class Person {
                private int age
                void grow() {
                    age += 1
                }
                int getAge() {
                    return age
                }
            }
        ''')

        cu.addPhaseOperation({ SourceUnit source, GeneratorContext context, ClassNode cn ->
            def visitor = new VariableScopeVisitor(source)
            visitor.visitClass(cn)
            assert visitor.classMemberCache.entrySet().stream().anyMatch(e -> e.value.accessedCount > 1)
        } as CompilationUnit.IPrimaryClassNodeOperation, Phases.CANONICALIZATION)

        cu.compile(Phases.CANONICALIZATION)
    }
}
