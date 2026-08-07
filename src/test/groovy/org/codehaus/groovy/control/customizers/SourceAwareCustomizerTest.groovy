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
package org.codehaus.groovy.control.customizers

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.codehaus.groovy.ast.tools.GeneralUtils.returnS
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX

/**
 * Tests that {@link SourceAwareCustomizer} tolerates a {@code null} source unit.
 */
final class SourceAwareCustomizerTest {

    /** Records the class nodes it is invoked for. */
    private static class RecordingCustomizer extends CompilationCustomizer {
        List<String> seen = []

        RecordingCustomizer() {
            super(CompilePhase.CANONICALIZATION)
        }

        @Override
        void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            seen << classNode.name
        }
    }

    @Test
    void testDelegateSeesSourceBackedClasses() {
        def recorder = new RecordingCustomizer()
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new SourceAwareCustomizer(recorder))

        new GroovyShell(config).evaluate('class Fromsource {}\nnull')

        assert 'Fromsource' in recorder.seen
    }

    @Test
    void testNullSourceUnitDoesNotInvokeDelegate() {
        def recorder = new RecordingCustomizer()
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new SourceAwareCustomizer(recorder))

        // a class node added directly to the unit has no source unit, so the customizer
        // is called with a null source (see CompilationUnit.addClassNode)
        def unit = new CompilationUnit(config)
        def classNode = new ClassNode('Synthetic', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        classNode.addMethod('answer', Modifier.PUBLIC | Modifier.STATIC,
            ClassHelper.int_TYPE, org.codehaus.groovy.ast.Parameter.EMPTY_ARRAY,
            ClassNode.EMPTY_ARRAY, returnS(constX(42)))
        unit.addClassNode(classNode)

        unit.compile(Phases.CLASS_GENERATION)

        assert recorder.seen.isEmpty()
    }
}
