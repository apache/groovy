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
package org.apache.groovy.ginq

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.tools.WideningCategories
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import java.util.IdentityHashMap

/**
 * A multi-key group infers a map value from names and numbers. That lowest
 * upper bound must not be a parameterized {@code Serializable}.
 */
final class GinqMultiKeyGroupTypeTest {

    @Test
    void multiKeyGroupValueIsNotAParameterizedSerializable() {
        String script = '''
            import groovy.ginq.transform.GQ
            def result = GQ {
                from n in [[name: 'a', val: 1], [name: 'b', val: 2]]
                groupby n.name as name, n.val as val into g
                select g["name"], g["val"], g.count()
            }.toList().collect { it.toList() }.sort()
            assert result == [['a', 1, 1], ['b', 2, 1]]
        '''
        CompilationUnit unit = compileStatic(script)
        List<String> illegal = []
        Set<ClassNode> seen = Collections.newSetFromMap(new IdentityHashMap<ClassNode, Boolean>())
        unit.iterator().each { SourceUnit source ->
            source.AST.classes.each { ClassNode type ->
                new ClassCodeVisitorSupport() {
                    @Override
                    void visitClass(ClassNode node) {
                        check(node, illegal, seen)
                        super.visitClass(node)
                    }

                    @Override
                    void visitField(FieldNode node) {
                        check(node.type, illegal, seen)
                        check(node.originType, illegal, seen)
                        super.visitField(node)
                    }

                    @Override
                    void visitMethod(MethodNode node) {
                        check(node.returnType, illegal, seen)
                        super.visitMethod(node)
                    }

                    @Override
                    void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
                        for (Parameter parameter : node.parameters) {
                            check(parameter.type, illegal, seen)
                            check(parameter.originType, illegal, seen)
                        }
                        super.visitConstructorOrMethod(node, isConstructor)
                    }

                    @Override
                    protected SourceUnit getSourceUnit() { source }
                }.visitClass(type)
            }
        }
        assert illegal.isEmpty() : illegal.toString()
        for (generated in unit.classes) {
            String pool = new String(generated.bytes, 'ISO-8859-1')
            assert !pool.contains('Serializable<') : generated.name
        }
    }

    private static void check(ClassNode type, List<String> illegal, Set<ClassNode> seen) {
        if (type == null || !seen.add(type)) return
        GenericsType[] arguments = type.genericsTypes
        if (arguments != null && arguments.length > 0 && declaresNoTypeParameters(type)) {
            illegal << "${type.name} with ${arguments.length} type argument(s)"
        }
        if (arguments != null) {
            for (GenericsType argument : arguments) {
                check(argument.type, illegal, seen)
                if (argument.upperBounds != null) {
                    for (ClassNode bound : argument.upperBounds) {
                        check(bound, illegal, seen)
                    }
                }
                check(argument.lowerBound, illegal, seen)
            }
        }
        if (type instanceof WideningCategories.LowestUpperBoundClassNode) {
            check(type.unresolvedSuperClass, illegal, seen)
            for (ClassNode face : type.interfaces) {
                check(face, illegal, seen)
            }
        }
    }

    private static boolean declaresNoTypeParameters(ClassNode type) {
        String writtenName = type.name
        ClassNode declared = type.redirect()
        if (declared instanceof WideningCategories.LowestUpperBoundClassNode) {
            ClassNode named = ClassHelper.make(writtenName).redirect()
            if (!named.resolved || named.array) return writtenName == 'java.io.Serializable'
            return named.typeClass.typeParameters.length == 0
        }
        if (!declared.resolved || declared.array) return false
        return declared.typeClass.typeParameters.length == 0
    }

    /**
     * The macro lowers {@code GQ} at semantic analysis, so {@code @CompileStatic}
     * has to be on the script class before that transform runs.
     */
    private static CompilationUnit compileStatic(String script) {
        CompilerConfiguration configuration = new CompilerConfiguration()
        GroovyClassLoader loader = new GroovyClassLoader(GinqMultiKeyGroupTypeTest.classLoader, configuration)
        CompilationUnit unit = new CompilationUnit(configuration, null, loader)
        unit.addSource('GroupKey.groovy', script)
        def field = CompilationUnit.getDeclaredField('phaseOperations')
        field.accessible = true
        def operations = field.get(unit) as Deque[]
        Deque conversion = operations[Phases.CONVERSION]
        List existing = new ArrayList(conversion)
        conversion.clear()
        conversion.add(existing.get(0))
        conversion.add((CompilationUnit.ISourceUnitOperation) { SourceUnit source ->
            for (ClassNode type : source.AST.classes) {
                if (type.script) {
                    type.addAnnotation(new AnnotationNode(ClassHelper.make(CompileStatic)))
                }
            }
        })
        for (int i = 1; i < existing.size(); i++) {
            conversion.add(existing.get(i))
        }
        unit.compile()
        return unit
    }
}
