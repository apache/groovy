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
package org.apache.groovy.parser.antlr4;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.MixinNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.control.CompilationFailedException;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.ErrorCollector;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.reflect.Method;
import java.util.List;
import java.util.stream.Stream;

import static org.codehaus.groovy.control.CompilerConfiguration.ERROR_RECOVERY;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Parser-level snippets cannot construct every {@link InnerClassNode} shape
 * {@code isAnonymousConstructorDeclaration} defends against (interfaces live
 * on {@code new Interface()}, which is stored as the unresolved super type).
 * The same is true of empty / null names in {@code looksLikeTypeName}.
 */
final class AstBuilderHelperTest {

    @Test
    void anonymousConstructorMatchesAnImplementedInterfaceWhenSuperDoesNot() throws Exception {
        InnerClassNode anon = anonymous(ClassHelper.OBJECT_TYPE, new ClassNode[]{ClassHelper.make("Runnable")});
        assertTrue(isAnonymousConstructorDeclaration(anon, "Runnable"),
                "Runnable() {} inside new Object() { } that implements Runnable is a constructor-shaped member");
        assertFalse(isAnonymousConstructorDeclaration(anon, "Foo"),
                "a name that is neither the super type nor an interface is a method, not a constructor");
    }

    @Test
    void anonymousConstructorSkipsNullInterfaceEntries() throws Exception {
        InnerClassNode anon = anonymous(ClassHelper.OBJECT_TYPE, new ClassNode[]{ClassHelper.make("Runnable")});
        // ClassNode.setInterfaces walks the array when the node is not already
        // generic; mark it so a null slot is preserved for the skip branch.
        anon.setUsingGenerics(true);
        anon.setInterfaces(new ClassNode[]{null, ClassHelper.make("CharSequence")});
        assertTrue(isAnonymousConstructorDeclaration(anon, "CharSequence"));
    }

    @Test
    void anonymousConstructorIgnoresANullInterfaceArray() throws Exception {
        InnerClassNode anon = anonymous(ClassHelper.OBJECT_TYPE, ClassNode.EMPTY_ARRAY);
        anon.setInterfaces(null);
        assertFalse(isAnonymousConstructorDeclaration(anon, "Runnable"));
    }

    @Test
    void anonymousConstructorIgnoresANullSuperClassAndStillMatchesAnInterface() throws Exception {
        InnerClassNode anon = anonymous(null, new ClassNode[]{ClassHelper.make("Runnable")});
        assertTrue(isAnonymousConstructorDeclaration(anon, "Runnable"));
    }

    @Test
    void anonymousConstructorRejectsANamedInnerClassEvenWhenTheNameMatches() throws Exception {
        InnerClassNode named = new InnerClassNode(ClassHelper.make("Outer"), "Outer$Inner", 0, ClassHelper.make("Inner"));
        named.setAnonymous(false);
        assertFalse(isAnonymousConstructorDeclaration(named, "Inner"));
    }

    @Test
    void anonymousConstructorRejectsATopLevelClass() throws Exception {
        assertFalse(isAnonymousConstructorDeclaration(ClassHelper.make("Foo"), "Foo"));
    }

    /**
     * Fail-fast {@code addFatalError} throws inside {@code createParsingFailedException},
     * so Jacoco never sees the outer {@code throw}. Recovery records the diagnostic
     * and returns, which lets the {@code throw} complete while still failing the compile.
     */
    @ParameterizedTest
    @MethodSource("semanticDiagnostics")
    void recoveryModeStillReportsSemanticDiagnostics(final String source, final String fragment) {
        List<String> errors = compileRecovering(source);
        assertFalse(errors.isEmpty(), "recovery must still fail the compilation for:\n" + source);
        assertTrue(errors.stream().anyMatch(msg -> msg.contains(fragment)),
                () -> "expected '" + fragment + "' in: " + errors);
    }

