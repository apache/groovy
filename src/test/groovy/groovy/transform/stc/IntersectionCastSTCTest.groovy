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
package groovy.transform.stc

import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.IntersectionTypeClassNode
import org.codehaus.groovy.ast.expr.CastExpression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.tools.GenericsUtils
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.ASTTransformationCustomizer
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.control.messages.SyntaxErrorMessage
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

/**
 * Tests for static type checking of intersection-cast targets (GROOVY-11998 PR2).
 *
 * These tests stop compilation at {@link Phases#INSTRUCTION_SELECTION} so they
 * exercise resolution and STC without producing class files, which keeps the
 * tests focused on resolution/STC behavior delivered in this PR.
 */
final class IntersectionCastSTCTest {

    @Test
    void 'STC accepts (Runnable & Serializable) lambda'() {
        def cu = compileToSemantic('''
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (Runnable & java.io.Serializable) () -> { System.out.println("hi") }
                }
            }
        ''')
        assertNoErrors(cu)
    }

    @Test
    void 'STC accepts (Runnable & Serializable) closure'() {
        def cu = compileToSemantic('''
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (Runnable & java.io.Serializable) { -> System.out.println("hi") }
                }
            }
        ''')
        assertNoErrors(cu)
    }

    @Test
    void 'STC sets PRIMARY_FUNCTIONAL_TYPE and LAMBDA_MARKERS on intersection-cast lambda'() {
        def cu = compileToSemantic('''
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (Runnable & java.io.Serializable) () -> { System.out.println("hi") }
                }
            }
        ''')
        assertNoErrors(cu)
        CastExpression cast = findFirstIntersectionCast(cu)
        assert cast != null

        ClassNode primary = (ClassNode) cast.getNodeMetaData(StaticTypesMarker.PRIMARY_FUNCTIONAL_TYPE)
        assert primary != null
        assert primary.name == 'java.lang.Runnable'

        List<ClassNode> markers = (List<ClassNode>) cast.getNodeMetaData(StaticTypesMarker.LAMBDA_MARKERS)
        assert markers != null
        assert markers.size() == 1
        assert markers[0].name == 'java.io.Serializable'
    }

    @Test
    void 'lambda is marked Serializable when intersection includes Serializable'() {
        def cu = compileToSemantic('''
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (Runnable & java.io.Serializable) () -> { System.out.println("hi") }
                }
            }
        ''')
        assertNoErrors(cu)
        CastExpression cast = findFirstIntersectionCast(cu)
        assert cast != null
        assert cast.expression instanceof LambdaExpression
        assert ((LambdaExpression) cast.expression).serializable
    }

    @Test
    void 'STC rejects intersection with two SAM-bearing interfaces for lambda target'() {
        // Define two SAM-bearing interfaces inside a script
        def errors = compileExpectingErrors('''
            interface A { void runIt() }
            interface B { void doIt() }
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (A & B) () -> {}
                }
            }
        ''')
        assert errors.any { it.contains('multiple functional interface components') }
    }

    @Test
    void 'STC rejects intersection with no functional interface for lambda target'() {
        def errors = compileExpectingErrors('''
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (java.io.Serializable & Cloneable) () -> {}
                }
            }
        ''')
        assert errors.any { it.contains('no functional interface component') }
    }

    @Test
    void 'STC rejects intersection where the class component is not first'() {
        def errors = compileExpectingErrors('''
            class C {}
            @groovy.transform.TypeChecked
            class T {
                static def make(value) {
                    return (Runnable & C) value
                }
            }
        ''')
        assert errors.any { it.contains('Class component of intersection type must come first') }
    }

    @Test
    void 'STC rejects intersection with a final class component'() {
        def errors = compileExpectingErrors('''
            @groovy.transform.TypeChecked
            class T {
                static def make(value) {
                    return (String & Runnable) value
                }
            }
        ''')
        assert errors.any { it.contains('may not include the final class') }
    }

    @Test
    void 'resolver resolves all components and reclassifies'() {
        def cu = compileToSemantic('''
            @groovy.transform.TypeChecked
            class T {
                static def make() {
                    return (Runnable & java.io.Serializable) () -> { }
                }
            }
        ''')
        assertNoErrors(cu)
        CastExpression cast = findFirstIntersectionCast(cu)
        assert cast != null
        IntersectionTypeClassNode it = (IntersectionTypeClassNode) cast.type
        ClassNode[] components = it.components
        assert components.length == 2
        assert components.every { it.isResolved() || !it.isPrimaryClassNode() }
        // After resolution + reclassification, both components are interfaces, so superClass is Object
        assert it.superClass.name == 'java.lang.Object'
        assert it.interfaces*.name as Set == ['java.lang.Runnable', 'java.io.Serializable'] as Set
    }

    //--------------------------------------------------------------------------

    private static CompilationUnit compileToSemantic(String src) {
        CompilerConfiguration config = new CompilerConfiguration()
        ImportCustomizer imports = new ImportCustomizer()
        config.addCompilationCustomizers(imports)

        CompilationUnit cu = new CompilationUnit(config, null, new GroovyClassLoader())
        cu.addSource('Test.groovy', src)
        try {
            cu.compile(Phases.INSTRUCTION_SELECTION)
        } catch (MultipleCompilationErrorsException ignored) {
            // tests inspect cu.errorCollector
        }
        return cu
    }

    private static List<String> compileExpectingErrors(String src) {
        CompilationUnit cu = compileToSemantic(src)
        return cu.errorCollector.errors.findAll { it instanceof SyntaxErrorMessage }
                .collect { ((SyntaxErrorMessage) it).cause.message }
    }

    private static void assertNoErrors(CompilationUnit cu) {
        if (cu.errorCollector.hasErrors()) {
            String msg = cu.errorCollector.errors
                    .findAll { it instanceof SyntaxErrorMessage }
                    .collect { ((SyntaxErrorMessage) it).cause.message }.join('\n')
            Assertions.fail("Compilation produced errors:\n${msg}")
        }
    }

    private static CastExpression findFirstIntersectionCast(CompilationUnit cu) {
        CastExpression[] holder = new CastExpression[1]
        cu.AST.classes.each { cn ->
            cn.methods.each { mn ->
                if (mn.code == null) return
                mn.code.visit(new org.codehaus.groovy.ast.CodeVisitorSupport() {
                    @Override
                    void visitCastExpression(CastExpression expression) {
                        if (holder[0] == null && expression.type instanceof IntersectionTypeClassNode) {
                            holder[0] = expression
                        }
                        super.visitCastExpression(expression)
                    }
                })
            }
        }
        return holder[0]
    }
}
