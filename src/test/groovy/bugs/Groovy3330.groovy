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

import groovy.transform.NonSealed
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.InnerClassNode
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.IfStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.Verifier
import org.codehaus.groovy.classgen.asm.OptimizingStatementWriter
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.jupiter.api.Test

/**
 * Joint compilation prints stubs during conversion, and printing them runs
 * verification. Verification has to leave the AST alone until class generation:
 * a transform that runs in between must still see labels on an implicit return,
 * the original {@code main} signature, default-argument expressions, and none
 * of the synthetic members verification adds for bytecode.
 */
final class Groovy3330 {

    private static final String GROOVY = '''
        import groovy.lang.annotation.ExtendedElementType
        import groovy.lang.annotation.ExtendedTarget
        import java.lang.annotation.Retention
        import java.lang.annotation.RetentionPolicy

        @Retention(RetentionPolicy.SOURCE)
        @ExtendedTarget(ExtendedElementType.LOOP)
        @interface Mark {}

        @groovy.transform.Sealed
        class Base {}

        class Child extends Base {
            String name = 'n'
            int x
            { x = 7 }

            static main(args) {
                args
            }

            def value() {
                outer:
                inner:
                123
            }

            def tagged() {
                tagged:
                1
            }

            def counted() {
                done:
                @Mark
                while (false) {
                    break done
                }
            }

            def branched() {
                gate:
                if (true) 1 else 0
            }

            def greet(String who = 'world') {
                who
            }

            def wrap(value = new Object() {
                String toString() { 'anon' }
            }) {
                value.toString()
            }

            class Inner {
                def innerValue() {
                    inside:
                    5
                }
            }
        }

        interface Face {
            def ping()
        }
    '''

    private static final String JAVA = '''
        public class Trigger {
            public static String call(Child child) {
                return child.getName() + child.greet();
            }
        }
    '''

    @Test
    void testStubPathLeavesTheSameAstAsANormalCompile() {
        compile(false, Phases.CANONICALIZATION, null) { plainUnit ->
            compile(true, Phases.CANONICALIZATION, null) { jointUnit ->
                def plain = classes(plainUnit)
                def joint = classes(jointUnit)

                assert plain.keySet().containsAll(['Base', 'Child', 'Face', 'Child$Inner'])
                assert joint.keySet().containsAll(plain.keySet())

                for (name in ['Base', 'Child', 'Face', 'Child$Inner']) {
                    assertEarlyShape(name, joint[name], plain[name])
                }
            }
        }
    }

    @Test
    void testLabeledImplicitReturnSurvivesUntilClassGeneration() {
        compile(true, Phases.CANONICALIZATION, null) { early ->
            def joint = classes(early)
            Statement value = lastStatement(joint['Child'], 'value')
            assert value instanceof ExpressionStatement
            assert value.statementLabels as Set == ['outer', 'inner'] as Set

            Statement tagged = lastStatement(joint['Child'], 'tagged')
            assert tagged instanceof ExpressionStatement
            assert tagged.statementLabels == ['tagged']

            Statement counted = lastStatement(joint['Child'], 'counted')
            assert counted.statementLabels == ['done']
            assert counted.statementAnnotations*.classNode*.name == ['Mark']

            Statement branched = lastStatement(joint['Child'], 'branched')
            assert branched instanceof IfStatement
            assert branched.statementLabels == ['gate']

            Statement inner = lastStatement(joint['Child$Inner'], 'innerValue')
            assert inner instanceof ExpressionStatement
            assert inner.statementLabels == ['inside']
        }

        compile(true, Phases.ALL, null) { unit ->
            def child = classes(unit)['Child']
            Statement returned = lastStatement(child, 'value')
            assert returned instanceof ReturnStatement
            assert returned.statementLabels as Set == ['outer', 'inner'] as Set
            assert ((ConstantExpression) ((ReturnStatement) returned).expression).value == 123

            Statement taggedReturn = lastStatement(child, 'tagged')
            assert taggedReturn instanceof ReturnStatement
            assert taggedReturn.statementLabels == ['tagged']

            Statement done = findLabeled(declaredMethod(child, 'counted').code, 'done')
            assert done != null
            assert done.statementAnnotations*.classNode*.name == ['Mark']
        }
    }

