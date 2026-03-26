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
package groovy

import groovy.test.GroovyTestCase
import org.junit.jupiter.api.Test
import org.objectweb.asm.ClassReader
import org.objectweb.asm.Opcodes

class ClassTest {

    @Test
    void testClassExpression() {
        def c = String.class
        assert c instanceof Class
        assert c.name == "java.lang.String" , c.name

        c = GroovyTestCase.class
        assert c instanceof Class
        assert c.name.endsWith("GroovyTestCase") , c.name

        c = ClassTest.class
        assert c instanceof Class
        assert c.name.endsWith("ClassTest") , c.name
    }

    @Test
    void testClassesHaveSuperModiferSet() {
        // ACC_SUPER (0x0020) is written into the .class file by Groovy's AsmClassGenerator
        // for every non-interface class. java.lang.reflect.Class.getModifiers() does NOT
        // expose it — bit 0x0020 is reused as ACC_SYNCHRONIZED for method modifiers — so
        // we inspect the raw bytecode access flags via ASM.
        //
        // ClassTest has Object as its implicit supertype;
        // GroovyTestCase has junit.framework.TestCase as its supertype.
        [ClassTest, GroovyTestCase].each { Class<?> clazz ->
            def stream = clazz.getResourceAsStream("/${clazz.name.replace('.', '/')}.class")
            assert (new ClassReader(stream).access & Opcodes.ACC_SUPER) != 0,
                    "Expected ACC_SUPER to be set on ${clazz.name}"
        }
    }

}
