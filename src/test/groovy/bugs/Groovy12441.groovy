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

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.expr.ClosureExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.LambdaExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * {@link VariableExpression#THIS_EXPRESSION} is one shared node. Static
 * checking and static compilation must still type each use from its own
 * context, including when the same node is used inside and outside a closure.
 */
final class Groovy12441 {

    /**
     * Replaces every {@code this} / {@code super} receiver with the shared
     * sentinel, the way an AST transform does when it reuses
     * {@code VariableExpression.THIS_EXPRESSION}.
     */
    private static class ShareReceivers extends CompilationCustomizer {
        ShareReceivers() {
            super(CompilePhase.CANONICALIZATION)
        }

        @Override
        void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
            new ClassCodeExpressionTransformer() {
                @Override
                SourceUnit getSourceUnit() { source }

                @Override
                Expression transform(Expression expr) {
                    if (expr instanceof ClosureExpression || expr instanceof LambdaExpression) {
                        expr.visit(this)
                        return expr
                    }
                    if (expr instanceof VariableExpression) {
                        if (expr.isThisExpression()) return VariableExpression.THIS_EXPRESSION
                        if (expr.isSuperExpression()) return VariableExpression.SUPER_EXPRESSION
                    }
                    return super.transform(expr)
                }
            }.visitClass(classNode)
        }
    }

    private static Object eval(String code) {
        def config = new CompilerConfiguration()
        config.addCompilationCustomizers(new ShareReceivers())
        new GroovyShell(Groovy12441.classLoader, config).evaluate(code)
    }

    @Test
    void testSharedThisInsideAndOutsideClosureUnderCompileStatic() {
        assertEquals('C:C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String tag() { 'C' }
                String run() {
                    def inner = { -> this.tag() }
                    return inner.call() + ':' + this.tag()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisInsideClosureOnlyUnderCompileStatic() {
        assertEquals('C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String tag() { 'C' }
                String run() {
                    def inner = { -> this.tag() }
                    inner.call()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisOutsideClosureUnderCompileStatic() {
        assertEquals('C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String tag() { 'C' }
                String run() { this.tag() }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisNestedClosureUnderCompileStatic() {
        assertEquals('C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String tag() { 'C' }
                String run() {
                    def outer = { ->
                        def inner = { -> this.tag() }
                        inner.call()
                    }
                    outer.call()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisAsArgumentAndPropertyUnderCompileStatic() {
        assertEquals('C:field:C:field', eval('''
            @groovy.transform.CompileStatic
            class C {
                String name = 'field'
                String id(Object receiver) { receiver.getClass().getSimpleName() }
                String run() {
                    def inner = { -> this.id(this) + ':' + this.name }
                    return inner.call() + ':' + this.id(this) + ':' + this.name
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisImplicitCallInsideClosureUnderCompileStatic() {
        assertEquals('C:C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String tag() { 'C' }
                String run() {
                    def inner = { -> tag() }
                    return inner.call() + ':' + tag()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisInsideAndOutsideClosureUnderTypeChecked() {
        assertEquals('C:C', eval('''
            @groovy.transform.TypeChecked
            class C {
                String tag() { 'C' }
                String run() {
                    def inner = { -> this.tag() }
                    return inner.call() + ':' + this.tag()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisInsideClosureOnlyUnderTypeChecked() {
        assertEquals('inside', eval('''
            @groovy.transform.TypeChecked
            class C {
                String tag() { 'inside' }
                String run() {
                    def inner = { -> this.tag() }
                    inner.call()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedSuperInsideAndOutsideClosureUnderCompileStatic() {
        assertEquals('B:B', eval('''
            class Base {
                String tag() { 'B' }
            }
            @groovy.transform.CompileStatic
            class C extends Base {
                @Override String tag() { 'C' }
                String run() {
                    def inner = { -> super.tag() }
                    return inner.call() + ':' + super.tag()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedImplicitClosureMethodUnderCompileStatic() {
        assertEquals('C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String run() {
                    def inner = { -> getDelegate().getClass().getSimpleName() }
                    inner.call()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisInStaticMethodUnderCompileStatic() {
        assertEquals('S:S', eval('''
            @groovy.transform.CompileStatic
            class C {
                static String tag() { 'S' }
                static String run() {
                    def inner = { -> this.tag() }
                    return inner.call() + ':' + this.tag()
                }
            }
            C.run()
        '''))
    }

    @Test
    void testSharedThisInInnerClassClosureUnderCompileStatic() {
        assertEquals('I', eval('''
            @groovy.transform.CompileStatic
            class Outer {
                String tag() { 'O' }
                String run() { new Inner().go() }
                class Inner {
                    String mine() { 'I' }
                    String go() {
                        def inner = { -> this.mine() }
                        inner.call()
                    }
                }
            }
            new Outer().run()
        '''))
    }

    @Test
    void testSharedThisInFieldInitializerClosureUnderCompileStatic() {
        assertEquals('F', eval('''
            @groovy.transform.CompileStatic
            class C {
                String name = ({ -> this.tag() })()
                String tag() { 'F' }
            }
            new C().name
        '''))
    }

    @Test
    void testSharedThisAcrossExpressionFormsUnderCompileStatic() {
        assertEquals('C:C:C:C:C:C:C:C:C:C:n', eval('''
            @groovy.transform.CompileStatic
            class C {
                String name = 'n'
                String tag() { 'C' }
                String run() {
                    String viaIf = 'x'
                    if (this.tag()) viaIf = this.tag()
                    String viaTernary = this ? this.tag() : 'z'
                    String viaElvis = this.tag() ?: 'z'
                    String viaCast = (String) this.tag()
                    String viaGString = "${this.tag()}"
                    List viaList = [this.tag()]
                    Map viaMap = [k: this.tag()]
                    assert this.tag() == 'C' : this.tag()
                    int n = 0
                    while (n < 1) { n = n + 1; this.tag() }
                    String viaFor = ''
                    for (String s in [this.tag()]) viaFor = s
                    String viaSwitch = switch (this.tag()) {
                        case 'C' -> this.tag()
                        default -> 'z'
                    }
                    def pointer = this.&tag
                    return [viaIf, viaTernary, viaElvis, viaCast, viaGString, viaList[0], viaMap.k, viaFor, viaSwitch, pointer(), this.name].join(':')
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSharedThisMethodReferenceUnderCompileStatic() {
        assertEquals('C', eval('''
            @groovy.transform.CompileStatic
            class C {
                String tag() { 'C' }
                String run() {
                    java.util.function.Supplier<String> supplier = this::tag
                    def inner = { -> supplier.get() }
                    inner.call()
                }
            }
            new C().run()
        '''))
    }

    @Test
    void testSequentialCompilationsDoNotShareThisType() {
        assertEquals('A', eval('''
            @groovy.transform.CompileStatic
            class A {
                String tag() { 'A' }
                String run() {
                    def inner = { -> this.tag() }
                    inner.call()
                }
            }
            new A().run()
        '''))
        assertEquals('B', eval('''
            @groovy.transform.CompileStatic
            class B {
                String tag() { 'B' }
                String run() {
                    def inner = { -> this.tag() }
                    inner.call()
                }
            }
            new B().run()
        '''))
    }
}