    @Test
    void testJointCompilationKeepsStubsAndRunsVerificationLater() {
        compile(true, Phases.ALL) { CompilationUnit cu ->
            cu.addPhaseOperation({ source, context, classNode ->
                if (classNode.name == 'Child') {
                    assert !implementsGroovyObject(classNode)
                    assert syntheticMembers(classNode).isEmpty()
                    assert classNode.getNodeMetaData(NonSealed) == null

                    def main = declaredMethod(classNode, 'main')
                    assert ClassHelper.isObjectType(main.parameters[0].type)
                    assert ClassHelper.isObjectType(main.returnType)

                    def greet = declaredMethod(classNode, 'greet')
                    assert greet.parameters[0].initialExpression instanceof ConstantExpression
                    assert greet.parameters[0].initialExpression.value == 'world'
                    assert greet.parameters[0].getNodeMetaData(Verifier.INITIAL_EXPRESSION) == null
                    assert declaredMethods(classNode, 'greet').size() == 1

                    Statement value = lastStatement(classNode, 'value')
                    assert value instanceof ExpressionStatement
                    assert value.statementLabels as Set == ['outer', 'inner'] as Set
                    ((ExpressionStatement) value).expression = new ConstantExpression(321)

                    def anon = anonymousClasses(classNode).find { it.name == 'Child$1' }
                    assert anon != null
                    assert anon.enclosingMethod == null || (anon.enclosingMethod.declaringClass == classNode && classNode.methods.contains(anon.enclosingMethod))
                }
                if (classNode.name == 'Face') {
                    assert classNode.getNodeMetaData(OptimizingStatementWriter.ClassNodeSkip) == null
                }
            }, Phases.CANONICALIZATION)
        } { CompilationUnit unit ->

        def childNode = classes(unit)['Child']
        def main = declaredMethod(childNode, 'main')
        assert ClassHelper.isPrimitiveVoid(main.returnType)
        assert main.parameters[0].type.isArray()
        assert ClassHelper.isStringType(main.parameters[0].type.componentType)

        Statement returned = lastStatement(childNode, 'value')
        assert returned instanceof ReturnStatement
        assert returned.statementLabels as Set == ['outer', 'inner'] as Set

        def loader = unit.classLoader
        def child = loader.loadClass('Child').getDeclaredConstructor().newInstance()
        assert child.value() == 321
        assert child.tagged() == 1
        assert child.counted() == null
        assert child.branched() == 1
        assert child.greet() == 'world'
        assert child.greet('Ada') == 'Ada'
        assert child.wrap() == 'anon'
        assert child.x == 7
        assert child.name == 'n'
        def innerClass = loader.loadClass('Child$Inner')
        def inner = innerClass.getDeclaredConstructor(child.getClass()).newInstance(child)
        assert inner.innerValue() == 5
        assert loader.loadClass('Trigger').call(child) == 'nworld'

        def fieldNames = child.getClass().declaredFields*.name
        assert fieldNames.count { it.startsWith('$staticClassInfo') } == 1
        assert fieldNames.contains('$staticClassInfo')
        assert !fieldNames.contains('$staticClassInfo$')
        assert child.getClass().declaredMethods.count { it.name == '$getStaticMetaClass' } == 1
        }
    }

    private static void assertEarlyShape(String name, ClassNode joint, ClassNode plain) {
        assert joint.name == name
        assert implementsGroovyObject(joint) == implementsGroovyObject(plain)
        assert !implementsGroovyObject(joint)
        assert syntheticMembers(joint) == syntheticMembers(plain)
        assert syntheticMembers(joint).isEmpty() : "${name} synthetic members ${syntheticMembers(joint)}"

        assert joint.getNodeMetaData(NonSealed) == plain.getNodeMetaData(NonSealed)
        assert joint.getNodeMetaData(NonSealed) == null
        assert joint.getNodeMetaData(OptimizingStatementWriter.ClassNodeSkip) ==
                plain.getNodeMetaData(OptimizingStatementWriter.ClassNodeSkip)
        assert joint.getNodeMetaData(OptimizingStatementWriter.ClassNodeSkip) == null

        assert joint.objectInitializerStatements.size() == plain.objectInitializerStatements.size()
        if (name == 'Child') {
            assert !joint.objectInitializerStatements.isEmpty()
            assert joint.getField('name').initialValueExpression instanceof ConstantExpression
            assert joint.getField('name').initialValueExpression.value == 'n'

            def main = declaredMethod(joint, 'main')
            def plainMain = declaredMethod(plain, 'main')
            assert ClassHelper.isObjectType(main.parameters[0].type)
            assert ClassHelper.isObjectType(main.returnType)
            assert ClassHelper.isObjectType(plainMain.parameters[0].type)
            assert ClassHelper.isObjectType(plainMain.returnType)

            def greet = declaredMethod(joint, 'greet')
            assert greet.parameters[0].initialExpression instanceof ConstantExpression
            assert greet.parameters[0].initialExpression.value == 'world'
            assert greet.parameters[0].getNodeMetaData(Verifier.INITIAL_EXPRESSION) == null
            assert declaredMethods(joint, 'greet').size() == declaredMethods(plain, 'greet').size()

            def anon = anonymousClasses(joint).find { it.name == 'Child$1' }
            def plainAnon = anonymousClasses(plain).find { it.name == 'Child$1' }
            assert anon != null && plainAnon != null
            // A default-argument anonymous class has no enclosing method yet. Stub
            // verification must not point it at a synthetic method that is not on the class.
            assert anon.enclosingMethod == null || (anon.enclosingMethod.declaringClass == joint && joint.methods.contains(anon.enclosingMethod))
            assert (anon.enclosingMethod == null) == (plainAnon.enclosingMethod == null)
            if (anon.enclosingMethod != null) {
                assert anon.enclosingMethod.name == plainAnon.enclosingMethod.name
            }
        }
    }

