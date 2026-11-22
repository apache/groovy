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
package org.codehaus.groovy.classgen.asm.sc

import groovy.transform.stc.FieldsAndPropertiesSTCTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfSystemProperty
import org.codehaus.groovy.control.CompilerConfiguration;

/**
 * Unit tests for static compilation : fields and properties.
 */
final class FieldsAndPropertiesStaticCompileTest extends FieldsAndPropertiesSTCTest implements StaticCompilationTestSupport {

    // GROOVY-5561
    @Test
    void testShouldNotThrowAccessForbidden() {
        assertScript '''
            class Test {
                def foo() {
                    def bar = createBar()
                    bar.foo
                }
                Bar createBar() { new Bar() }
            }
            class Bar {
                List<String> foo = ['1','2']
            }
            assert new Test().foo() == ['1','2']
        '''
    }

    // GROOVY-5579
    @Test
    void testUseSetterNotSetProperty() {
        assertScript '''
            Date d = new Date()
            d.time = 1

            assert d.time == 1
        '''
        String typeName = astTrees.keySet().first()
        assert astTrees[typeName][1].contains('INVOKEVIRTUAL java/util/Date.setTime (J)V')
    }

    // GROOVY-5619
    @Test
    void testUseDirectWriteFieldAccess() {
        assertScript '''
            class C {
                boolean setterCalled

                protected int x
                public void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            class D extends C {
                void directAccess() {
                    this.@x = 2
                }
            }
            D d = new D()
            d.directAccess()
            assert d.isSetterCalled() == false
            assert d.x == 2
        '''
        assert astTrees['C'][1].contains('PUTFIELD C.x')
        assert astTrees['D'][1].contains('PUTFIELD C.x')
    }

    @Test
    void testUseDirectWriteStaticFieldAccess() {
        assertScript '''
            class C {
                static boolean setterCalled

                static protected int x
                public static void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            class D extends C {
                static void directAccess() {
                    this.@x = 2
                }
            }
            D.directAccess()
            assert D.isSetterCalled() == false
            assert D.x == 2
        '''
        assert astTrees['D'][1].contains('PUTSTATIC C.x')
    }

    @Test
    void testUseSetterFieldAccess() {
        assertScript '''
            class C {
                boolean setterCalled

                protected int x
                public void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            class D extends C {
                void setterAccess() {
                    this.x = 2
                }
            }
            D d = new D()
            d.setterAccess()
            assert d.isSetterCalled() == true
            assert d.x == 2
        '''
        assert astTrees['D'][1].contains('INVOKEVIRTUAL D.setX')
    }

    @Test
    void testUseDirectWriteFieldAccessFromOutsideClass() {
        assertScript '''
            class C {
                public int x
            }
            class D  {
                void directAccess(C c) {
                    c.@x = 2
                }
            }
            D d = new D()
            C c = new C()
            d.directAccess(c)
            assert c.x == 2
        '''
        assert astTrees['D'][1].contains('PUTFIELD C.x')
    }

    @Test
    void testUseDirectWriteFieldAccessPrivateWithRuntimeClassBeingDifferent() {
        assertScript '''
            class C {
                private int x
                public C(int x) {
                    this.@x = x
                }
                public boolean sameAs(C c) {
                    return this.@x == c.@x
                }
            }
            class D extends C {
                // D.x visible in D C.x in C, but reflection depending on the runtime type would see C.x in C#sameAs and not C.x
                private int x
                public D(int x) {
                    super(x)
                    this.@x = x + 50
                }
            }
            D d = new D(1)
            C c = new C(1)
            assert d.sameAs(c)
        '''
        // same with property style access:
        assertScript '''
            class C {
                private int x
                public C(int x) {
                    this.x = x
                }
                public boolean sameAs(C c) {
                    return this.x == c.x
                }
            }
            class D extends C {
                // D.x visible in D C.x in C, but reflection depending on the runtime type would see D.x in C#sameAs and not C.x
                private int x
                public D(int x) {
                    super(x)
                    this.x = x + 50
                }
            }
            D d = new D(1)
            C c = new C(1)
            assert d.sameAs(c)
        '''
    }

