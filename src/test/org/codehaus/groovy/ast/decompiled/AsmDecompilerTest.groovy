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
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.control.ClassNodeResolver
import org.codehaus.groovy.control.CompilationUnit
import org.objectweb.asm.Opcodes

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

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

    void "test simple class annotations"() {
        def node = decompile().annotations[0]

        assert node.classNode.name == Anno.name

        assert ((ConstantExpression) node.members.stringAttr).value == "s"
        assert !node.members.booleanAttr
        assert ((ClassExpression) node.members.clsAttr).type.name == String.name

        assert ((PropertyExpression) node.members.enumAttr).propertyAsString == 'BAR'
        assert ((PropertyExpression) node.members.enumAttr).objectExpression.type.name == SomeEnum.name
    }

    void "test member annotations"() {
        def node = decompile()
        assert node.getDeclaredField("aField").annotations[0].classNode.name == Anno.name
        assert node.getDeclaredMethod("objectMethod").annotations[0].classNode.name == Anno.name
        assert !node.getDeclaredMethods("withParametersThrowing")[0].annotations
    }

    void "test parameter annotations"() {
        def node = decompile()
        def params = node.getDeclaredMethods("withParametersThrowing")[0].parameters
        assert params[0].annotations.collect { it.classNode.name } == [Anno.name]
        assert !params[1].annotations
    }

    void "test primitive array attribute"() {
        def node = decompile().annotations[0]

        def list = ((ListExpression) node.members.intArrayAttr).expressions
        assert list.collect { ((ConstantExpression) it).value } == [4, 2]
    }

    void "test class array attribute"() {
        def node = decompile().annotations[0]

        def list = ((ListExpression) node.members.classArrayAttr).expressions
        assert list.collect { ((ClassExpression) it).type.name } == [ToDecompile.name]
    }

    void "test annotation array attribute"() {
        def node = decompile().annotations[0]

        def list = ((ListExpression) node.members.annoArrayAttr).expressions
        assert list.size() == 2
        list.each {
            assert it.type.name == Anno.name
            assert it instanceof AnnotationConstantExpression
        }

        def annotationNode = (AnnotationNode) ((AnnotationConstantExpression) list[1]).value
        assert ((ConstantExpression) annotationNode.members.booleanAttr).value == false
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

@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE, ElementType.METHOD, ElementType.FIELD, ElementType.PARAMETER])
@interface Anno {
    String stringAttr() default ""
    SomeEnum enumAttr() default SomeEnum.FOO
    Class clsAttr() default Object
    boolean booleanAttr() default true
    int[] intArrayAttr() default []
    Class[] classArrayAttr() default []
    Anno[] annoArrayAttr() default []
}

enum SomeEnum {
    FOO, BAR
}

@SuppressWarnings(["GroovyUnusedDeclaration", "GrMethodMayBeStatic"])
@Anno(
        stringAttr = "s",
        enumAttr = SomeEnum.BAR,
        intArrayAttr = [4, 2],
        clsAttr = String,
        classArrayAttr = [ToDecompile],
        annoArrayAttr = [@Anno, @Anno(booleanAttr = false)]
)
class ToDecompile extends SuperClass implements Intf {
    @Anno
    protected aField

    ToDecompile(boolean b) {}

    @Anno
    ClassNode objectMethod() { null }

    void withParametersThrowing(@Anno int a, ToDecompile[] b) throws IOException { }

    int[][] primitiveArrayMethod() { null }
}