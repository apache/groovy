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
package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.junit.jupiter.api.Test

final class PeepholeBytecodeIntegrationTest extends AbstractBytecodeTestCase {

    @Test
    void testListHelperMethodsUseCompactIndexes() {
        def constants = (0..1000).join(', ')
        def bytecode = compile(method: '$createListEntry_1', """\
            def values = [$constants]
        """)

        assert bytecode.hasStrictSequence([
                'ALOAD 0',
                'ICONST_0',
                'ICONST_0',
                'INVOKESTATIC java/lang/Integer.valueOf',
        ])
        assert bytecode.hasSequence([
                'ALOAD 0',
                'BIPUSH 6',
                'BIPUSH 6',
                'INVOKESTATIC java/lang/Integer.valueOf',
        ])
    }

    @Test
    void testCallSiteArrayHelpersUseCompactIndexes() {
        def bytecode = compileClassic(method: '$createCallSiteArray_1', '''\
            def text = 'x'
            text.toUpperCase()
            text.toLowerCase()
            text.trim()
        ''')

        assert clazz.declaredMethods*.name.contains('$createCallSiteArray_1')
        assert bytecode.hasStrictSequence([
                'ALOAD 0',
                'ICONST_0',
                'LDC',
        ])
        assert bytecode.hasSequence([
                'ALOAD 0',
                'ICONST_1',
                'LDC',
        ])
    }

    protected InstructionSequence compileClassic(Map options = [:], final String scriptText) {
        options = [method: 'run', classNamePattern: '.*script', *: options]
        sequence = null
        clazz = null
        classBytes = null

        def cu = new CompilationUnit(new CompilerConfiguration(optimizationOptions: [indy: false]))
        def su = cu.addSource('script', scriptText)
        cu.compile(Phases.CONVERSION)
        if (options.conversionAction != null) {
            options.conversionAction(su)
        }
        cu.compile(Phases.CLASS_GENERATION)

        for (gc in cu.classes) {
            if (gc.name ==~ options.classNamePattern) {
                classBytes = gc.bytes
                sequence = extractSequence(gc.bytes, options)
            }
        }
        if (sequence == null && cu.classes) {
            def gc = cu.classes.find { it.name == cu.firstClassNode.name }
            classBytes = gc.bytes
            sequence = extractSequence(gc.bytes, options)
        }
        for (gc in cu.classes) {
            Class c = cu.classLoader.defineClass(gc.name, gc.bytes)
            if (Script.isAssignableFrom(c)) { clazz = c }
            c.isInterface()
        }

        sequence
    }
}