    private static List<String> syntheticMembers(ClassNode classNode) {
        def names = []
        classNode.fields.each { field ->
            if (field.name == 'metaClass' || field.name == '__$stMC' || field.name.startsWith('$staticClassInfo')) {
                names << field.name
            }
        }
        classNode.methods.each { method ->
            if (method.name == '$getStaticMetaClass' || method.name == 'getMetaClass' || method.name == 'setMetaClass') {
                names << method.name
            }
        }
        names
    }

    private static boolean implementsGroovyObject(ClassNode classNode) {
        classNode.interfaces.any { it.name == 'groovy.lang.GroovyObject' } || classNode.derivedFromGroovyObject
    }

    private static Statement findLabeled(Statement statement, String label) {
        if (statement?.statementLabels?.contains(label)) {
            return statement
        }
        if (statement instanceof BlockStatement) {
            for (child in ((BlockStatement) statement).statements) {
                Statement found = findLabeled(child, label)
                if (found != null) {
                    return found
                }
            }
        }
        null
    }

    private static Statement lastStatement(ClassNode classNode, String methodName) {
        def method = declaredMethod(classNode, methodName)
        assert method.code instanceof BlockStatement
        ((BlockStatement) method.code).statements[-1]
    }

    private static List declaredMethods(ClassNode classNode, String name) {
        classNode.methods.findAll { it.name == name && it.declaringClass == classNode }
    }

    private static declaredMethod(ClassNode classNode, String name) {
        def methods = declaredMethods(classNode, name)
        assert methods.size() == 1 : "${classNode.name}.$name -> ${methods*.typeDescriptor}"
        methods[0]
    }

    private static List<ClassNode> anonymousClasses(ClassNode classNode) {
        def found = []
        def pending = [classNode]
        while (!pending.isEmpty()) {
            def next = pending.remove(0)
            for (inner in next.innerClasses) {
                pending << inner
                if (inner instanceof InnerClassNode && ((InnerClassNode) inner).anonymous) {
                    found << inner
                }
            }
        }
        found
    }

    private static Map<String, ClassNode> classes(CompilationUnit unit) {
        def map = [:]
        unit.AST.modules.each { module ->
            module.classes.each { ClassNode cn ->
                map[cn.name] = cn
            }
        }
        map
    }

    private static void compile(boolean joint, int phase, Closure configure, Closure use) {
        def parent = File.createTempDir()
        def target = File.createTempDir()
        def loader = new GroovyClassLoader(Groovy3330.classLoader)
        try {
            new File(parent, 'Sample.groovy').text = GROOVY
            if (joint) {
                new File(parent, 'Trigger.java').text = JAVA
            }
            def config = new CompilerConfiguration(targetDirectory: target)
            if (joint) {
                config.jointCompilationOptions = [memStub: true]
            }
            CompilationUnit unit = joint ? new JavaAwareCompilationUnit(config, loader) : new CompilationUnit(config, null, loader)
            if (joint) {
                unit.addSources(parent.listFiles())
            } else {
                unit.addSource(new File(parent, 'Sample.groovy'))
            }
            if (configure != null) {
                configure(unit)
            }
            unit.compile(phase)
            use(unit)
        } finally {
            loader.close()
            parent.deleteDir()
            target.deleteDir()
        }
    }
}
