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
package org.codehaus.groovy.classgen.asm

import org.apache.groovy.parser.antlr4.Antlr4ParserPlugin
import org.codehaus.groovy.GroovyBugError
import org.codehaus.groovy.ast.CodeVisitorSupport
import org.codehaus.groovy.ast.ModuleNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.classgen.BytecodeExpression
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.ParserPlugin
import org.codehaus.groovy.control.ParserPluginFactory
import org.codehaus.groovy.control.Phases
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.syntax.Reduction
import org.junit.jupiter.api.Test
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.tree.MethodNode

import static groovy.test.GroovyAssert.shouldFail
import static org.objectweb.asm.Opcodes.DUP
import static org.objectweb.asm.Opcodes.ICONST_1
import static org.objectweb.asm.Opcodes.INVOKESPECIAL
import static org.objectweb.asm.Opcodes.LDC
import static org.objectweb.asm.Opcodes.NEW

/**
 * Checks how the static initializer of an enum creates its constants.
 * <p>
 * A constant that supplies no arguments of its own is created with a direct call to the
 * {@code (String,int)} constructor of the enum, and so is a constant whose arguments are
 * literals that the only constructor able to take them accepts as they are.  Every other
 * constant, and every constant of an enum with such a constant, keeps going through the
 * synthetic {@code $INIT} helper, which spreads an {@code Object[]} over the constructors
 * of the enum and so has the meta class select one reflectively.
 */
final class EnumConstantInitBytecodeTest extends AbstractBytecodeTestCase {

    private List<String> staticInitializerOf(final String source) {
        compile(method: '<clinit>', classNamePattern: 'E', source)
        bodyOf('static <clinit>()V')
    }

    private List<String> staticInitializerOf(final CompilerConfiguration config, final String source) {
        def unit = new CompilationUnit(config)
        unit.addSource('script', source)
        unit.compile(Phases.CLASS_GENERATION)
        sequence = extractSequence(unit.classes.find { it.name == 'E' }.bytes, [method: '<clinit>'])
        bodyOf('static <clinit>()V')
    }

    // the expressions that the static initializer of the enum assigns to the given number of
    // constants, as the class generator leaves them
    private static List<Expression> constantInitsAfterClassGeneration(final String source, final int count) {
        def unit = new CompilationUnit()
        unit.addSource('script', source)
        unit.compile(Phases.CLASS_GENERATION)
        def statements = unit.AST.classes.find { it.name == 'E' }.getDeclaredMethod('<clinit>').code.statements
        statements[0..<count]*.expression*.rightExpression
    }

    private List<String> bodyOf(final String header) {
        def all = sequence.instructions
        int start = all.findIndexOf { it == header }
        assert start >= 0: "method not found: $header\n$sequence"
        int end = all.findIndexOf(start) { it.startsWith('MAXSTACK') }
        all[(start + 1)..<end]
    }

    private static void assertContiguous(final List<String> code, final List<String> pattern) {
        def body = new InstructionSequence(instructions: code.findAll {
            !(it ==~ /L\d+/ || it.startsWith('LINENUMBER') || it.startsWith('FRAME'))
        })
        assert body.hasStrictSequence(pattern)
    }

