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
package org.codehaus.groovy.classgen

import groovy.transform.CompileStatic
import org.codehaus.groovy.control.CompilerConfiguration
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

@CompileStatic
class RecordTest {
    @Test
    void testRecordOnJDK16plus() {
        assumeTrue(isAtLeastJdk('16.0'))
        assertScript(new GroovyShell(new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK16)), '''
            record RecordJDK16plus(String name) {}
            assert java.lang.Record == RecordJDK16plus.class.getSuperclass()
        ''')
    }

    @Test
    void testRecordOnJDK16plusWhenDisabled() {
        assumeTrue(isAtLeastJdk('16.0'))
        def configuration = new CompilerConfiguration(targetBytecode: CompilerConfiguration.JDK16, recordsNative: false)
        assertScript(new GroovyShell(configuration), '''
            record RecordJDK16plus2(String name) {}
            assert java.lang.Record != RecordJDK16plus2.class.getSuperclass()
        ''')
    }

    @Test
    void testInnerRecordIsImplicitlyStatic() {
        assertScript '''
            class Test {
                record Point(int i, int j) {}
            }
            assert java.lang.reflect.Modifier.isStatic(Test$Point.modifiers)
        '''
    }
}
