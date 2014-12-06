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

package org.codehaus.groovy.ast.decompiled

import junit.framework.TestCase
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.control.ClassNodeResolver
import org.codehaus.groovy.control.CompilationUnit
import org.objectweb.asm.Opcodes

/**
 * @author Peter Gromov
 */
class AsmDecompilerTest extends TestCase {

    void "test decompile class"() {
        ClassNode node = decompile()
        assert ToDecompile.name == node.name
        assert (node.modifiers & Opcodes.ACC_PUBLIC) != 0
    }

    void "test method object return type"() {
        def method = decompile().getDeclaredMethod("objectMethod")
        assert method.returnType.name == ClassNode.name
    }

    void "test method primitive array return type"() {
        def method = decompile().getDeclaredMethod("primitiveArrayMethod")
        assert method.returnType.array
        assert method.returnType.componentType.array
        assert method.returnType.componentType.componentType == ClassHelper.int_TYPE
    }

    void "test method parameters and exceptions"() {
        def method = decompile().getDeclaredMethods("withParametersThrowing")[0]

        assert method.parameters.length == 2
        assert method.parameters[0].type == ClassHelper.int_TYPE
        assert method.parameters[1].type.componentType.name == ToDecompile.name

        assert method.exceptions.length == 1
        assert method.exceptions[0].name == IOException.name
    }

    void "test field"() {
        def field = decompile().getDeclaredField("aField")
        assert field.type.name == Object.name
    }

    void "test constructor"() {
        def constructor = decompile().getDeclaredConstructors()[0]
        assert constructor.parameters[0].type == ClassHelper.boolean_TYPE
    }

    void "test supers"() {
        def node = decompile()
        assert node.superClass.name == SuperClass.name
        assert node.interfaces[0].name == Intf.name
    }

    private static ClassNode decompile() {
        def classFileName = ToDecompile.name.replace('.', '/') + '.class'
        def resource = AsmDecompilerTest.classLoader.getResource(classFileName)
        assert resource: classFileName
        def file = new File(resource.toURI())
        def stub = AsmDecompiler.parseClass(file)

        def unit = new CompilationUnit(new GroovyClassLoader(AsmDecompilerTest.classLoader))
        return new DecompiledClassNode(stub, new ClassNodeResolver(), unit)
    }

}

interface Intf {}

class SuperClass {}

@SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
class ToDecompile extends SuperClass implements Intf {
    protected aField

    ToDecompile(boolean b) {}

    ClassNode objectMethod() { null }

    void withParametersThrowing(int a, ToDecompile[] b) throws IOException { null }

    int[][] primitiveArrayMethod() { null }
}