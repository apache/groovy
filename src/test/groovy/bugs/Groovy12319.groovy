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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.GenericsType
import org.codehaus.groovy.ast.expr.ArgumentListExpression
import org.codehaus.groovy.ast.expr.ConstantExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.control.CompilationFailedException
import org.codehaus.groovy.control.GenericsVisitor
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test
import org.objectweb.asm.Opcodes

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

final class Groovy12319 {

    // =========================================================================
    // 1. Diamond <> on anonymous inner classes
    // =========================================================================

    @Test
    void testDiamondOnAnonymousInnerClass_Basic() {
        assertScript '''
            interface Processor<T> {
                T process(T val)
            }

            Processor<String> p = new Processor<>() {
                @Override
                String process(String val) {
                    return val.toUpperCase()
                }
            }

            assert p.process("hello") == "HELLO"
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_ExtendingClass() {
        assertScript '''
            abstract class BaseHolder<T> {
                T value
                BaseHolder(T value) {
                    this.value = value
                }
                abstract T transform()
            }

            BaseHolder<Integer> holder = new BaseHolder<>(42) {
                @Override
                Integer transform() {
                    return value * 2
                }
            }

            assert holder.transform() == 84
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_MultipleTypeParams() {
        assertScript '''
            interface BiTransformer<T, U, R> {
                R transform(T t, U u)
            }

            BiTransformer<String, Integer, String> transformer = new BiTransformer<>() {
                @Override
                String transform(String s, Integer i) {
                    return s + ":" + i
                }
            }

            assert transformer.transform("score", 100) == "score:100"
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_CompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            @CompileStatic
            class Tester {
                interface Handler<T> {
                    T handle(T input)
                }

                static String run() {
                    Handler<String> h = new Handler<>() {
                        @Override
                        String handle(String input) {
                            return "handled " + input
                        }
                    }
                    return h.handle("data")
                }
            }

            assert Tester.run() == "handled data"
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_TypeChecked() {
        assertScript '''
            import groovy.transform.TypeChecked

            @TypeChecked
            class Tester {
                static class Box<T> {
                    T item
                    Box(T item) { this.item = item }
                    T get() { return item }
                }

                static Integer run() {
                    Box<Integer> b = new Box<>(123) {
                        @Override
                        Integer get() {
                            return super.get() + 1
                        }
                    }
                    return b.get()
                }
            }

            assert Tester.run() == 124
        '''
    }

    // =========================================================================
    // 2. Qualified parameterized inner types ("rare" types)
    // =========================================================================

    @Test
    void testQualifiedParameterizedInnerType_VariableDeclaration() {
        assertScript '''
            class Outer<T> {
                class Inner<U> {
                    T t
                    U u
                    Inner(T t, U u) {
                        this.t = t
                        this.u = u
                    }
                    String display() {
                        return "" + t + "-" + u
                    }
                }
            }

            Outer<String>.Inner<Integer> x = new Outer<String>().new Inner<Integer>("val", 99)
            assert x.display() == "val-99"
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_FieldsAndMethods() {
        assertScript '''
            class Outer<T> {
                class Inner<U> {
                    T t
                    U u
                    Inner(T t, U u) { this.t = t; this.u = u }
                }
            }

            class Service {
                Outer<String>.Inner<Integer> field

                Outer<String>.Inner<Integer> echo(Outer<String>.Inner<Integer> param) {
                    return param
                }
            }

            Outer<String> outer = new Outer<>()
            Outer<String>.Inner<Integer> inner = outer.new Outer.Inner<Integer>("abc", 123)
            Service s = new Service(field: inner)
            assert s.echo(s.field).t == "abc"
            assert s.echo(s.field).u == 123
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_NestedGenerics() {
        assertScript '''
            class Outer<T> {
                class Inner<U> {
                    T t; U u
                    Inner(T t, U u) { this.t = t; this.u = u }
                }
            }

            List<Outer<String>.Inner<Integer>> list = new ArrayList<>()
            Outer<String> o = new Outer<>()
            list.add(o.new Outer.Inner<Integer>("item", 1))
            assert list.size() == 1
            assert list[0].t == "item"
            assert list[0].u == 1
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_CompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            class Outer<T> {
                class Inner<U> {
                    T t
                    U u
                    Inner(T t, U u) {
                        this.t = t
                        this.u = u
                    }
                    T getT() { return t }
                    U getU() { return u }
                }
            }

            @CompileStatic
            class Tester {
                static String test() {
                    Outer<String> outer = new Outer<String>()
                    Outer<String>.Inner<Integer> inner = outer.new Outer.Inner<Integer>("hello", 42)
                    return inner.getT() + ":" + inner.getU()
                }
            }

            assert Tester.test() == "hello:42"
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_RawInner() {
        assertScript '''
            class Outer<T> {
                class Inner {
                    T t
                    Inner(T t) { this.t = t }
                    T get() { return t }
                }
            }

            Outer<String>.Inner x = new Outer<String>().new Inner("test")
            assert x.get() == "test"
        '''
    }

    // =========================================================================
    // 3. Explicit type arguments on constructors, this() and super()
    // =========================================================================

    @Test
    void testConstructorTypeArguments_Basic() {
        assertScript '''
            class Box {
                String value
                <T> Box(T t) {
                    this.value = t.toString()
                }
            }

            Box b = new <String>Box("hello")
            assert b.value == "hello"

            Box b2 = new <Integer>Box(12345)
            assert b2.value == "12345"
        '''
    }

    @Test
    void testConstructorTypeArguments_SameCompilationUnitTypeResolved() {
        assertScript '''
            import groovy.transform.CompileStatic
            class Token {
                String id
                Token(String id) { this.id = id }
            }
            class Box {
                def t
                <T> Box(T t) { this.t = t }
            }
            @CompileStatic
            class Tester {
                static String run() {
                    ((Token) new <Token>Box(new Token('ok')).t).id
                }
            }
            assert Tester.run() == 'ok'
        '''
    }

    @Test
    void testConstructorTypeArguments_GenericClass() {
        assertScript '''
            class Pair<K> {
                K key
                String desc
                <V> Pair(K key, V val) {
                    this.key = key
                    this.desc = val.toString()
                }
            }

            Pair<String> p = new <Integer>Pair<String>("k1", 999)
            assert p.key == "k1"
            assert p.desc == "999"
        '''

        shouldFail CompilationFailedException, '''
            class Pair<K> {
                <V> Pair(K key, V val) {}
            }
            new <Integer>Pair<>("k2", 888)
        '''
    }

    @Test
    void testConstructorTypeArguments_InnerClass() {
        assertScript '''
            class Outer {
                class Inner {
                    String result
                    <T> Inner(T t) {
                        this.result = "inner:" + t
                    }
                }
            }

            Outer outer = new Outer()
            Outer.Inner inner = outer.new <String>Outer.Inner("custom")
            assert inner.result == "inner:custom"
        '''
    }

    @Test
    void testConstructorTypeArguments_ThisCall() {
        assertScript '''
            class Target {
                String text
                <T> Target(T t) {
                    this.text = "generic:" + t
                }
                Target() {
                    <String>this("defaultVal")
                }
            }

            Target t = new Target()
            assert t.text == "generic:defaultVal"
        '''
    }

    @Test
    void testConstructorTypeArguments_SuperCall() {
        assertScript '''
            class Base {
                String message
                <T> Base(T t) {
                    this.message = "base:" + t
                }
            }

            class Derived extends Base {
                Derived() {
                    <String>super("fromDerived")
                }
            }

            Derived d = new Derived()
            assert d.message == "base:fromDerived"
        '''
    }

    @Test
    void testConstructorTypeArguments_CompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            class Holder {
                String str
                <T> Holder(T t) {
                    this.str = String.valueOf(t)
                }
            }

            @CompileStatic
            class Tester {
                static String run() {
                    Holder h = new <String>Holder("static compiled")
                    return h.str
                }
            }

            assert Tester.run() == "static compiled"
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_ExtendingClass_CompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            abstract class BaseHolder<T> {
                T value
                BaseHolder(T value) {
                    this.value = value
                }
                abstract T transform()
            }

            @CompileStatic
            class Tester {
                static Integer run() {
                    BaseHolder<Integer> holder = new BaseHolder<>(42) {
                        @Override
                        Integer transform() {
                            return value * 2
                        }
                    }
                    return holder.transform()
                }
            }

            assert Tester.run() == 84
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_DefVariable() {
        assertScript '''
            interface Formatter<T> {
                String format(T t)
            }

            def f = new Formatter<String>() {
                @Override
                String format(String s) {
                    return "fmt:" + s
                }
            }
            assert f.format("hello") == "fmt:hello"
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_Chained() {
        assertScript '''
            import groovy.transform.CompileStatic

            class Level1<A> {
                A a
                Level1(A a) { this.a = a }

                class Level2<B> {
                    B b
                    Level2(B b) { this.b = b }
                    String summarize() {
                        return "" + a + ":" + b
                    }
                }
            }

            @CompileStatic
            class Tester {
                static String run() {
                    Level1<String> l1 = new Level1<String>("hello")
                    Level1<String>.Level2<Integer> l2 = l1.new Level1.Level2<Integer>(42)
                    return l2.summarize()
                }
            }

            assert Tester.run() == "hello:42"
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_NestedInTypeArgument() {
        assertScript '''
            class Outer<T> {
                class Inner<U> {
                    T t
                    U u
                    Inner(T t, U u) {
                        this.t = t
                        this.u = u
                    }
                }
            }

            List<Outer<String>.Inner<Integer>> list = new ArrayList<Outer<String>.Inner<Integer>>()
            Outer<String> o = new Outer<String>()
            list.add(o.new Outer.Inner<Integer>("hello", 123))
            assert list.get(0).t == "hello"
            assert list.get(0).u == 123
        '''
    }

    @Test
    void testConstructorTypeArguments_MultipleTypes() {
        assertScript '''
            class MultiHolder {
                String desc
                <K, V> MultiHolder(K k, V v) {
                    this.desc = k + " -> " + v
                }
            }

            MultiHolder m = new <String, Integer>MultiHolder("key", 100)
            assert m.desc == "key -> 100"
        '''
    }

    @Test
    void testConstructorTypeArguments_CompileStatic_ThisAndSuper() {
        assertScript '''
            import groovy.transform.CompileStatic

            class BaseClass {
                String value
                <T> BaseClass(T t) {
                    this.value = "base:" + t
                }
            }

            @CompileStatic
            class ChildClass extends BaseClass {
                ChildClass(int n) {
                    <Integer>super(n)
                }
                ChildClass() {
                    <String>this("default-child")
                }
                <T> ChildClass(T t) {
                    <T>super(t)
                }
            }

            assert new ChildClass(123).value == "base:123"
            assert new ChildClass().value == "base:default-child"
            assert new ChildClass("explicit").value == "base:explicit"
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_MethodArgument() {
        assertScript '''
            interface Processor<T> {
                T process(T val)
            }

            static String apply(Processor<String> p) {
                p.process('ok')
            }

            assert apply(new Processor<>() {
                @Override
                String process(String val) { val.toUpperCase() }
            }) == 'OK'
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_ThreeLevels() {
        assertScript '''
            class A<T> {
                T a
                A(T a) { this.a = a }
                class B<U> {
                    U b
                    B(U b) { this.b = b }
                    class C<V> {
                        V c
                        C(V c) { this.c = c }
                        String all() { '' + a + ':' + b + ':' + c }
                    }
                }
            }

            A<String> a = new A<String>('x')
            A<String>.B<Integer> b = a.new A.B<Integer>(1)
            A<String>.B<Integer>.C<Long> c = b.new A.B.C<Long>(2L)
            assert c.all() == 'x:1:2'
        '''
    }

    @Test
    void testConstructorTypeArguments_NestedThisCall() {
        assertScript '''
            class Box {
                String value
                <T> Box(T t) { this.value = String.valueOf(t) }
                Box(int ignored) { <Double>this(1.5d) }
            }

            assert new Box(0).value == '1.5'
        '''
    }

    @Test
    void testConstructorTypeArgumentsAndDiamondRejectedOnInterfaceAIC() {
        shouldFail CompilationFailedException, '''
            interface Processor<T> {
                T process(T val)
            }
            new <String>Processor<>() {
                String process(String val) { val }
            }
        '''
    }

    @Test
    void testConstructorTypeArgumentsArityMismatchCompileStatic() {
        shouldFail CompilationFailedException, '''
            import groovy.transform.CompileStatic
            class Box {
                <T> Box(T t) {}
            }
            @CompileStatic
            void m() {
                new <String, Integer>Box("x")
            }
            m()
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_MethodArgument_CompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            interface Processor<T> {
                T process(T val)
            }

            @CompileStatic
            class Tester {
                static String apply(Processor<String> p) {
                    p.process('ok')
                }
                static String run() {
                    apply(new Processor<>() {
                        @Override
                        String process(String val) { val.toUpperCase() }
                    })
                }
            }

            assert Tester.run() == 'OK'
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_MethodArgument_NestedGenerics() {
        assertScript '''
            interface Box<T> {
                List<T> items()
            }

            static List<String> take(Box<String> b) {
                b.items()
            }

            assert take(new Box<>() {
                @Override
                List<String> items() { ['a'] }
            }) == ['a']
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_MethodArgument_NestedGenerics_CompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic

            interface Box<T> {
                List<T> items()
            }

            @CompileStatic
            class Tester {
                static List<String> take(Box<String> b) {
                    b.items()
                }
                static List<String> run() {
                    take(new Box<>() {
                        @Override
                        List<String> items() { ['a'] }
                    })
                }
            }

            assert Tester.run() == ['a']
        '''
    }

    @Test
    void testQualifiedParameterizedInnerType_UnparameterizedMiddle() {
        assertScript '''
            class A<T> {
                T a
                A(T a) { this.a = a }
                class B {
                    class C<U> {
                        U c
                        C(U c) { this.c = c }
                        String all() { '' + a + ':' + c }
                    }
                }
            }

            A<String> a = new A<String>('x')
            A<String>.B b = a.new A.B()
            A<String>.B.C<Integer> c = b.new A.B.C<Integer>(7)
            assert c.all() == 'x:7'
        '''
    }

    @Test
    void testRareTypeInstanceCreationInsideOuter() {
        assertScript '''
            class Outer<T> {
                class Inner<U> {
                    U u
                    Inner(U u) { this.u = u }
                }
                Inner<Integer> make() {
                    return new Outer<T>.Inner<Integer>(7)
                }
            }

            assert new Outer<String>().make().u == 7
        '''
    }

    @Test
    void testDiamondOnAnonymousInnerClass_ArrayTypeParameter() {
        assertScript '''
            interface ArrProc<T> {
                T[] process(T[] val)
            }
            ArrProc<String> p = new ArrProc<>() {
                @Override
                String[] process(String[] val) { val }
            }
            assert p.process(['a', 'b'] as String[]).join('-') == 'a-b'
        '''
    }

    @Test
    void testConstructorCallExpressionTypeArgumentsText() {
        def stringType = new GenericsType(ClassHelper.STRING_TYPE)
        def cce = new ConstructorCallExpression(ClassHelper.make('Box'), new ArgumentListExpression(new ConstantExpression('x')))
        assert !cce.isUsingGenerics()
        cce.setGenericsTypes([stringType] as GenericsType[])
        assert cce.isUsingGenerics()
        assert cce.text.startsWith('new <java.lang.String>Box')

        def thisCall = new ConstructorCallExpression(ClassNode.THIS, new ArgumentListExpression())
        thisCall.setGenericsTypes([stringType] as GenericsType[])
        assert thisCall.text.contains('<java.lang.String>this')

        def superCall = new ConstructorCallExpression(ClassNode.SUPER, new ArgumentListExpression())
        superCall.setGenericsTypes([stringType] as GenericsType[])
        assert superCall.text.contains('<java.lang.String>super')

        def transformed = cce.transformExpression { it }
        assert transformed instanceof ConstructorCallExpression
        assert transformed.isUsingGenerics()
        assert transformed.genericsTypes.length == 1

        def emptyArgs = new ConstructorCallExpression(ClassHelper.make('Box'), new ArgumentListExpression())
        emptyArgs.setGenericsTypes(GenericsType.EMPTY_ARRAY)
        assert !emptyArgs.isUsingGenerics()
        assert emptyArgs.text.startsWith('new Box')
    }

    @Test
    void testValueClassPropertyRemainsLegal() {
        assertScript '''
            def values = [1, 'x']
            assert values*.class == [Integer, String]
            assert values[0].class == Integer
        '''
    }

    @Test
    void testTypeParameterArrayCreationNamesTypeVariable() {
        def err = shouldFail CompilationFailedException, '''
            class Stack<T> {
                T[] items
                Stack(int n) { items = new T[n] }
            }
        '''
        assert err.message.contains('generic array creation of T')
        assert !err.message.contains('of Object')
    }

    @Test
    void testConstructorTypeArgumentsWithFunctionalArgumentCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Supplier

            class Box {
                String s
                <T> Box(Supplier<String> f) {
                    this.s = f.get()
                }
            }

            @CompileStatic
            class Tester {
                static String run() {
                    new <Integer>Box({ -> 'ok' }).s
                }
            }

            assert Tester.run() == 'ok'
        '''
    }

    @Test
    void testCompileStaticRareTypeVariableAndMethodCall() {
        assertScript '''
            import groovy.transform.CompileStatic
            class Outer<T> {
                class Inner<U> {
                    U u
                    Inner(U u) { this.u = u }
                    U get() { u }
                }
            }
            @CompileStatic
            class Tester {
                static Integer run() {
                    Outer<String>.Inner<Integer> inner = new Outer<String>().new Inner<Integer>(9)
                    def copy = inner
                    copy.get()
                }
            }
            assert Tester.run() == 9
        '''
    }

    @Test
    void testInvalidDiamondOnFieldMentionsDiamond() {
        def fieldType = ClassHelper.LIST_TYPE.getPlainNodeReference()
        fieldType.genericsTypes = GenericsType.EMPTY_ARRAY
        def cn = new ClassNode('C', Opcodes.ACC_PUBLIC, ClassHelper.OBJECT_TYPE)
        cn.addField(new FieldNode('f', Opcodes.ACC_PUBLIC, fieldType, cn, null))
        def source = SourceUnit.create('C.groovy', '', 0) // GROOVY-12306: unlimited tolerance, the visitor must not bail out
        new GenericsVisitor(source).visitClass(cn)
        assert source.errorCollector.errorCount > 0
        assert source.errorCollector.errors.any { it.cause.message.contains('invalid Diamond') }
    }

    @Test
    void testMultiDimensionalArrayCreationIsReifiable() {
        assertScript '''
            def a = new String[0][0]
            assert a.length == 0
            def b = new String[][]{ new String[]{'a'} }
            assert b[0][0] == 'a'
        '''
    }

    @Test
    void testPrimitiveArrayCreatedName() {
        assertScript '''
            def a = new int[2]
            assert a.length == 2
        '''
    }

    @Test
    void testMethodLevelTypeCheckedAnonymousClass() {
        assertScript '''
            interface Processor<T> {
                T process(T val)
            }
            class Host {
                @groovy.transform.TypeChecked
                String run() {
                    Processor<String> p = new Processor<>() {
                        @Override
                        String process(String val) { val.toUpperCase() }
                    }
                    p.process('ok')
                }
            }
            assert new Host().run() == 'OK'
        '''
    }

    @Test
    void testNamedArgConstructorCompileStatic() {
        assertScript '''
            import groovy.transform.CompileStatic
            class Person {
                String name
            }
            @CompileStatic
            class Tester {
                static String run() {
                    Person p = new Person(name: 'Ada')
                    p.name
                }
            }
            assert Tester.run() == 'Ada'
        '''
    }

    @Test
    void testSpreadConstructorArgumentCompileStatic() {
        def err = shouldFail CompilationFailedException, '''
            import groovy.transform.CompileStatic
            @CompileStatic
            void m(List<String> args) {
                new ArrayList<String>(*args)
            }
        '''
        assert err.message != null
    }

    @Test
    void testGenericClassConstructorWithSamAndConstructorTypeArguments() {
        assertScript '''
            import groovy.transform.CompileStatic
            import java.util.function.Supplier

            class Box<A> {
                String s
                <T> Box(Supplier<String> f) {
                    this.s = f.get()
                }
            }

            @CompileStatic
            class Tester {
                static String run() {
                    def a = new <Integer>Box<String>({ -> 'ok' }).s
                    def b = new Box<String>({ -> 'ok' }).s
                    a + b
                }
            }
            assert Tester.run() == 'okok'
        '''
    }

    @Test
    void testDiamondAicConstructorArityMismatchFallsBack() {
        assertScript '''
            import groovy.transform.CompileStatic
            abstract class Base<T> {
                T value
                Base(int a, int b) { this.value = null }
                abstract T get()
            }
            @CompileStatic
            class Tester {
                static String run() {
                    Base<String> b = new Base<String>(1, 2) {
                        @Override String get() { 'x' }
                    }
                    b.get()
                }
            }
            assert Tester.run() == 'x'
        '''
    }

    @Test
    void testDiamondInterfaceAicInsideInstanceMethod() {
        assertScript '''
            import groovy.transform.CompileStatic
            @CompileStatic
            class Outer {
                interface I<T> { T n() }
                String run() {
                    I<String> i = new I<>() {
                        @Override String n() { 'x' }
                    }
                    i.n()
                }
            }
            assert new Outer().run() == 'x'
        '''
    }

    @Test
    void testUnresolvedRareOuterTypeRejected() {
        def err = shouldFail CompilationFailedException, '''
            UnknownOuter<String>.Inner x
        '''
        assert err.message.toLowerCase().contains('unable to resolve') || err.message.contains('UnknownOuter')
    }

    @Test
    void testGrabGeneratesClassLoaderPlainReference() {
        def err = shouldFail '''
            @Grab(group='org.apache.groovy', module='does-not-exist', version='0.0.0')
            class GrabDummy12319 {
                def x() { 1 }
            }
            new GrabDummy12319()
        '''
        assert err != null
    }

    @Test
    void testStaticSuperPropertyAndMethodPlainNodeReference() {
        assertScript '''
            class Base<T> {
                static String NAME = 'base'
                static String m() { 'm' }
            }
            class Sub extends Base<String> {
                static String read() { super.NAME + super.m() }
            }
            assert Sub.read() == 'basem'
        '''
    }
}

