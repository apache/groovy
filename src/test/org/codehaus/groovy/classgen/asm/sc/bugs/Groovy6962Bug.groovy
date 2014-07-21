/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.classgen.asm.sc.bugs

import groovy.transform.stc.StaticTypeCheckingTestCase
import org.codehaus.groovy.classgen.asm.sc.StaticCompilationTestSupport

class Groovy6962Bug extends StaticTypeCheckingTestCase implements StaticCompilationTestSupport {
    void testShouldNotThrowIllegalAccessException() {
        try {
            assertScript '''import org.codehaus.groovy.classgen.asm.sc.bugs.support.Groovy6962Ext
        assert new Groovy6962Ext().foo() == 1
    '''
        } finally {
            def bytecode = astTrees.values()[0][1]
            assert bytecode.contains('INVOKEVIRTUAL org/codehaus/groovy/classgen/asm/sc/bugs/support/Groovy6962Ext.foo ()I')
            println bytecode
        }
    }
}
