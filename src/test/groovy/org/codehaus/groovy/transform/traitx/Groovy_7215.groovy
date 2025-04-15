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
package org.codehaus.groovy.transform.traitx

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

final class Groovy_7215 {

    @Test
    void testTraitAndClassUnderJointCompilation() {
        def config = new CompilerConfiguration().tap {
            jointCompilationOptions = [flags: new String[]{'release=11'}, memStub: true]
            targetDirectory = File.createTempDir()
            targetBytecode = '11'
        }
        File parentDir = File.createTempDir()
        try {
            def a = new File(parentDir, 'A.java')
            a.write '''
                public class A {
                    public static final String TEXT = "";
                }
            '''
            def b = new File(parentDir, 'B.groovy')
            b.write '''
                @groovy.transform.CompileStatic
                class B implements org.codehaus.groovy.transform.traitx.Groovy7215SupportTrait {
                    String text // same name as trait property
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def unit = new JavaAwareCompilationUnit(config, loader)
            unit.addSources(a, b)
            unit.compile()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }
}