    static Stream<Arguments> semanticDiagnostics() {
        return Stream.of(
                Arguments.of("""
                        switch (a) {
                            case 1:
                                break
                            default:
                                break
                            default:
                                break
                        }
                        """, "A switch can have only one default branch"),
                Arguments.of("""
                        switch (a) {
                            default:
                                break
                            case 1:
                                break
                        }
                        """, "A default branch must appear as the last branch of a switch"),
                Arguments.of("""
                        def r = switch (1) {
                            case 1 -> def x = 'a'; yield x
                            default -> 0
                        }
                        """, "Arrow switch cases must contain a single statement"),
                Arguments.of("record Fruit {}", "Record 'Fruit' is missing a header"),
                Arguments.of("class Fruit(String name) {}", "Only records can have a compact header"),
                Arguments.of("""
                        class Person {
                            public Person {
                            }
                        }
                        """, "Compact constructors are only allowed in records"),
                Arguments.of("""
                        abstract class Main {
                            abstract void meth() {}
                        }
                        """, "Abstract method 'meth' cannot have a body"),
                Arguments.of("def w()", "Scripts cannot declare method 'w' without a body"),
                Arguments.of("abstract v()", "Scripts cannot declare abstract method 'v'"),
                Arguments.of("abstract u() {}", "Scripts cannot declare abstract method 'u'"),
                Arguments.of("""
                        trait T {
                            abstract m() {}
                        }
                        """, "Abstract method cannot have a body"),
                Arguments.of("""
                        @interface A {
                            String a() {
                            }
                        }
                        """, "Annotation type elements cannot have a body"),
                Arguments.of("""
                        class A {
                            def x()
                        }
                        """, "Method 'x' is missing a body"),
                Arguments.of("""
                        interface I {
                            def foo() { 1 }
                        }
                        """, "Abstract method 'foo' cannot have a body"),
                Arguments.of("""
                        class Foo {}
                        new Foo() {
                            Foo() {}
                        }
                        """, "Anonymous classes cannot declare constructors"),
                Arguments.of("""
                        @interface A {
                            void a()
                        }
                        """, "Annotation type elements cannot have a void return type"),
                Arguments.of("List < Integer name", "Missing '>'"),
                Arguments.of("1 = 2", "The left-hand side of an assignment must be a variable or a field"),
                Arguments.of("foo(String a)", "Invalid method declaration; a return type or 'def' is required"),
                Arguments.of("""
                        class C {
                            void field
                        }
                        """, "'void' type is not allowed here"),
                Arguments.of("def def m() {}", "Cannot repeat modifier 'def'"),
                Arguments.of("""
                        class A {
                            private public a
                        }
                        """, "Cannot specify modifier 'public' when the access scope has already been defined"),
                Arguments.of("volatile x() {}", "Modifier 'volatile' is not allowed on a method")
        );
    }

    @Test
    void looksLikeTypeNameAcceptsClassLiteralsAndCapitalizedVariables() throws Exception {
        assertTrue(looksLikeTypeName(new ClassExpression(ClassHelper.STRING_TYPE)));
        assertTrue(looksLikeTypeName(new VariableExpression("List")));
        assertFalse(looksLikeTypeName(new VariableExpression("list")));
        assertFalse(looksLikeTypeName(new VariableExpression("")));
        assertFalse(looksLikeTypeName(new VariableExpression((String) null)));
        assertFalse(looksLikeTypeName(new ConstantExpression(1)));
    }

    private static InnerClassNode anonymous(final ClassNode superClass, final ClassNode[] interfaces) {
        InnerClassNode inner = new InnerClassNode(
                ClassHelper.make("Outer"), "Outer$1", 0, superClass, interfaces, MixinNode.EMPTY_ARRAY);
        inner.setAnonymous(true);
        return inner;
    }

    private static boolean isAnonymousConstructorDeclaration(final ClassNode classNode, final String methodName) throws Exception {
        Method method = AstBuilder.class.getDeclaredMethod("isAnonymousConstructorDeclaration", ClassNode.class, String.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, classNode, methodName);
    }

    private static boolean looksLikeTypeName(final Expression expression) throws Exception {
        Method method = AstBuilder.class.getDeclaredMethod("looksLikeTypeName", Expression.class);
        method.setAccessible(true);
        return (Boolean) method.invoke(null, expression);
    }

    private static List<String> compileRecovering(final String source) {
        CompilerConfiguration config = new CompilerConfiguration();
        config.getOptimizationOptions().put(ERROR_RECOVERY, true);
        CompilationUnit unit = new CompilationUnit(config);
        unit.addSource("test.groovy", source);
        try {
            unit.compile(Phases.CONVERSION);
        } catch (CompilationFailedException ignored) {
            // diagnostics live on the collector either way
        }
        ErrorCollector collector = unit.getErrorCollector();
        return collector.getErrors().stream()
                .filter(SyntaxErrorMessage.class::isInstance)
                .map(msg -> ((SyntaxErrorMessage) msg).getCause().getMessage())
                .toList();
    }
}