    private static void assertDirectConstructorCall(final List<String> code) {
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [], '(Ljava/lang/String;I)V')
    }

    private static void assertDirectConstructorCall(final List<String> code, final String name, final String ordinal,
                                                    final List<String> arguments, final String descriptor) {
        assertContiguous(code, [
            'NEW E',
            'DUP',
            'LDC "' + name + '"',
            ordinal,
            *arguments,
            'INVOKESPECIAL E.<init> ' + descriptor,
            'PUTSTATIC E.' + name + ' : LE;'
        ])
        assert !code.any { it.contains('$INIT') }
        assert !code.any { it.contains('selectConstructorAndTransformArguments') }
    }

    private static void assertInitHelperCall(final List<String> code) {
        assert code.any { it.contains('$INIT') }
        assert !code.any { it.startsWith('NEW E') }
    }

    // a parser that leaves the only argument of an enum constant as its initial value, where
    // the Groovy parser puts it in a list (a list argument stays in its list, as it would be
    // taken for the arguments)
    private static CompilerConfiguration unwrappingOnlyArguments() {
        new CompilerConfiguration(pluginFactory: new ParserPluginFactory() {
            @Override
            ParserPlugin createParserPlugin() {
                new Antlr4ParserPlugin() {
                    @Override
                    ModuleNode buildAST(SourceUnit sourceUnit, ClassLoader classLoader, Reduction cst) {
                        ModuleNode module = super.buildAST(sourceUnit, classLoader, cst)
                        module.classes*.fields.flatten().each { field ->
                            def init = field.initialExpression
                            if (field.enum && init instanceof ListExpression && init.expressions.size() == 1
                                    && !(init.expressions[0] instanceof ListExpression)) {
                                field.initialValueExpression = init.expressions[0]
                            }
                        }
                        module
                    }
                }
            }
        })
    }

    // the imports and the annotation of an enum whose $INIT calls an @ASTTest rewrites as a
    // transform would: the given argument is added to each call that passes only the name
    // and the ordinal, i.e. to the call of each constant without arguments of its own
    private static String addingToPlainConstants(final String argument) {
        """
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.ClassCodeVisitorSupport
            import org.codehaus.groovy.ast.expr.ArgumentListExpression
            import org.codehaus.groovy.ast.expr.ConstantExpression
            import org.codehaus.groovy.ast.expr.MethodCallExpression
            import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
            import org.codehaus.groovy.control.SourceUnit

            import static org.codehaus.groovy.control.CompilePhase.CANONICALIZATION

            @ASTTest(phase = CANONICALIZATION, value = {
                new ClassCodeVisitorSupport() {
                    protected SourceUnit getSourceUnit() { null }
                    void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                        if (call.method == '\$INIT' && call.arguments.expressions.size() == 2) {
                            call.arguments.addExpression($argument)
                        }
                    }
                }.visitMethod(node.getDeclaredMethod('<clinit>'))
            })
        """
    }

    @Test
    void testConstantsWithoutArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E { ONE, TWO }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testInitHelperIsStillGenerated() {
        staticInitializerOf '''
            enum E { ONE, TWO }
        '''
        def header = sequence.instructions.find { it.contains(' $INIT([Ljava/lang/Object;)LE;') }
        assert header != null
        assert bodyOf(header).any { it.contains('selectConstructorAndTransformArguments') }
    }

    @Test
    void testExplicitNoArgConstructorCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO
                private final String tag
                E() { tag = 'x' }
            }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testConstructorWithDefaultsCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO
                final String tag
                E(String tag = 'x') { this.tag = tag }
            }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testCompileStaticConstantsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            enum E { ONE, TWO }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testTraitImplementingEnumCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            trait Named {
                String describe() { "constant $name" }
            }
            enum E implements Named { ONE, TWO }
        '''
        assertDirectConstructorCall(code)
    }

    @Test
    void testLargeOrdinalsSelectCorrectPushInstruction() {
        def code = staticInitializerOf """
            enum E { ${(0..129).collect { 'C' + it }.join(', ')} }
        """
        assertContiguous(code, ['LDC "C5"', 'ICONST_5'])
        assertContiguous(code, ['LDC "C6"', 'BIPUSH 6'])
        assertContiguous(code, ['LDC "C127"', 'BIPUSH 127'])
        assertContiguous(code, ['LDC "C128"', 'SIPUSH 128'])
        assert !code.any { it.contains('$INIT') }
    }

    // GROOVY-12490
    @Test
    void testConstantsWithArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1), TWO(2)
                final int value
                E(int value) { this.value = value }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['ICONST_1'], '(Ljava/lang/String;II)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['ICONST_2'], '(Ljava/lang/String;II)V')
    }

    // GROOVY-12490
    @Test
    void testConstantsWithStringArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'), TWO('two')
                final String keyword
                E(String keyword) { this.keyword = keyword }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "two"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // GROOVY-12490
    @Test
    void testCompileStaticConstantsWithArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            enum E {
                ONE('one')
                final String keyword
                E(String keyword) { this.keyword = keyword }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // every kind of literal, each passed to a parameter that the runtime hands it to
    // unchanged: of its own type, of its primitive type, or of a type it extends or implements
    // GROOVY-12490
    @Test
    void testLiteralArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('s', 1, true, 2L, 1.5f, 2.5d, 3.5, 4G, -5, null, String, 6, 'x')
                E(String s, int i, boolean b, long l, float f, double d, BigDecimal bd, BigInteger bi,
                  int negative, Integer nothing, Class c, Number n, CharSequence cs) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [
            'LDC "s"',
            'ICONST_1',
            'ICONST_1',
            'LDC 2L',
            'LDC 1.5F',
            'LDC 2.5D',
            'NEW java/math/BigDecimal',
            'DUP',
            'LDC "3.5"',
            'INVOKESPECIAL java/math/BigDecimal.<init> (Ljava/lang/String;)V',
            'NEW java/math/BigInteger',
            'DUP',
            'LDC "4"',
            'INVOKESPECIAL java/math/BigInteger.<init> (Ljava/lang/String;)V',
            'BIPUSH -5',
            'ACONST_NULL',
            'LDC Ljava/lang/String;.class',
            'BIPUSH 6',
            'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;',
            'LDC "x"'
        ], '(Ljava/lang/String;ILjava/lang/String;IZJFDLjava/math/BigDecimal;Ljava/math/BigInteger;' +
            'ILjava/lang/Integer;Ljava/lang/Class;Ljava/lang/Number;Ljava/lang/CharSequence;)V')
    }

    // the number of arguments picks the constructor when no other one can take as many
    // GROOVY-12490
    @Test
    void testConstructorsOfDifferentArityCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'), TWO('two', 2)
                E(String s) { }
                E(String s, int i) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "two"', 'ICONST_2'], '(Ljava/lang/String;ILjava/lang/String;I)V')
    }

    // GROOVY-12490
    @Test
    void testOverloadedConstructorsOfTheSameArityUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1), TWO('two')
                E(int i) { }
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // GROOVY-12490
    @Test
    void testNullArgumentWithOverloadedConstructorsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(null)
                E(String s) { }
                E(Integer i) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // GROOVY-12490
    @Test
    void testVarargsConstructorUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('a', 'b')
                E(String... names) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // the runtime prefers the constructor of matching arity to a varargs one
    // GROOVY-12490
    @Test
    void testVarargsConstructorBesideMatchingConstructorCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('a')
                E(String name) { }
                E(String name, String... more) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "a"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // likewise beside a varargs constructor that could take the arguments of every constant
    // GROOVY-12490
    @Test
    void testVarargsConstructorForAnyArgumentsBesideMatchingConstructorCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('a'), TWO('b')
                E(String name) { }
                E(Object... values) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "a"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "b"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // the runtime weighs a null last argument as the array of a varargs constructor of the
    // same arity, without a penalty
    // GROOVY-12490
    @Test
    void testNullLastArgumentForAVarargsConstructorOfTheSameArityUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('a', null)
                E(String name, Integer count) { }
                E(String name, Object... more) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // GROOVY-12490
    @Test
    void testGStringArgumentUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE("o${'n'}e")
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // unlike a boolean literal, a null passes to a Boolean parameter unchanged
    // GROOVY-12490
    @Test
    void testNullArgumentForABooleanParameterCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE(null)
                E(Boolean flag) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['ACONST_NULL'], '(Ljava/lang/String;ILjava/lang/Boolean;)V')
    }

    // a varargs constructor that needs more arguments than the constant supplies is not
    // weighed by the runtime
    // GROOVY-12490
    @Test
    void testVarargsConstructorThatNeedsMoreArgumentsCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one')
                E(String s) { }
                E(String s, int i, String... more) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // the parameter of a generic constructor is of the bound of its type parameter
    // GROOVY-12490
    @Test
    void testGenericConstructorCallsConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1)
                <T extends Number> E(T n) { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [
            'ICONST_1',
            'INVOKESTATIC java/lang/Integer.valueOf (I)Ljava/lang/Integer;'
        ], '(Ljava/lang/String;ILjava/lang/Number;)V')
    }

    // the runtime converts each of these arguments on the way to the constructor (a
    // Boolean parameter goes through asBoolean), or rejects it: a null passed to a
    // primitive, a class literal passed to a String
    // GROOVY-12490
    @Test
    void testArgumentsThatTheRuntimeDoesNotPassUnchangedUseInitHelper() {
        [
            long   : '1',
            double : '1.5',
            Long   : '1',
            Boolean: 'true',
            char   : "'c'",
            int    : 'null',
            String : 'String'
        ].each { type, argument ->
            def code = staticInitializerOf """
                enum E {
                    ONE($argument)
                    E($type value) { }
                }
            """
            assertInitHelperCall(code)
        }
    }

    // GROOVY-12490
    @Test
    void testNonLiteralArgumentUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'.toUpperCase())
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // a constant that does not qualify keeps the other constants of the enum on $INIT too
    // GROOVY-12490
    @Test
    void testMixOfQualifyingAndOtherConstantsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE('one'), TWO('two'.toUpperCase())
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
    }

    // GROOVY-12490
    @Test
    void testConstantsWithArgumentsAndABodyUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(1) { int twice() { 2 } }
                E(int i) { }
                int twice() { 0 }
            }
        '''
        assertInitHelperCall(code)
    }

    // the explicit empty argument list keeps its meaning as the opt-out next to constants
    // that have arguments of their own
    // GROOVY-12490
    @Test
    void testEmptyArgumentListBesideConstantsWithArgumentsUsesInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(), TWO('two')
                E(String s = 'none') { }
            }
        '''
        assertInitHelperCall(code)
    }

    // an explicit empty argument list means the same [name, ordinal] arguments but keeps
    // the meta-class-mediated $INIT path: the per-enum opt-out from the direct call for
    // anyone intercepting constant creation through a meta class registered before init
    @Test
    void testConstantsWithEmptyArgumentListUseInitHelper() {
        def code = staticInitializerOf '''
            enum E { ONE(), TWO }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testConstantsWithNamedArgumentsUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE(value: 1), TWO(value: 2)
                int value
            }
        '''
        assertInitHelperCall(code)
    }

    @Test
    void testConstantsWithABodyUseInitHelper() {
        def code = staticInitializerOf '''
            enum E {
                ONE { int twice() { 2 } },
                TWO { int twice() { 4 } }
                abstract int twice()
            }
        '''
        assertInitHelperCall(code)
    }

    // GROOVY-12490
    @Test
    void testMixOfConstantsWithAndWithoutArgumentsCallConstructorDirectly() {
        def code = staticInitializerOf '''
            enum E {
                ONE, TWO(2)
                final int value
                E(int value = 0) { this.value = value }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', [], '(Ljava/lang/String;I)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['ICONST_2'], '(Ljava/lang/String;II)V')
    }

    // the transform leaves the enum without a constructor that takes just the name and
    // the ordinal, so the direct call is not available and $INIT is emitted as before
    @Test
    void testMissingNameAndOrdinalConstructorUsesInitHelper() {
        def code = staticInitializerOf '''
            @groovy.transform.TupleConstructor(defaults = false)
            enum E {
                ONE
                String value
            }
        '''
        assertInitHelperCall(code)
    }

    // the $INIT call that is emitted instead of the direct call must be the one the static
    // compilation transformer produced, not the untransformed call it was given
    @Test
    void testMissingNameAndOrdinalConstructorKeepsStaticInitHelperCall() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            @groovy.transform.TupleConstructor(defaults = false)
            enum E {
                ONE
                String[] value
            }
        '''
        assertInitHelperCall(code)
        assert code.any { it.contains('INVOKESTATIC E.$INIT ([Ljava/lang/Object;)LE;') }
        assert !code.any { it.contains('ScriptBytecodeAdapter') }
    }

    // likewise for constants whose arguments do not qualify for the direct call
    // GROOVY-12490
    @Test
    void testIneligibleArgumentsKeepStaticInitHelperCall() {
        def code = staticInitializerOf '''
            @groovy.transform.CompileStatic
            enum E {
                ONE(1), TWO('two')
                E(int i) { }
                E(String s) { }
            }
        '''
        assertInitHelperCall(code)
        assert code.any { it.contains('INVOKESTATIC E.$INIT ([Ljava/lang/Object;)LE;') }
        assert !code.any { it.contains('ScriptBytecodeAdapter') }
    }

    // a transform may give a constant arguments of its own: the constructor is selected for
    // the arguments the transforms leave
    // GROOVY-12490
    @Test
    void testArgumentAddedByATransformToAPlainConstantCallsConstructorDirectly() {
        def code = staticInitializerOf addingToPlainConstants("new ConstantExpression('added')") + '''
            enum E {
                ONE, TWO('two')
                E(String s = 'none') { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "added"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "two"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // likewise for an enum whose constants have no arguments of their own
    // GROOVY-12490
    @Test
    void testArgumentAddedByATransformToEveryConstantCallsConstructorDirectly() {
        def code = staticInitializerOf addingToPlainConstants("new ConstantExpression('added')") + '''
            enum E {
                ONE, TWO
                E(String s = 'none') { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "added"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "added"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // GROOVY-12490
    @Test
    void testArgumentAddedByATransformThatIsNotALiteralUsesInitHelper() {
        def code = staticInitializerOf addingToPlainConstants(
            "new MethodCallExpression(new ConstantExpression('added'), 'toUpperCase', ArgumentListExpression.EMPTY_ARGUMENTS)") + '''
            enum E {
                ONE, TWO('two')
                E(String s = 'none') { }
            }
        '''
        assertInitHelperCall(code)
    }

    // a constant whose $INIT call a transform replaces keeps every constant of the enum on
    // the calls the transforms leave
    // GROOVY-12490
    @Test
    void testInitCallReplacedByATransformKeepsTheEnumOnTheEmittedCalls() {
        def code = staticInitializerOf '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.expr.ArgumentListExpression
            import org.codehaus.groovy.ast.expr.ConstantExpression
            import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

            import static org.codehaus.groovy.control.CompilePhase.CANONICALIZATION

            @ASTTest(phase = CANONICALIZATION, value = {
                def assignment = node.getDeclaredMethod('<clinit>').code.statements[0].expression
                assignment.rightExpression = new StaticMethodCallExpression(node, 'make',
                    new ArgumentListExpression(new ConstantExpression('ONE'), new ConstantExpression(0)))
            })
            enum E {
                ONE, TWO('two')
                E(String s = 'none') { }
                static E make(String name, int ordinal) { $INIT(name, ordinal, 'made') }
            }
        '''
        assertInitHelperCall(code)
        assert code.any { it.contains('"make"') }
    }

    // the transforms, which all run before the class generator, see the $INIT calls of
    // constants that are then created directly
    // GROOVY-12490
    @Test
    void testTransformsSeeTheInitCallsOfConstantsThatAreCreatedDirectly() {
        def code = staticInitializerOf '''
            import groovy.transform.ASTTest
            import org.codehaus.groovy.ast.expr.StaticMethodCallExpression

            import static org.codehaus.groovy.control.CompilePhase.INSTRUCTION_SELECTION

            @ASTTest(phase = INSTRUCTION_SELECTION, value = {
                def inits = node.getDeclaredMethod('<clinit>').code.statements[0..1]*.expression*.rightExpression
                assert inits.every { it instanceof StaticMethodCallExpression && it.method == '$INIT' }
                assert inits*.arguments*.expressions*.value == [['ONE', 0, 'one'], ['TWO', 1]]
            })
            enum E {
                ONE('one'), TWO
                E(String s = 'none') { }
            }
        '''
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', [], '(Ljava/lang/String;I)V')
    }

    // a parser plugin may leave the only argument of a constant as its initial value instead
    // of in a list
    // GROOVY-12490
    @Test
    void testOnlyArgumentOutsideAListCallsConstructorDirectly() {
        def code = staticInitializerOf(unwrappingOnlyArguments(), '''
            enum E {
                ONE('one'), TWO('two')
                E(String s) { }
            }
        ''')
        assertDirectConstructorCall(code, 'ONE', 'ICONST_0', ['LDC "one"'], '(Ljava/lang/String;ILjava/lang/String;)V')
        assertDirectConstructorCall(code, 'TWO', 'ICONST_1', ['LDC "two"'], '(Ljava/lang/String;ILjava/lang/String;)V')
    }

    // GROOVY-12490
    @Test
    void testOnlyArgumentOutsideAListThatIsNotALiteralUsesInitHelper() {
        def code = staticInitializerOf(unwrappingOnlyArguments(), '''
            enum E {
                ONE('one'.toUpperCase()), TWO('two')
                E(String s) { }
            }
        ''')
        assertInitHelperCall(code)
    }

    // a visitor other than the class generator, such as one that walks the AST after class
    // generation, visits the $INIT calls that the direct constructor calls replace
    // GROOVY-12490
    @Test
    void testOtherVisitorsSeeTheInitCallsThatTheDirectCallsReplace() {
        def inits = constantInitsAfterClassGeneration('''
            enum E {
                ONE('one'), TWO
                E(String s = 'none') { }
            }
        ''', 2)
        assert inits.every { it instanceof BytecodeExpression }

        def calls = []
        def visitor = new CodeVisitorSupport() {
            @Override
            void visitStaticMethodCallExpression(StaticMethodCallExpression call) {
                calls << [call.method, *call.arguments.expressions*.value]
            }
        }
        inits*.visit(visitor)
        assert calls == [['$INIT', 'ONE', 0, 'one'], ['$INIT', 'TWO', 1]]
    }

    // the arguments of a constant are loaded by the class generator, which passes itself to the
    // expression it visits; a method visitor alone can only write a constant without arguments
    // GROOVY-12490
    @Test
    void testOnlyTheClassGeneratorWritesTheArgumentsOfAConstant() {
        def inits = constantInitsAfterClassGeneration('''
            enum E {
                ONE('one'), TWO
                E(String s = 'none') { }
            }
        ''', 2)

        def method = new MethodNode()
        inits[1].visit((MethodVisitor) method)
        assert method.instructions*.opcode == [NEW, DUP, LDC, ICONST_1, INVOKESPECIAL]

        def error = shouldFail(GroovyBugError) {
            inits[0].visit((MethodVisitor) new MethodNode())
        }
        assert error.message.contains('ONE')
    }
}
