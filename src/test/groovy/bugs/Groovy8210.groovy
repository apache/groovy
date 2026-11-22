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

import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir

/**
 * GROOVY-8210: a string literal written with a Unicode escape (for example
 * {@code '\u0047'}) must keep a source start column of 1, not a start column
 * after the escape has been expanded.
 */
final class Groovy8210 {

    @TempDir
    File tempDir

    @Test
    void testUnicodeEscapeStringLiteralStartsAtColumnOne() {
        def found = []
        def customizer = new CompilationCustomizer(CompilePhase.CLASS_GENERATION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                def run = classNode.getDeclaredMethods('run') ?: classNode.getMethods('run')
                if (!run) return
                def visitor = new CodeVisitorSupport() {
                    @Override
                    void visitConstantExpression(ConstantExpression ce) {
                        if (ce.value == 'G') {
                            found << [line: ce.lineNumber, start: ce.columnNumber, end: ce.lastColumnNumber]
                        }
                    }
                }
                run[0].code.visit(visitor)
            }
        }
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(customizer)
        def src = new File(tempDir, 'Literal.groovy')
        src.text = "'\\u0047'\n"
        new GroovyClassLoader(this.class.classLoader, config).parseClass(src)
        assert found.size() == 1
        assert found[0].line == 1
        assert found[0].start == 1 : "expected start column 1, got ${found[0]}"
        assert found[0].end >= 8
    }
}