    @Test
    void testReadFieldFromSameClass() {
        for (mod in ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                class C {
                    $mod int x
                    int m() {
                        x
                    }
                }
                assert new C().m() == 0
            """
            String c = astTrees['C'][1]
            assert (c =~ 'GETFIELD C.x').collect().size() == mod.empty ? 2 : 1
        }
    }

    @Test
    void testWriteFieldFromSameClass() {
        for (mod in ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                class C {
                    $mod int x
                    int m() {
                        x = 5
                        x
                    }
                }
                new C().m() == 5
            """
            String c = astTrees['C'][1]
            assert (c =~ 'PUTFIELD C.x').collect().size() == mod.empty ? 2 : 1
        }
    }

    @Test
    void testReadFieldFromSuperClass() {
        for (mod in ['public', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                class C {
                    $mod int x
                }
                class D extends C {
                    int m() {
                        x
                    }
                }
                assert new D().m() == 0
            """
            String d = astTrees['D'][1]
            assert d.contains('GETFIELD C.x')
        }
    }

    // GROOVY-9791
    @Test
    void testReadFieldFromSuperClass2() {
        assertScript '''
            package p
            class C {
                protected int x
            }
            new p.C()
        '''
        assertScript '''
            class D extends p.C {
                int m() {
                    x
                }
            }
            assert new D().m() == 0
        '''
        String  d = astTrees['D'][1]
        assert  d.contains('GETFIELD p/C.x')
        assert !d.contains('INVOKEINTERFACE groovy/lang/GroovyObject.getProperty')
    }

    // GROOVY-9791
    @Test
    void testReadFieldFromSuperClass3() {
        assertScript '''
            package p
            class C {
                protected static int x
            }
            new p.C()
        '''
        assertScript '''
            class D extends p.C {
                static int m() {
                    x
                }
            }
            assert D.m() == 0
        '''
        String  d = astTrees['D'][1]
        assert  d.contains('GETSTATIC D.x')
        assert !d.contains('INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.getGroovyObjectProperty')
    }

    @Test
    void testReadPropertyFromSuperClass() {
        for (mod in ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope']) {
            assertScript """
                class C {
                    $mod int x
                    int getX() { x }
                }
                class D extends C {
                    int m() {
                        x
                    }
                }
                assert new D().m() == 0
            """
            String  d = astTrees['D'][1]
            assert !d.contains('GETFIELD C.x') : 'no GETFIELD in D'
            assert  d.contains('INVOKEVIRTUAL D.getX') : 'getX() in D'
        }
    }

    @Test
    void testUseDirectReadFieldAccess() {
        assertScript '''
            class C {
                boolean getterCalled

                protected int x
                public int getX() {
                    getterCalled = true
                    x
                }
            }
            class D extends C {
                void m() {
                    this.@x
                }
            }
            D d = new D()
            d.m()
            assert d.isGetterCalled() == false
        '''
        String d = astTrees['D'][1]
        assert d.contains('GETFIELD C.x')
    }

    @Test
    void testUseAttributeExternal() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            C c = new C()
            c.@x = 100
            assert c.x == 100
            assert c.isSetterCalled() == false
        '''
    }

    @Test
    void testUseAttributeExternalSafe() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            C c = new C()
            c?.@x = 100
            assert c.x == 100
            assert c.isSetterCalled() == false
        '''
    }

    @Test
    void testUseAttributeExternalSafeWithNull() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            C c = null
            c?.@x = 100
        '''
    }

    @Test
    void testUseSetterExternal() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            C c = new C()
            c.x = 100
            assert c.x == 100
            assert c.isSetterCalled() == true
        '''
    }

    @Test
    void testUseAttributeExternalSpread() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<C> list = [new C(), new C()]
            list*.@x = 100
            assert list[0].x == 100
            assert list[0].isSetterCalled() == false
        '''
    }

    @Test
    void testUseAttributeExternalSpreadSafeWithNull() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<C> list = [new C(), null]
            list*.@x = 100
            assert list[0].x == 100
            assert list[0].isSetterCalled() == false
            assert list[1] == null
        '''
    }

    @Test
    void testUseAttributeExternalSpreadUsingSetter() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<C> list = [new C(), new C()]
            list*.x = 100
            assert list[0].x == 100
            assert list[0].isSetterCalled() == true
        '''
    }

    @Test
    void testUseAttributeExternalSpreadSafeWithNullUsingSetter() {
        assertScript '''
            class C {
                boolean setterCalled

                public int x
                void setX(int a) {
                    setterCalled = true
                    x = a
                }
            }
            List<C> list = [new C(), null]
            list*.x = 100
            assert list[0].x == 100
            assert list[0].isSetterCalled() == true
            assert list[1] == null
        '''
    }

    // GROOVY-5649
    @Test
    void testShouldNotThrowStackOverflowUsingThis() {
        new GroovyShell().evaluate '''
            class HaveOption {
                private String helpOption

                public void setHelpOption(String helpOption) {
                    this.helpOption = helpOption
                }
            }
            def o = new HaveOption()
            o.setHelpOption 'foo'
            assert o.helpOption
        '''
    }

    @Test
    void testShouldNotThrowStackOverflow() {
        new GroovyShell().evaluate '''
            class HaveOption {
              private String helpOption

              public void setHelpOption(String ho) {
                  helpOption = ho
              }
            }
            def o = new HaveOption()
            o.setHelpOption 'foo'
            assert o.helpOption
        '''
    }

    // GROOVY-12610: static compilation accesses the field directly from a nest-mate
    @Override @Test
    void testPrivateFieldOfSuperclassViaStaticNestedSubclass() {
        assertScript '''
            class Base {
                private String z = 'x'
                static class Sub extends Base {
                    def m1() { this.z }
                    def m2() { super.z }
                    def m3() { def c = { -> this.z }; c() }
                    def m4() { java.util.function.Supplier<String> s = () -> this.z; s.get() }
                    def m5() { this.z = 'y'; this.z }
                    def m6() { super.z = 'z'; super.z }
                    def m7(Sub that) { that.z += '!'; that.z }
                }
                static class Other {
                    def m(Sub sub) { sub.z }
                }
            }
            def sub = new Base.Sub()
            assert [sub.m1(), sub.m2(), sub.m3(), sub.m4()] == ['x', 'x', 'x', 'x']
            assert [sub.m5(), sub.m6(), sub.m7(sub)] == ['y', 'z', 'z!']
            assert new Base.Other().m(sub) == 'z!'
        '''
    }

    // GROOVY-12610: static compilation reads super.z from this instance, not the enclosing instance
    @Override @Test
    void testPrivateFieldOfSuperclassViaInnerSubclassOrDeclaringClass() {
        assertScript '''
            class Base {
                private String z
                Base(String z) { this.z = z }
                class Sub extends Base {
                    Sub() { super('sub') }
                    def m1() { this.z }
                    def m2() { z }
                    def m3() { super.z }
                    def m4() { def c = { -> z }; c() }
                    def m5() { java.util.function.Supplier<String> s = () -> this.z; s.get() }
                    def m6() { z = 'y'; z }
                }
                static class Nested extends Base {
                    Nested() { super('nested') }
                }
                static List test(Nested nested) {
                    def c = { -> nested.z }
                    [nested.z, c()]
                }
                List test() {
                    def sub = new Sub()
                    [sub.m1(), sub.m2(), sub.m3(), sub.m4(), sub.m5(), sub.m6(), z]
                }
            }
            assert new Base('outer').test() == ['outer', 'outer', 'sub', 'outer', 'outer', 'y', 'y']
            assert Base.test(new Base.Nested()) == ['nested', 'nested']
        '''
    }

    // GROOVY-12621: static compilation accesses the field directly from an enum constant body;
    // this.z and a bare z there are GROOVY-12626
    @Override @Test
    void testPrivateFieldOfEnumViaConstantBody() {
        assertScript '''
            enum E {
                A {
                    List m() {
                        def c = { -> super.z }
                        java.util.function.Supplier<String> s = () -> super.z
                        [super.z, c(), s.get()]
                    }
                    String w() { super.z = 'y'; super.z += '!'; super.z }
                }
                abstract List m()
                abstract String w()
                private String z = 'x'
            }
            assert E.A.m() == ['x', 'x', 'x']
            assert E.A.w() == 'y!'
        '''
    }

    // GROOVY-12621: static compilation writes the field directly
    @Override @Test
    void testPrivateFieldOfSuperclassWrittenThroughSuperInInnerSubclass() {
        assertScript '''
            class Base {
                private String z = 'x'
                class Sub extends Base {
                    def m1() { super.z = 'y'; super.z }
                    def m2() { super.z += 'y'; super.z }
                    def m3() { super.z = 'n'; super.z++; super.z }
                    def m4() { def c = { -> super.z = 'c' }; c(); super.z }
                }
                List test() {
                    def sub = new Sub()
                    [sub.m1(), sub.m2(), sub.m3(), sub.m4()]
                }
            }
            assert new Base().test() == ['y', 'yy', 'o', 'c']
        '''
    }

    // GROOVY-12621: static compilation accesses the field directly on any receiver
    @Override @Test
    void testPrivateFieldOfSuperclassViaReceiverOfOtherClass() {
        assertScript '''
            class Base {
                private String z = 'x'
                static class Nested extends Base {
                }
                class Sub extends Base {
                    def m1(Nested that) { that.z }
                    def m2(Sub2 that) { that.z }
                    def m3() { new Object() { def get() { Sub.this.z } }.get() }
                    def m4(Sub that) { new Object() { def get() { that.z } }.get() }
                }
                class Sub2 extends Base {
                }
                class Inner {
                    def m1(Nested that) { that.z }
                    def m2(Sub that) { that.z }
                    def m3(Nested that) { def c = { -> that.z }; c() }
                    def m4(Sub that) { that.z = 'y'; that.z }
                }
                List test() {
                    def sub = new Sub(), nested = new Nested(), inner = new Inner()
                    [sub.m1(nested), sub.m2(new Sub2()), sub.m3(), sub.m4(sub), inner.m1(nested), inner.m2(sub), inner.m3(nested), inner.m4(sub)]
                }
            }
            assert new Base().test() == ['x', 'x', 'x', 'x', 'x', 'x', 'x', 'y']
        '''
    }

    // GROOVY-12621: static compilation reads the field of the receiver itself, not of its enclosing instance
    @Override @Test
    void testPrivateFieldOfSuperclassViaReceiverOfCallingClass() {
        assertScript '''
            class Base {
                private String z
                Base(String z) { this.z = z }
                static class Nested extends Base {
                    Nested() { super('nested') }
                }
                class Sub extends Base {
                    Sub(String z) { super(z) }
                    def m1(Sub that) { that.z }
                    def m2(SubSub that) { def c = { -> that.z }; c() }
                    def m3() { def c = { -> super.z }; c() }
                    def m4(Nested that) { that.with { z } }
                    def m5() { new Object() { def get() { z } }.get() }
                    def m6() { new Inner().get() }
                    class Inner {
                        def get() { z }
                    }
                }
                class SubSub extends Sub {
                    SubSub() { super('subsub') }
                }
                class Other {
                    def m1(Sub that) { that.with { z } }
                    def m2() { new Object() { def get() { z } }.get() }
                }
                Sub newSub(String z) { new Sub(z) }
                List test(Sub that) {
                    def sub = new Sub('sub'), other = new Other()
                    [sub.m1(that), sub.m2(new SubSub()), sub.m3(), sub.m4(new Nested()), sub.m5(), sub.m6(), other.m1(that), other.m2()]
                }
            }
            def that = new Base('far').newSub('that')
            assert new Base('outer').test(that) == ['that', 'subsub', 'sub', 'nested', 'outer', 'outer', 'that', 'outer']
        '''
    }

    @Override @Test
    void testPropertyWithMultipleSetters() {
        // we need to override the test because the AST is going to be changed
        assertScript '''
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*
            import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression

            class C {
                private field
                void setX(Integer a) {field=a}
                void setX(String b) {field=b}
                def getX(){field}
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                lookup('test1').each { stmt ->
                    def exp = stmt.expression
                    assert exp instanceof ListOfExpressionsExpression
                }
                lookup('test2').each { stmt ->
                    def exp = stmt.expression
                    assert exp instanceof ListOfExpressionsExpression
                }
            })
            void test() {
                def c = new C()
                test1:
                c.x = 1
                assert c.x==1
                test2:
                c.x = "3"
                assert c.x == "3"
            }
            test()
        '''
    }

    @Test
    void testCallSetterAsPropertyWithinFinallyBlockShouldNotThrowVerifyError() {
        assertScript '''
            class C {
               void setOut(int a) {}
            }
            def c = new C()
            try {
            } finally {
                c.out = 1
            }
        '''
        String typeName = astTrees.keySet().minus('C').first()
        assert astTrees[typeName][1].contains('INVOKEVIRTUAL C.setOut (I)V')
    }

    @Test
    void testCallMultiSetterAsPropertyWithinFinallyBlockShouldNotThrowVerifyError() {
        assertScript '''
            class C {
               void setOut(int a) {}
               void setOut(String a) {}
            }
            def c = new C()
            try {
            } finally {
                c.out = 1
                c.out = 'foo'
            }
        '''
        String typeName = astTrees.keySet().minus('C').first()
        assert astTrees[typeName][1].contains('INVOKEVIRTUAL C.setOut (I)V')
        assert astTrees[typeName][1].contains('INVOKEVIRTUAL C.setOut (Ljava/lang/String;)V')
    }

    // GROOVY-7698
    @Test
    void testSafePropertyStyleSetterCalls() {
        assertScript '''
            class C {
                private String id

                void setId(String id) {
                    this.id = id
                }
            }
            C c = null
            c?.id = 'new'
        '''
    }

    // GROOVY-9700
    @Test
    void testVariableAssignmentUsesDirectSetterCall() {
        assertScript '''
            import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression

            class Foo {
                void setX(Date value) {}
                void setX(Long value) {}
            }
            class Bar extends Foo {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def assignment = node.code.statements[0].expression
                    assert assignment instanceof ListOfExpressionsExpression
                        assignment = node.code.statements[1].expression
                    assert assignment instanceof ListOfExpressionsExpression
                })
                void test() {
                    x = 42L
                    x = new Date()
                }
            }
            new Bar().test()
        '''
        String  bar = astTrees['Bar'][1]
        assert  bar.contains('INVOKEVIRTUAL Bar.setX (Ljava/lang/Long;)V')
        assert  bar.contains('INVOKEVIRTUAL Bar.setX (Ljava/util/Date;)V')
        assert !bar.contains('INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setGroovyObjectProperty ')
    }

    // GROOVY-7705, GROOVY-10687
    @Test
    @DisabledIfSystemProperty(named = CompilerConfiguration.CLOSURE_PACKING, matches = 'true',
        disabledReason = 'asserts the pre-pack generated closure-class bytecode shape; superseded when GEP-27 closure packing is enabled')
    void testPrivateFieldMutationInClosureUsesDirectAccess() {
        for (prefix in ['','this.','thisObject.','getThisObject().']) {
            assertScript """
                class Foo {
                    Closure c = { -> ${prefix}s = 'abc' }
                    private s

                    void test() {
                        c()
                        assert s == 'abc'
                    }
                }
                new Foo().test()
            """
            String  c = astTrees['Foo$_closure1'][1]
            assert  c.contains('PUTFIELD Foo.s') // JEP 181: nestmate access
            assert !c.contains('INVOKESTATIC Foo.pfaccess$00 (LFoo;Ljava/lang/Object;)')
        }
    }

    // GROOVY-7705, GROOVY-10687
    @Test
    @DisabledIfSystemProperty(named = CompilerConfiguration.CLOSURE_PACKING, matches = 'true',
        disabledReason = 'asserts the pre-pack generated closure-class bytecode shape; superseded when GEP-27 closure packing is enabled')
    void testPrivateStaticFieldMutationInClosureUsesDirectAccess() {
        assertScript '''
            class Foo {
                private static String s
                Closure c = { s = 'abc' }

                String test() {
                    c()
                    assert s == 'abc'
                }
            }
            new Foo().test()
        '''
        String  c = astTrees['Foo$_closure1'][1]
        assert  c.contains('PUTSTATIC Foo.s') // JEP 181: nestmate access
        assert !c.contains('INVOKESTATIC Foo.pfaccess$00 (LFoo;Ljava/lang/String;)')
    }

    // GROOVY-7705, GROOVY-10687
    @Test
    void testPrivateFieldMutationInAICUsesDirectAccess() {
        assertScript '''
            class C {
                private int x
                void test() {
                    def aic = new Runnable() { void run() { C.this.x = 666 } }
                    aic.run()
                    assert x == 666
                }
            }
            new C().test()
        '''
        String  aic = astTrees['C$1'][1]
        assert  aic.contains('PUTFIELD C.x') // direct access
        assert !aic.contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
    }

    // GROOVY-7705, GROOVY-10687
    @Test
    void testImplicitPrivateFieldMutationInAICUsesDirectAccess() {
        assertScript '''
            class C {
                private int x
                void test() {
                    def aic = new Runnable() { void run() { x = 666 } }
                    aic.run()
                    assert x == 666
                }
            }
            new C().test()
        '''
        String  aic = astTrees['C$1'][1]
        assert  aic.contains('PUTFIELD C.x') // direct access
        assert !aic.contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
    }

    // GROOVY-7705, GROOVY-10687
    @Test
    void testPrivateStaticFieldMutationInAICUsesDirectAccess() {
        assertScript '''
            class C {
                private static int x
                void test() {
                    def aic = new Runnable() { void run() { x = 666 } }
                    aic.run()
                    assert x == 666
                }
            }
            new C().test()
        '''
        String  aic = astTrees['C$1'][1]
        assert  aic.contains('PUTSTATIC C.x') // direct access
        assert !aic.contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
    }

    // GROOVY-7705, GROOVY-10687
    @Test
    @DisabledIfSystemProperty(named = CompilerConfiguration.CLOSURE_PACKING, matches = 'true',
        disabledReason = 'asserts the pre-pack generated closure-class bytecode shape; superseded when GEP-27 closure packing is enabled')
    void testMultiplePrivateFieldMutatorDirectAccess() {
        assertScript '''
            class C {
                private int x
                private String y
                Closure mutate = { x = 1; y = 'abc' }

                void test() {
                    mutate()
                    assert x == 1
                    assert y == 'abc'
                }
            }
            new C().test()
        '''
        String  closure = astTrees['C$_closure1'][1]
        assert  closure.contains('PUTFIELD C.x') // direct access
        assert  closure.contains('PUTFIELD C.y') // direct access
        assert !closure.contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
        assert !closure.contains('INVOKESTATIC C.pfaccess$01 (LC;Ljava/lang/String;)Ljava/lang/String;')
    }

    // GROOVY-7705, GROOVY-9385, GROOVY-10687
    @Test
    @DisabledIfSystemProperty(named = CompilerConfiguration.CLOSURE_PACKING, matches = 'true',
        disabledReason = 'asserts the pre-pack generated closure-class bytecode shape; superseded when GEP-27 closure packing is enabled')
    void testPrivateFieldBridgeMethodsAreGeneratedAsNecessary() {
        assertScript '''
            class C {
                private int accessed = 0
                private String mutated
                private String accessedAndMutated = ''
                Closure cl = {
                    println accessed
                    mutated = 'abc'
                    println accessedAndMutated
                    accessedAndMutated = 'def'
                }
                void test() {
                    cl()
                    assert mutated == 'abc'
                    assert accessedAndMutated == 'def'
                }
            }
            new C().test()
        '''
        String  dump = astTrees['C'][1]
        assert !dump.contains('pfaccess$0')  // no access bridge method for 'accessed'
        assert !dump.contains('pfaccess$00') // no mutate bridge method for 'accessed'
        assert !dump.contains('pfaccess$01') // no mutate bridge method for 'mutated'
        assert !dump.contains('pfaccess$1')  // no access bridge method for 'mutated'
        assert !dump.contains('pfaccess$2')  // no access bridge method for 'accessedAndMutated'
        assert !dump.contains('pfaccess$02') // no mutate bridge method for 'accessedAndMutated'
               dump = astTrees['C$_closure1'][1]
        assert !dump.contains('INVOKESTATIC C.pfaccess$2 (LC;)Ljava/lang/String;')
        assert !dump.contains('INVOKESTATIC C.pfaccess$02 (LC;Ljava/lang/String;)Ljava/lang/String;')
    }

    // GROOVY-12600: a class nested in a static nested subclass has no enclosing instance of the outer class; "z" is the field the subclass inherits
    @Test
    void testPrivateFieldOfOuterClassInClassNestedInStaticNestedSubclass() {
        assertScript '''
            import java.util.function.Supplier

            class Base {
                private String z = 'x'
                static class Sub extends Base {
                    class Inner { String v() { z } }
                    List read() {
                        Supplier<String> lambda = new Object() { Supplier<String> m() { () -> z } }.m()
                        [new Object() { String v() { z } }.v(), new Object() { String v() { { -> z }() } }.v(), lambda.get(), new Inner().v()]
                    }
                    String write() { new Runnable() { void run() { z = 'a' } }.run(); this.@z }
                    String writeInClosure() { new Runnable() { void run() { { -> z = 'b' }() } }.run(); this.@z }
                    String compound() { new Runnable() { void run() { z += '1'; z++; { -> z += '3' }() } }.run(); this.@z }
                }
                static class Mid {
                    class Sub extends Base {
                        String read() { new Object() { String v() { z } }.v() }
                    }
                }
            }
            def sub = new Base.Sub()
            assert sub.read() == ['x', 'x', 'x', 'x']
            assert sub.write() == 'a'
            assert sub.writeInClosure() == 'b'
            assert sub.compound() == 'b23'
            assert new Base.Mid().with { new Base.Mid.Sub(it) }.read() == 'x'
        '''
    }

    // GROOVY-12600: an inner subclass and its anonymous classes read and write the field of the enclosing instance, as dynamic Groovy does
    @Test
    void testPrivateFieldOfOuterClassInInnerSubclass() {
        assertScript '''
            import java.util.function.Supplier

            class Base {
                private String z
                Base(String z) { this.z = z }
                class Sub extends Base {
                    Sub(String z) { super(z) }
                    List test() {
                        Supplier<String> lambda = () -> z
                        def r = [z, { -> z }(), lambda.get(), new Object() { String v() { z } }.v()]
                        z = 'a';
                        { -> z += 'b' }()
                        new Runnable() { void run() { z += 'c' } }.run()
                        r << Base.this.@z << this.@z
                    }
                }
            }
            def sub = new Base('outer').with { new Base.Sub(it, 'own') }
            assert sub.test() == ['outer', 'outer', 'outer', 'outer', 'abc', 'own']
        '''
    }

    // GROOVY-12626: an enum constant body extends the enum but has no enclosing instance of it; "z" is the field it inherits
    @Test
    void testPrivateFieldOfEnumByBareNameInConstantBody() {
        assertScript '''
            import java.util.function.Supplier

            enum E {
                A {
                    List read() {
                        Supplier<String> lambda = () -> z
                        [z, this.z, { -> z }(), lambda.get(), new Object() { String v() { z } }.v(), m(), { -> m() }()]
                    }
                    String write() { z = 'a'; this.@z }
                    String writeInClosure() { { -> z = 'b' }(); this.@z }
                    String compound() { z += '1'; z++; { -> z += '3' }(); this.@z }
                }
                abstract List read()
                abstract String write()
                abstract String writeInClosure()
                abstract String compound()
                private String z = 'x'
                private String m() { z }
            }
            assert E.A.read() == ['x', 'x', 'x', 'x', 'x', 'x', 'x']
            assert E.A.write() == 'a'
            assert E.A.writeInClosure() == 'b'
            assert E.A.compound() == 'b23'
        '''
    }

    // GROOVY-12626: an anonymous subclass created in a static method has no enclosing instance; "z" is the field it inherits
    @Test
    void testPrivateFieldOfOuterClassInAnonymousSubclassInStaticMethod() {
        assertScript '''
            import java.util.function.Supplier

            class Base {
                private String z = 'x'
                static Base make() {
                    new Base() {
                        String toString() {
                            Supplier<String> lambda = () -> z
                            String r = [z, { -> z }(), lambda.get()].join()
                            z += '1'; { -> z += '2' }()
                            r + this.@z
                        }
                    }
                }
            }
            assert Base.make().toString() == 'xxxx12'
        '''
    }

    // GROOVY-8369
    @Test
    void testPropertyAccessOnEnumClass() {
        assertScript '''
            enum E { }
            assert E.modifiers == E.getModifiers()
        '''
    }

    // GROOVY-8753
    @Test
    void testPrivateFieldWithPublicGetter() {
        assertScript '''
            class C {
               private List<String> fooNames = []
               public C(Collection<String> names) {
                  names.each { fooNames << it }
               }
               public List<String> getFooNames() { fooNames }
            }
            assert new C(['foo1', 'foo2']).fooNames.size() == 2
        '''
    }

    // GROOVY-12518: the setter selected for the value, among overloads
    @Test
    void testSuperPropertyIncrementAndCompoundAssignmentWithOverloadedSetters() {
        assertScript '''
            import java.util.function.Supplier

            class Base {
                List log = []
                String x = 'x'
                int n = 1
                String y
                void setX(int i) { log << 'x:int:' + i }
                void setX(String s) { log << 'x:string:' + s; this.@x = s }
                void setN(String s) { log << 'n:string:' + s }
                void setN(int i) { log << 'n:int:' + i; this.@n = i }
                void setY(int i) { log << 'y:int:' + i }
                void setY(String s) { log << 'y:string:' + s; this.@y = s }
            }
            class Sub extends Base {
                String x = 'sub'
                int n = 100
                List test() {
                    super.x += 'a'
                    super.n++
                    ++super.n
                    super.n += 10
                    super.y ?= 'v'
                    def c = { -> super.x += 'b'; super.n++ }
                    c()
                    Supplier<Integer> lambda = () -> super.n++
                    lambda.get()
                    log + [super.x, super.n, x, n]
                }
            }
            class Outer extends Base {
                String x = 'outer'
                class Inner {
                    List test() {
                        Outer.super.x += 'c'
                        Outer.super.n++
                        def c = { -> Outer.super.n += 2 }
                        c()
                        log + [Outer.super.x, Outer.super.n, x, n]
                    }
                }
            }
            assert new Sub().test() == ['x:string:xa', 'n:int:2', 'n:int:3', 'n:int:13', 'y:string:v', 'x:string:xab', 'n:int:14', 'n:int:15', 'xab', 15, 'sub', 100]
            assert new Outer.Inner(new Outer()).test() == ['x:string:xc', 'n:int:2', 'n:int:4', 'xc', 4, 'outer', 4]
        '''
    }

    // GROOVY-12518: extension methods as the accessors
    @Test
    void testSuperPropertyIncrementAndCompoundAssignmentThroughExtensionMethods() {
        assertScript '''
            import java.util.function.Supplier

            class Text extends File {
                Text(File file) { super(file.path) }
                String getText() { 'own' }
                void setText(String s) { throw new UnsupportedOperationException(s) }
                String test() {
                    super.text = 'a'
                    super.text += 'b'
                    super.text++
                    def c = { -> super.text += 'c' }
                    c()
                    Supplier<String> lambda = () -> super.text += 'd'
                    lambda.get()
                    new File(path).getText('UTF-8')
                }
                class Inner {
                    String test() {
                        Text.super.text += 'e'
                        def c = { -> Text.super.text += 'f' }
                        c()
                        new File(Text.this.path).getText('UTF-8')
                    }
                }
            }
            def file = File.createTempFile('text', '.txt')
            try {
                def text = new Text(file)
                assert text.test() == 'accd'
                assert new Text.Inner(text).test() == 'accdef'
                assert text.text == 'own'
            } finally {
                file.delete()
            }
        '''
        astTrees.each { name, ast ->
            assert !ast[1].contains('PropertyOnSuper') : name
        }
        assert astTrees['Text'][1].contains('INVOKESTATIC org/codehaus/groovy/runtime/ResourceGroovyMethods.getText (Ljava/io/File;)Ljava/lang/String;')
        assert astTrees['Text'][1].contains('INVOKESTATIC org/codehaus/groovy/runtime/ResourceGroovyMethods.setText (Ljava/io/File;Ljava/lang/String;)V')
    }

    // GROOVY-12517: "Outer.super.@name" reads and writes the field of the superclass directly
    @Test
    void testOuterSuperFieldIsAccessedDirectly() {
        assertScript '''
            class Base {
                protected String x = 'x:base'
            }
            class Outer extends Base {
                protected String x = 'x:outer'
                class Inner {
                    List test() {
                        def read = Outer.super.@x
                        Outer.super.@x = 'w'
                        [read, Outer.super.@x, Outer.this.@x]
                    }
                }
            }
            class Sub extends Outer {
                protected String x = 'x:sub'
            }
            assert new Outer.Inner(new Outer()).test() == ['x:base', 'w', 'x:outer']
            assert new Outer.Inner(new Sub()).test() == ['x:base', 'w', 'x:outer']
        '''
        String inner = astTrees['Outer$Inner'][1]
        assert !inner.contains('FieldOnSuper')
        assert inner.contains('GETFIELD Base.x')
        assert inner.contains('PUTFIELD Base.x')
    }
}
