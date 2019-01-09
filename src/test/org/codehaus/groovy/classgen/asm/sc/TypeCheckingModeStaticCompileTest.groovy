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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.TypeCheckingModeTest

/**
 * Unit tests for static type checking : type checking mode.
 */
class TypeCheckingModeStaticCompileTest extends TypeCheckingModeTest implements StaticCompilationTestSupport {

    void testEnsureBytecodeIsDifferentWhenSkipped() {
        assertScript '''
            // transparent @CompileStatic
            String foo() { 'foo'.toUpperCase() }

            @groovy.transform.CompileStatic(groovy.transform.TypeCheckingMode.SKIP)
            String bar() { 'foo'.toUpperCase() }
        '''

        String bytecodeAsString = astTrees.values().iterator().next()[1]
        int st = bytecodeAsString.indexOf('foo()Ljava/lang/String;')
        int ed = bytecodeAsString.indexOf('ARETURN', st)
        String linesOfCode = bytecodeAsString.substring(st, ed)
        assert linesOfCode.contains('INVOKEVIRTUAL')

        st = bytecodeAsString.indexOf('bar()Ljava/lang/String;')
        ed = bytecodeAsString.indexOf('ARETURN', st)
        linesOfCode = bytecodeAsString.substring(st, ed)
        assert !linesOfCode.contains('INVOKEVIRTUAL')
    }

    void testSkipAndAnonymousInnerClass() {
        new GroovyShell().evaluate '''import groovy.transform.CompileStatic
            public interface HibernateCallback<T> {
                T doInHibernate()
            }

            @CompileStatic
            class Enclosing {
                @CompileStatic(groovy.transform.TypeCheckingMode.SKIP)
                def shouldBeSkipped(Closure callable) {
                    new HibernateCallback() {
                        @Override
                        def doInHibernate() {
                            callable(1+new Date()) // should pass because we're in a skipped section
                        }}
                }
            }

            new Enclosing().shouldBeSkipped {
                println 'This is ok'
            }
        '''
    }
}

