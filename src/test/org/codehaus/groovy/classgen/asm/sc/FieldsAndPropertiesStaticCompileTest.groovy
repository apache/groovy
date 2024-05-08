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

/**
 * Unit tests for static compilation : fields and properties.
 */
final class FieldsAndPropertiesStaticCompileTest extends FieldsAndPropertiesSTCTest implements StaticCompilationTestSupport {

    // GROOVY-5561
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
    void testUseSetterNotSetProperty() {
        assertScript '''
            Date d = new Date()
            d.time = 1

            assert d.time == 1
        '''
        assert astTrees.values().any {
            it.toString().contains 'INVOKEVIRTUAL java/util/Date.setTime (J)V'
        }
    }

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
        assert astTrees['D'][1].contains('PUTFIELD C.x')
    }

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

    void testReadFieldFromSameClass() {
        ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope'].each { mod ->
            assertScript """
                class C {
                    $mod int x
                    int m() {
                        x
                    }
                }
                assert new C().m() == 0
            """
            def a = astTrees['C'][1]
            assert (a =~ 'GETFIELD C.x').collect().size() == mod.empty ? 2 : 1
        }
    }

    void testWriteFieldFromSameClass() {
        ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope'].each { mod ->
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
            def a = astTrees['C'][1]
            assert (a =~ 'PUTFIELD C.x').collect().size() == mod.empty ? 2 : 1
        }
    }

    void testReadFieldFromSuperClass() {
        ['public', 'protected', '@groovy.transform.PackageScope'].each { mod ->
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
            def d = astTrees['D'][1]
            assert  d.contains('GETFIELD C.x')
        }
    }

    // GROOVY-9791
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
        def b = astTrees['D'][1]
        assert  b.contains('GETFIELD p/C.x')
        assert !b.contains('INVOKEINTERFACE groovy/lang/GroovyObject.getProperty')
    }

    // GROOVY-9791
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
        def d = astTrees['D'][1]
        assert  d.contains('GETSTATIC D.x')
        assert !d.contains('INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.getGroovyObjectProperty')
    }

    void testReadPropertyFromSuperClass() {
        ['', 'public', 'private', 'protected', '@groovy.transform.PackageScope'].each { mod ->
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
            def d = astTrees['D'][1]
            assert !d.contains('GETFIELD C.x') : 'no GETFIELD in D'
            assert  d.contains('INVOKEVIRTUAL D.getX') : 'getX() in D'
        }
    }

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
        def d = astTrees['D'][1]
        assert d.contains('GETFIELD C.x')
    }

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

    @Override
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

    void testCallSetterAsPropertyWithinFinallyBlockShouldNotThrowVerifyError() {
        try {
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
        } finally {
            assert astTrees.values().any {
                it.toString().contains 'INVOKEVIRTUAL C.setOut (I)V'
            }
        }
    }

    void testCallMultiSetterAsPropertyWithinFinallyBlockShouldNotThrowVerifyError() {
        try {
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
        } finally {
            assert astTrees.values().any {
                def code = it.toString()
                code.contains('INVOKEVIRTUAL C.setOut (I)V')
                        && code.contains('INVOKEVIRTUAL C.setOut (Ljava/lang/String;)V')
            }
        }
    }

    // GROOVY-7698
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

        def bar = astTrees['Bar'][1]
        assert bar.contains('INVOKEVIRTUAL Bar.setX (Ljava/lang/Long;)V')
        assert bar.contains('INVOKEVIRTUAL Bar.setX (Ljava/util/Date;)V')
        assert !bar.contains('INVOKESTATIC org/codehaus/groovy/runtime/ScriptBytecodeAdapter.setGroovyObjectProperty ')
    }

    void testPrivateFieldMutationInClosureUsesBridgeMethod() {
        try {
            assertScript '''
                class Foo {
                    private String s
                    Closure c = { this.s = 'abc' }

                    void test() {
                        c()
                        assert s == 'abc'
                    }
                }
                new Foo().test()
            '''
        } finally {
            assert astTrees['Foo$_closure1'][1].contains('INVOKESTATIC Foo.pfaccess$00 (LFoo;Ljava/lang/String;)Ljava/lang/String')
        }
    }

    void testImplicitPrivateFieldMutationInClosureUsesBridgeMethod() {
        try {
            assertScript '''
                class Foo {
                    private String s
                    Closure c = { s = 'abc' }

                    String test() {
                        c()
                        assert s == 'abc'
                    }
                }
                new Foo().test()
            '''
        } finally {
            assert astTrees['Foo$_closure1'][1].contains('INVOKESTATIC Foo.pfaccess$00 (LFoo;Ljava/lang/String;)Ljava/lang/String')
        }
    }

    void testPrivateStaticFieldMutationInClosureUsesBridgeMethod() {
        try {
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
        } finally {
            assert astTrees['Foo$_closure1'][1].contains('INVOKESTATIC Foo.pfaccess$00 (LFoo;Ljava/lang/String;)Ljava/lang/String')
        }
    }

    void testPrivateFieldMutationInAICUsesBridgeMethod() {
        try {
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
        } finally {
            assert astTrees['C$1'][1].contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
        }
    }

    void testImplicitPrivateFieldMutationInAICUsesBridgeMethod() {
        try {
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
        } finally {
            assert astTrees['C$1'][1].contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
        }
    }

    void testPrivateStaticFieldMutationInAICUsesBridgeMethod() {
        try {
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
        } finally {
            assert astTrees['C$1'][1].contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
        }
    }

    void testMultiplePrivateFieldMutatorBridgeMethods() {
        try {
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
        } finally {
            assert astTrees['C$_closure1'][1].contains('INVOKESTATIC C.pfaccess$00 (LC;I)I')
            assert astTrees['C$_closure1'][1].contains('INVOKESTATIC C.pfaccess$01 (LC;Ljava/lang/String;)Ljava/lang/String;')
        }
    }

    void testPrivateFieldBridgeMethodsAreGeneratedAsNecessary() {
        try {
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
        } finally {
            def dump = astTrees['C'][1]
            assert dump.contains('pfaccess$0') // accessor bridge method for 'accessed'
            assert !dump.contains('pfaccess$00') // no mutator bridge method for 'accessed'
            assert dump.contains('pfaccess$01') // mutator bridge method for 'mutated'
            assert dump.contains('pfaccess$1') // accessor bridge method for 'mutated' -- GROOVY-9385
            assert dump.contains('pfaccess$2') // accessor bridge method for 'accessedAndMutated'
            assert dump.contains('pfaccess$02') // mutator bridge method for 'accessedAndMutated'
            dump = astTrees['C$_closure1'][1]
            assert dump.contains('INVOKESTATIC C.pfaccess$2 (LC;)Ljava/lang/String;')
            assert dump.contains('INVOKESTATIC C.pfaccess$02 (LC;Ljava/lang/String;)Ljava/lang/String;')
        }
    }

    // GROOVY-8369
    void testPropertyAccessOnEnumClass() {
        assertScript '''
            enum E { }
            assert E.modifiers == E.getModifiers()
        '''
    }

    // GROOVY-8753
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

    // GROOVY-9683
    void testPrivateFieldAccessInClosure3() {
        assertScript '''
            class C {
                private static X = 'xxx'
                void test() {
                    [:].with {
                        assert X == 'xxx'
                    }
                }
            }
            new C().test()
        '''
    }

    // GROOVY-9695
    void testPrivateFieldAccessInClosure4() {
        assertScript '''
            class C {
                private static final X = 'xxx'
                void test() {
                    Map m = [:]
                    def c = { ->
                        assert X == 'xxx'
                        m[X] = 123
                    }
                    c()
                    assert m == [xxx:123]
                }
            }
            new C().test()

            class D extends C {
            }
            new D().test()
        '''
    }

    // GROOVY-5001, GROOVY-5491, GROOVY-11367, GROOVY-11369
    void testMapPropertyAccess() {
        assertScript '''
            Map<String,Number> map = [:]

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == boolean_TYPE
            })
            def a = map.empty

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == CLASS_Type
            })
            def b = map.class

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == METACLASS_TYPE
            })
            def c = map.metaClass

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Number_TYPE
            })
            def d = map.something
        '''
    }

    // GROOVY-11367, GROOVY-11369
    @Override
    void testMapPropertyAccess5() {
        shouldFailWithMessages '''
            def map = [:]
            map.empty = null
        ''',
        'Cannot set read-only property: empty'

        shouldFailWithMessages '''
            def map = [:]
            map.class = null
        ''',
        'Cannot set read-only property: class'

        assertScript '''
            def map = [:]
            map.metaClass = null // TODO: GROOVY-6549 made this "put" (SC only)!
            assert map.metaClass != null
            assert map.containsKey('metaClass')
        '''
    }

    // GROOVY-11367, GROOVY-11368
    @Override
    void testMapPropertyAccess9() {
        String type = '''
            class C implements Map<String,String> {
                @Delegate Map<String,String> impl = [:]
            }
        '''
        assertScript type + '''
            def map = new C()
            assert map.entry == null
            assert map.empty != null
            assert map.class != null
            assert map.metaClass != null
        '''
        assertScript type + '''
            def test(C map) { // no diff
                assert map.entry == null
                assert map.empty != null
                assert map.class != null
                assert map.metaClass != null
            }
            test(new C())
        '''
    }

    // GROOVY-11223
    @Override
    void testMapPropertyAccess10() {
        assertScript """
            def map = new ${MapType.name}()
            map.foo = 11 // public setter
            assert map.foo == 11
            assert map.getFoo() == 11
            assert map.get('foo') == null
        """
        shouldFailWithMessages """
            def map = new ${MapType.name}()
            map.bar = 22 // protected setter
        """,
        "Method setBar is protected in ${MapType.name}"
        /*
        shouldFailWithMessages """
            def map = new ${MapType.name}()
            map.baz = 33 // package-private setter
        """,
        "Method setBaz is package-private in ${MapType.name}"
        */
    }
}
