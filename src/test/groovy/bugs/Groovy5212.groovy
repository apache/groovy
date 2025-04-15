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

import groovy.transform.AutoFinal
import org.codehaus.groovy.antlr.EnumHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.CompileUnit
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.tools.javac.JavaStubGenerator
import org.junit.Test

import static groovy.test.GroovyAssert.shouldFail
import static org.objectweb.asm.Opcodes.*

@AutoFinal
final class Groovy5212 {

    @Test
    void testShouldNotAllowExtendingEnum() {
        shouldFail '''
            enum MyEnum { X }
            enum MyExtendedEnum extends MyEnum { Y }
        '''
    }

    @Test
    void testShouldNotAllowExtendingEnumWithClass() {
        shouldFail '''
            enum MyEnum { X }
            class MyExtendedEnum extends MyEnum { }
        '''
    }

    @Test
    void testGeneratedEnumJavaStubShouldNotHaveFinalModifier() {
        ClassNode cn = EnumHelper.makeEnumNode('MyEnum', ACC_PUBLIC, ClassNode.EMPTY_ARRAY, null)
        ModuleNode module = new ModuleNode(new CompileUnit(null, null))
        cn.module = module

        String stub = new JavaStubGenerator(null).with {
            generateClass(cn); javaStubCompilationUnitSet[0].getCharContent(true)
        }
        assert !(stub =~ /final/)
    }
}
