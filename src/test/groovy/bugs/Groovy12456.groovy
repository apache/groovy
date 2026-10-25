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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MapEntryExpression
import org.codehaus.groovy.ast.expr.MapExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.tools.GeneralUtils.args
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX

/**
 * Under static compilation, a map-style constructor call whose keys had been
 * replaced by generated method calls (as Spock does when recording the values
 * of a condition) took the key's method call target for the property setter.
 */
final class Groovy12456 {

    static Object record(int index, Object value) {
        value
    }

    /**
     * Wraps each constant key of a map-style constructor call in a call to
     * {@link #record}, without a source position, like generated code.
     */
    private static class RecordKeys extends CompilationCustomizer {
        RecordKeys() {
            super(CompilePhase.CANONICALIZATION)
        }

        @Override
        void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            def visitor = new CodeVisitorSupport() {
                @Override
                void visitConstructorCallExpression(ConstructorCallExpression call) {
                    for (arg in call.arguments) {
                        if (arg instanceof MapExpression) {
                            List<MapEntryExpression> entries = arg.mapEntryExpressions
                            entries.eachWithIndex { entry, i ->
                                if (entry.keyExpression instanceof ConstantExpression) {
                                    def key = callX(classX(ClassHelper.make(Groovy12456)), 'record', args(constX(i), entry.keyExpression))
                                    entries[i] = new MapEntryExpression(key, entry.valueExpression)
                                }
                            }
                        }
                    }
                    super.visitConstructorCallExpression(call)
                }
            }
            classNode.methods*.code*.visit(visitor)
        }
    }

    @Test
    void testMapStyleConstructorWithGeneratedKeys() {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new RecordKeys())
        def shell = new GroovyShell(getClass().classLoader, config)

        def person = shell.evaluate '''
            class Person {
                def name
                int age
            }

            @groovy.transform.CompileStatic
            def make() {
                new Person(name: 'fred', age: 42)
            }

            make()
        '''

        assert person.name == 'fred'
        assert person.age == 42
    }
}
