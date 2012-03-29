/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.TypeCheckingModeTest
import org.objectweb.asm.util.TraceClassVisitor
import org.objectweb.asm.ClassReader

/**
 * Unit tests for static type checking : type checking mode.
 *
 * @author Cedric Champeau
 */
@Mixin(StaticCompilationTestSupport)
class TypeCheckingModeStaticCompileTest extends TypeCheckingModeTest {

    @Override
    protected void setUp() {
        super.setUp()
        extraSetup()
    }

    void testEnsureBytecodeIsDifferentWhenSkipped() {
        assertScript '''
            // transparent @CompileStatic
            String foo() { 'foo' }

            @groovy.transform.CompileStatic(groovy.transform.TypeCheckingMode.SKIP)
            String bar() { 'foo' }
        '''

        String bytecodeAsString = astTrees.values().iterator().next()[1]
        int st = bytecodeAsString.indexOf('foo()Ljava/lang/String;')
        int ed = bytecodeAsString.indexOf('ARETURN', st)
        int linesOfCode = bytecodeAsString.substring(st, ed).count('\n')
        assert linesOfCode == 3

        st = bytecodeAsString.indexOf('bar()Ljava/lang/String;')
        ed = bytecodeAsString.indexOf('ARETURN', st)
        linesOfCode = bytecodeAsString.substring(st, ed).count('\n')
        assert linesOfCode == 5
    }
}

