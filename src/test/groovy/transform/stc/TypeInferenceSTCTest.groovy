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

import groovy.test.NotYetImplemented
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.tools.WideningCategories
import org.codehaus.groovy.classgen.GeneratorContext
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.transform.stc.StaticTypesMarker

/**
 * Unit tests for static type checking : type inference.
 */
class TypeInferenceSTCTest extends StaticTypeCheckingTestCase {

    void testStringToInteger() {
        assertScript '''
            def name = "123" // we want type inference
            name.toInteger() // toInteger() is defined by DGM
        '''
    }

    // GROOVY-9935
    void testIntegerToNumber() {
        ['def', 'int', 'Integer', 'BigInteger'].each { type ->
            assertScript """
                Number f() {
                    $type n = 10
                    return n
                }
                assert f() == 10
            """
        }
    }

    void testGStringMethods() {
        assertScript '''
            def myname = 'Cedric'
            "My upper case name is ${myname.toUpperCase()}"
            println "My upper case name is ${myname}".toUpperCase()
        '''
    }

    void testDynamicMethodWithinTypeCheckedClass() {
        assertScript '''
            import groovy.transform.*

            class C {
                String m(String s) {
                    generateMarkup(s.toUpperCase())
                }

                // MarkupBuilder is dynamic so skip type-checking
                @TypeChecked(TypeCheckingMode.SKIP)
                String generateMarkup(String s) {
                    def sw = new StringWriter()
                    def mb = new groovy.xml.MarkupBuilder(sw)
                    mb.html {
                        body {
                            div s
                        }
                    }
                    sw.toString()
                }
            }

            def c = new C()
            def xml = c.m('x')
            // TODO: check XML
        '''
    }

    void testInstanceOf1() {
        assertScript '''
            Object o
            if (o instanceof String) {
                o.toUpperCase()
            }
        '''
    }

    void testInstanceOf2() {
        assertScript '''
            Object o
            if (o instanceof String) {
                if (true) {
                    o.toUpperCase()
                }
            }
        '''
    }

    void testInstanceOf3() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
               o.toUpperCase()
            }
            o.toUpperCase() // ensure that type information is reset
        ''',
        'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testInstanceOf4() {
        shouldFailWithMessages '''
            Object o
            if (o instanceof String) {
               o.toUpperCase()
            } else {
                o.toUpperCase() // ensure that type information is reset
            }
        ''',
        'Cannot find matching method java.lang.Object#toUpperCase()'
    }

    void testInstanceOf5() {
        assertScript '''
            class A {
                void bar() {
                }
            }
            class B {
                void bar() {
                }
            }

            void test(o) {
                if (o instanceof A) {
                    o.bar()
                }
                if (o instanceof B) {
                    o.bar()
                }
            }
            test(new A())
            test(new B())
        '''
    }

    // GROOVY-9953
    void testInstanceOf6() {
        assertScript '''
            class A {
            }
            A test(Object x) {
                if (x instanceof A) {
                    def y = x
                    return y
                } else {
                    new A()
                }
            }
            new A().with { assert test(it) === it }
        '''
    }

    // GROOVY-9454
    void testInstanceOf7() {
        assertScript '''
            interface Face {
            }
            class Impl implements Face {
                String something
            }
            class Task<R extends Face> implements java.util.concurrent.Callable<String> {
                R request

                @Override
                String call() {
                    if (request instanceof Impl) {
                        def thing = request.something // No such property: something for class: R
                        def lower = thing.toLowerCase()
                    } else {
                        // ...
                        return null
                    }
                }
            }
            def task = new Task<Impl>(request: new Impl(something: 'Hello World'))
            assert task.call() == 'hello world'
        '''
    }

    // GROOVY-10667
    void testInstanceOf8() {
        assertScript '''
            trait Tagged {
                String tag
            }
            class TaggedException extends Exception implements Tagged {
            }

            static void doSomething1(Exception e) {
                if (e instanceof Tagged) {
                    //println e.tag
                    doSomething2(e) // Cannot find matching method #doSomething2(Tagged)
                }
            }
            static void doSomething2(Exception e) {
            }

            doSomething1(new TaggedException(tag:'Test'))
        '''
    }

    // GROOVY-7971
    void testInstanceOf9() {
        assertScript '''
            import groovy.json.JsonOutput
            def test(value) {
                def out = new StringBuilder()
                def isString = value.class == String
                if (isString || value instanceof Map) {
                    out.append(JsonOutput.toJson(value))
                }
                return out.toString()
            }
            def string = test('two')
            assert string == '"two"'
        '''
    }

    // GROOVY-10096
    @NotYetImplemented
    void testInstanceOf10() {
        shouldFailWithMessages '''
            class Foo {
                void foo() {
                }
            }
            class Bar extends Foo {
                void bar() {
                }
            }
            static Bar baz(Foo foo) {
                (false || foo instanceof Bar) ? foo : new Bar()
            }
        ''',
        'Cannot return value of type Foo for method returning Bar'
    }

    // GROOVY-11007
    void testInstanceOf11() {
        assertScript '''
            interface I {
                CharSequence getCharSequence()
            }

            void accept(CharSequence cs) {   }

            void test(I i) {
                i.with {
                    if (charSequence instanceof String) {
                        charSequence.toUpperCase()
                        accept(charSequence)
                    }
                }
            }

            test({ -> 'works' } as I)
        '''
    }

    // GROOVY-11290
    void testInstanceOf12() {
        assertScript '''
            def test(List<String> list) {
                if (list instanceof List) {
                    (list*.toLowerCase()).join()
                }
            }

            String result = test(['foo', 'bar'])
            assert result == 'foobar'
        '''
    }

    // GROOVY-5226
    void testNestedInstanceOf1() {
        assertScript '''
            Object o = "foo"
            if (o instanceof Object) {
                if (o instanceof String) {
                    o.toUpperCase()
                }
            }
        '''
    }

    // GROOVY-5226
    void testNestedInstanceOf2() {
        assertScript '''
            Object o = "foo"
            if (o instanceof String) {
                if (o instanceof Object) {
                    o.toUpperCase()
                }
            }
        '''
    }

    // GROOVY-11290
    void testNestedInstanceOf3() {
        assertScript '''
            Object o = null
            if (o instanceof Closeable) {
                if (o instanceof Cloneable) {
                    o.close()
                }
            }
        '''
    }

    void testNestedInstanceOf4() {
        assertScript '''
            Object o = [1,2] as Number[]
            if (o instanceof Object[]) {
                if (o instanceof Number[]) {
                    o[0].intValue()
                }
            }
        '''
    }

    void testNestedInstanceOf5() {
        assertScript '''
            class A {
               int foo() { 1 }
            }
            class B {
               int foo2() { 2 }
            }

            def o = new A()
            int result = o instanceof A ? o.foo() : (o instanceof B ? o.foo2() : 3)
            assert result == 1
            o = new B()
            result = o instanceof A ? o.foo() : (o instanceof B ? o.foo2() : 3)
            assert result == 2
            o = new Object()
            result = o instanceof A ? o.foo() : (o instanceof B ? o.foo2() : 3)
            assert result == 3
        '''
    }

    void testMultipleInstanceOf1() {
        assertScript '''
            class A {
                void bar() {
                }
            }
            class B {
                void bar() {
                }
            }

            void test(o) {
                if (o instanceof A || o instanceof B) {
                    o.bar()
                }
            }
            test(new A())
            test(new B())
        '''
    }

    void testMultipleInstanceOf2() {
        assertScript '''
            int cardinality(o) {
                (o instanceof List || o instanceof Map ? o.size() : 1)
            }
            assert cardinality('') == 1
            assert cardinality(['foo','bar']) == 2
            assert cardinality([foo:'',bar:'']) == 2
        '''
    }

    void testMultipleInstanceOf3() {
        assertScript '''
            boolean empty(o) {
                (o instanceof List || o instanceof Map) && o.isEmpty()
            }
            assert !empty('')
            assert  empty([])
            assert  empty([:])
            assert !empty(['foo'])
            assert !empty([foo:null])
        '''
    }

    // GROOVY-8965
    void testMultipleInstanceOf4() {
        ['o', '((Number) o)'].each {
            assertScript """
                def foo(o) {
                    if (o instanceof Integer || o instanceof Double) {
                        ${it}.floatValue() // ClassCastException
                    }
                }
                def bar = foo(1.1d)
                assert bar == 1.1f
                def baz = foo(1)
                assert baz == 1
            """
        }
    }

    void testMultipleInstanceOf5() {
        assertScript '''
            void test(thing) {
                if (thing instanceof Deque) {
                    thing.addFirst(1) // 'addFirst' only in Deque
                } else if (thing instanceof Stack) {
                    thing.addElement(2) // 'addElement' only in Stack
                }
                if (thing instanceof Deque || thing instanceof Stack) {
                    assert thing.peek() in 1..2 // 'peek' in Deque and Stack but not LUB
                }
            }
            test(new Stack())
            test(new ArrayDeque())
        '''
    }

    // GROOVY-10668
    void testMultipleInstanceOf6() {
        ['(value as String)', 'value.toString()'].each { string ->
            assertScript """
                def toArray(Object value) {
                    def array
                    if (value instanceof List)
                        array = value.toArray()
                    else if (value instanceof String || value instanceof GString)
                        array = ${string}.split(',')
                    else
                        throw new Exception('not supported')

                    return array
                }
                toArray([1,2,3])
                toArray('1,2,3')
            """
        }
    }

    // GROOVY-8828
    void testMultipleInstanceOf7() {
        assertScript '''
            interface Foo { }
            interface Bar { String name() }

            Map<String, Foo> map = [:]
            map.values().each { foo ->
                if (foo instanceof Bar) {
                    String name = foo.name() // method available through Bar
                    map.put(name, foo)       // second parameter expects Foo
                }
            }
        '''
    }

    // GROOVY-6429
    void testNotInstanceof1() {
        String types = '''
            class C {
            }
            class D extends C {
                def foo() { }
            }
        '''
        for (test in ['!(x instanceof D)', 'x !instanceof D']) {
            assertScript types + """
                void p(x) {
                    if ($test) {
                        // ...
                    } else {
                        x.foo()
                    }
                }
                p(new C())
                p(new D())
            """
            assertScript types + """
                void p(x) {
                    if ($test) {
                        return
                    }
                    assert x instanceof D
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        def ecks = node.rightExpression.objectExpression
                        def type = ecks.getNodeMetaData(INFERRED_TYPE)
                        assert type.text == 'D' // not <UnionType:D+D>
                    })
                    def bar = x.foo()
                }
                p(new C())
                p(new D())
            """
        }
    }

    // GROOVY-8523
    void testNotInstanceof2() {
        for (test in ['!(x instanceof Runnable)', 'x !instanceof Runnable']) {
            assertScript """
                class Test {
                    public static int result = 0

                    static void p(x) {
                        if ($test) {
                            result = 1
                            return
                        }
                        q(x)
                    }

                    private static void q(Runnable r) {
                        result = 2
                    }
                }
                Test.p({->} as Runnable)
                assert Test.result == 2
                Test.p([''])
                assert Test.result == 1
            """
        }
    }

    // GROOVY-8523
    void testNotInstanceOf3() {
        for (test in ['!(x instanceof List)', 'x !instanceof List']) {
            assertScript """
                class Test {
                    public static int result = 0

                    static void p(x) {
                        if (x !instanceof Runnable) {
                            result = 1
                            return
                        }
                        if ($test) {
                            result = 2
                            return
                        }
                        q(x)
                    }

                    private static void q(Runnable r) { // and List
                        result = 3
                    }
                }
                // TODO: List and Runnable
                Test.p({->} as Runnable)
                assert Test.result == 2
                Test.p([''])
                assert Test.result == 1
            """
        }
    }

    // GROOVY-9455
    void testNotInstanceOf4() {
        for (test in ['!(x instanceof String)', 'x !instanceof String']) {
            shouldFailWithMessages """
                void p(x) {
                    if ($test) {
                        x.toUpperCase()
                    }
                }
            """,
            'Cannot find matching method java.lang.Object#toUpperCase()'
        }
    }

    // GROOVY-9931
    void testNotInstanceof5() {
        for (test in ['!(x instanceof Number)', 'x !instanceof Number']) {
            assertScript """
                Number f(x) {
                    if ($test) {
                        return null
                    } else {
                        return x
                    }
                }
                assert f(42) == 42
                assert f('') == null
            """
        }
    }

    // GROOVY-8412
    void testNotInstanceof6() {
        for (test in ['!(x instanceof Number)', 'x !instanceof Number']) {
            assertScript """
                Number f(x) {
                    return ($test) ? null : x
                }
                assert f(42) == 42
                assert f('') == null
            """
            assertScript """
                Number f(x) {
                    return !!($test) ? null : x // multiple negation
                }
                assert f(42) == 42
                assert f('') == null
            """
        }
    }

    // GROOVY-10217
    void testInstanceOfThenSubscriptOperator() {
        assertScript '''
            void test(Object o) {
                if (o instanceof List) {
                    assert o[0] == 1
                    def x = (List) o
                    assert x[0] == 1
                }
            }
            test([1])
        '''
    }

    void testInstanceOfInferenceWithImplicitIt() {
        assertScript '''
        ['a', 'b', 'c'].each {
            if (it instanceof String) {
                println it.toUpperCase()
            }
        }
        '''
    }

    void testInstanceOfTypeInferenceWithDef() {
        assertScript '''
            def profile = ['Guillaume', 34, true]
            def item = profile[0]
            if (item instanceof String) {
                println item.toUpperCase()
            }
        '''
    }

    void testInstanceOfTypeInferenceWithoutDef() {
        assertScript '''
            def profile = ['Guillaume', 34, true]
            if (profile[0] instanceof String) {
                println profile[0].toUpperCase()
            }
        '''
    }

    void testInstanceOfInferenceWithProperty1() {
        assertScript '''
            class A {
                int x
            }
            def a
            if (a instanceof A) {
                a.x = 2
            }
        '''
    }

    void testInstanceOfInferenceWithProperty2() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            if (a instanceof A) {
                a.x = '2'
            }
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testInstanceOfInferenceWithMissingProperty() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a
            if (a instanceof A) {
                a.y = 2
            }
        ''',
        'No such property: y for class: A'
    }

    // GROOVY-9967
    void testInstanceOfInferenceWithSubclassProperty() {
        assertScript '''
            class A {
                int i
            }
            class B extends A {
                String s = 'foo'
            }

            String scenario1(x) {
                (x instanceof String) ? x.toLowerCase() : 'bar'
            }
            String scenario2(B x) {
                x.s
            }
            String scenario2a(B x) {
                x.getS()
            }
            String scenario3(B x) {
                (x instanceof B) ? x.s : 'bar'
            }
            String scenario3a(B x) {
                (x instanceof B) ? x.getS() : 'bar'
            }
            String scenario4(A x) {
                (x instanceof B) ? x.s : 'bar' // Access to A#s is forbidden
            }
            String scenario4a(A x) {
                (x instanceof B) ? x.getS() : 'bar'
            }

            assert scenario1(null) == 'bar'
            assert scenario1('Foo') == 'foo'
            assert scenario2(new B()) == 'foo'
            assert scenario2a(new B()) == 'foo'
            assert scenario3(new B()) == 'foo'
            assert scenario3a(new B()) == 'foo'

            assert scenario4(new A()) == 'bar'
            assert scenario4(new B()) == 'foo'
            assert scenario4a(new A()) == 'bar'
            assert scenario4a(new B()) == 'foo'
        '''
    }

    void testShouldNotAllowDynamicVariable() {
        shouldFailWithMessages '''
            String name = 'Guillaume'
            println naamme
        ''',
        'The variable [naamme] is undeclared'
    }

    void testShouldNotFailWithWith() {
        assertScript '''
            class A {
                int x
            }
            def a = new A()
            a.with {
                x = 2 // should be recognized as a.x at compile time
            }
            assert a.x == 2
        '''
    }

    void testShouldFailWithWith() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            a.with {
                x = '2' // should be recognized as a.x at compile time and fail because of wrong type
            }
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testShouldNotFailWithWithTwoClasses() {
        // we must make sure that type inference engine in this case
        // takes the same property as at runtime
        assertScript '''
            class A {
                int x
            }
            class B {
                String x
            }
            def a = new A()
            def b = new B()
            a.with {
                b.with {
                    x = '2' // should be recognized as b.x at compile time
                }
            }
            assert a.x == 0
            assert b.x == '2'
        '''
    }

    void testShouldNotFailWithWithAndImplicitIt() {
        assertScript '''
            class A {
                int x
            }
            def a = new A()
            a.with {
                it.x = 2 // should be recognized as a.x at compile time
            }
            assert a.x == 2
        '''
    }

    void testShouldNotFailWithWithAndExplicitIt() {
        assertScript '''
            class A {
                int x
            }
            def a = new A()
            a.with { it ->
                it.x = 2 // should be recognized as a.x at compile time
            }
            assert a.x == 2
        '''
    }

    void testShouldFailWithWithAndWrongExplicitIt() {
        shouldFailWithMessages '''
            class A {
                int x
            }
            def a = new A()
            a.with { String it ->
                it.x = 2 // should be recognized as a.x at compile time
            }
        ''',
        'Expected type A for closure parameter: it'
    }

    void testShouldNotFailWithInheritanceAndWith() {
         assertScript '''
             class A {
                 int x
                 void method() { println x }
             }
             class B extends A {
             }
             def b = new B()
             b.with {
                 x = 2 // should be recognized as b.x at compile time
             }
             assert b.x == 2
         '''
    }

    void testCallMethodInWithContext() {
        assertScript '''
            class A {
                int method() { return 1 }
            }
            def a = new A()
            a.with {
                method()
            }
        '''
    }

    void testCallMethodInWithContextAndShadowing() {
       // make sure that the method which is found in 'with' is actually the one from class A
       // which returns a String
       assertScript '''
            class A {
                String method() { return 'Cedric' }
            }

            int method() { 1 }

            def a = new A()
            a.with {
                method().toUpperCase()
            }
        '''

       // check that if we switch signatures, it fails
       shouldFailWithMessages '''
            class A {
                int method() { 1 }
            }

            String method() { 'Cedric' }

            def a = new A()
            a.with {
                method().toUpperCase()
            }
        ''',
        'Cannot find matching method int#toUpperCase()'
   }

    void testDeclarationTypeInference() {
        MethodNode method
        config.addCompilationCustomizers(new CompilationCustomizer(CompilePhase.CLASS_GENERATION) {
            @Override
            void call(SourceUnit source, GeneratorContext context, ClassNode classNode) {
                method = classNode.methods.find { it.name == 'method' }
            }

        })
        assertScript '''
            void method() {
                def o
                o = 1
                o = 'String'
            }
        '''
        def inft = method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE)
        assert inft instanceof WideningCategories.LowestUpperBoundClassNode
        [Comparable, Serializable].each {
            assert ClassHelper.make(it) in inft.interfaces
        }

        assertScript '''
            void method() {
                def o
                o = 1
                o = 2
            }
        '''
        assert method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE) == ClassHelper.int_TYPE

        assertScript '''
            void method() {
                def o
                o = 1L
                o = 2
            }
        '''
        inft = method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE)
        assert inft  == ClassHelper.long_TYPE

        assertScript '''
            void method() {
                def o
                o = new HashSet()
                o = new LinkedHashSet()
            }
        '''
        assert method.code.statements[0].expression.leftExpression.getNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE) == ClassHelper.make(HashSet)
    }

    void testChooseMethodWithTypeInference() {
        assertScript '''
            void method(Object o) { println 'Object' }
            void method(int i) { println 'int' }
            def obj = 1
            method(obj)
        '''
    }

    void testStarOperatorOnMap() {
        assertScript '''
            List keys = [x:1,y:2,z:3]*.key
            List values = [x:1,y:2,z:3]*.value
        '''
    }

    void testStarOperatorOnMap2() {
        assertScript '''
            List keys = [x:1,y:2,z:3]*.key
            List values = [x:'1',y:'2',z:'3']*.value
            keys*.toUpperCase()
            values*.toUpperCase()
        '''

        shouldFailWithMessages '''
            List values = [x:1,y:2,z:3]*.value
            values*.toUpperCase()
        ''',
        'Cannot find matching method java.lang.Integer#toUpperCase()'
    }

    void testStarOperatorOnMap3() {
        assertScript '''
            def keys = [x:1,y:2,z:3]*.key
            def values = [x:'1',y:'2',z:'3']*.value
            keys*.toUpperCase()
            values*.toUpperCase()
        '''

        shouldFailWithMessages '''
            def values = [x:1,y:2,z:3]*.value
            values*.toUpperCase()
        ''',
        'Cannot find matching method java.lang.Integer#toUpperCase()'
    }

    void testStarOperatorOnMap4() {
        assertScript '''
            def map = [x:1,y:2,z:3]
            map*.value = 0

            assert map*.value == [0,0,0]
        '''

        assertScript '''
            Map<String,? extends Object> map = [x:1,y:2,z:3]
            map*.value = 0

            assert map*.value == [0,0,0]
        '''

        // GROOVY-10325
        assertScript '''
            Map<String,Object> map = [x:1,y:2,z:3]
            map*.value = 0 // was: Cannot assign List<Integer> to List<Object>

            assert map*.value == [0,0,0]
        '''

        shouldFailWithMessages '''
            [x:1,y:2,z:3]*.value = ""
        ''',
        'Cannot assign java.util.List<java.lang.String> to: java.util.List<java.lang.Integer>'

        // GROOVY-10326
        shouldFailWithMessages '''
            [x:1,y:2,z:3]*.key = null
        ''',
        'Cannot set read-only property: key'
    }

    // GROOVY-7247
    void testStarOperatorOnMapKey() {
        assertScript '''
            Map<String, Integer> map = [*:[A:1], *:[B:2]]
            assert map == [A:1, B:2]
        '''

        assertScript '''
            Map<String, Integer> one = [A:1]
            Map<String, Integer> two = [B:2]
            def map = [*:one, *:two]
            assert map == [A:1, B:2]
        '''

        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.genericsTypes[0].type == STRING_TYPE
                assert type.genericsTypes[1].type != Integer_TYPE
                assert type.genericsTypes[1].type.isDerivedFrom(Number_TYPE)
            })
            def map = [*:[A:1], *:[B:2.3]]
        '''

        shouldFailWithMessages '''
            Map<String, Integer> map = [*:[A:1], *:[B:2.3]]
        ''',
        'Cannot assign java.util.LinkedHashMap<java.lang.String, java.lang.Number',' to: java.util.Map<java.lang.String, java.lang.Integer>'
    }

    void testFlowTypingWithStringVariable() {
        assertScript '''
            String s = new Object() // anything assignable to String
            s.toUpperCase()
        '''
    }

    void testDefTypeAfterLongThenIntAssignments() {
        assertScript '''
            def o
            o = 1L
            o = 2
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.rightExpression.accessedVariable.getNodeMetaData(DECLARATION_INFERRED_TYPE) == long_TYPE
            })
            def z = o
        '''
    }

    // GROOVY-5519
    void testInferThrowable() {
        assertScript '''
            try {
                throw new RuntimeException('ok')
            } catch (e) {
                handleError(e)
            }
            void handleError(Throwable e) {
                assert e.message == 'ok'
            }
        '''
    }

    // GROOVY-5522
    void testTypeInferenceWithArrayAndFind() {
        assertScript '''
            File findFile() {
                new File[0].find { File f -> f.hidden }
            }
            findFile()
        '''
    }

    void testShouldNotThrowIncompatibleArgToFunVerifyError() {
        assertScript '''
            Object convertValueToType(Object value, Class targetType) {
                if (value instanceof CharSequence) {
                    value = value.toString()
                }
                if (value instanceof String) {
                    String strValue = value.trim()
                }
            }
            convertValueToType('foo', String)
        '''
    }

    void testSwitchCaseAnalysis1() {
        assertScript '''
            import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode as LUB

            def method(int x) {
                def returnValue = new Date()
                switch (x) {
                  case 1:
                    returnValue = 'string'
                    break
                  case 2:
                    returnValue = 42
                    break
                }
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.getNodeMetaData(INFERRED_TYPE)
                    assert type instanceof LUB
                    assert type.name == 'java.io.Serializable'
                })
                def val = returnValue

                returnValue
            }
        '''
    }

    // GROOVY-6215
    void testSwitchCaseAnalysis2() {
        assertScript '''
            def getValueForNumber(int x) {
                def valueToReturn
                switch(x) {
                  case 1:
                    valueToReturn = 'One'
                    break
                  case 2:
                    valueToReturn = []
                    valueToReturn << 'Two'
                    break
                }
                valueToReturn
            }

            def v1 = getValueForNumber(1)
            def v2 = getValueForNumber(2)
            def v3 = getValueForNumber(3)
            assert v1 == 'One'
            assert v2 == ['Two']
            assert v3 == null
        '''
    }

    // GROOVY-8411
    void testSwitchCaseAnalysis3() {
        shouldFailWithMessages '''
            void test(something) {
                switch (something) {
                  case Class:
                    break
                  case File:
                    something.canonicalName
                }
            }
        ''',
        'No such property: canonicalName for class: java.io.File'

        shouldFailWithMessages '''
            void test(something) {
                switch (something) {
                  case Class:
                    break
                  default:
                    something.canonicalName
                }
            }
        ''',
        'No such property: canonicalName for class: java.lang.Object'
/*
        shouldFailWithMessages '''
            void test(something) {
                switch (something) {
                  case Class:
                  case File:
                    something.toString()
                  default:
                    something.getCanonicalName()
                }
            }
        ''',
        'No such property: canonicalName for class: java.lang.Object'
*/
        shouldFailWithMessages '''
            void test(something) {
                switch (something) {
                  case Class:
                  case File:
                    something.toString()
                }
                something.canonicalName
            }
        ''',
        'No such property: canonicalName for class: java.lang.Object'

        assertScript '''
            void test(something) {
                switch (something) {
                  case Class.class:
                    something.canonicalName
                    break
                  case File:
                    something.canonicalPath
                    break
                  default:
                    something?.toString()
                }
            }
        '''

        assertScript '''
            void test(something) {
                switch (something) {
                  case Float:
                  case Double:
                  case Integer:
                    something.doubleValue()
                }
            }
            test(1)
            test(1.1d)
            test(1.1f)
            test('11')
        '''
    }

    void testNumberPrefixPlusPlusInference() {
        [Byte:'Integer',
         Character: 'Character',
         Short: 'Integer',
         Integer: 'Integer',
         Long: 'Long',
         Float: 'Double',
         Double: 'Double',
         BigDecimal: 'BigDecimal',
         BigInteger: 'BigInteger'
        ].each { orig, dest ->
            assertScript """
                $orig b = 65 as $orig
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                    assert type == make($dest)
                })
                def pp = ++b
                println '++${orig} -> ' + pp.class + ' ' + pp
                assert pp.class == ${dest}
            """
        }
    }

    void testNumberPostfixPlusPlusInference() {
        [Byte:'Byte',
         Character: 'Character',
         Short: 'Short',
         Integer: 'Integer',
         Long: 'Long',
         Float: 'Float',
         Double: 'Double',
         BigDecimal: 'BigDecimal',
         BigInteger: 'BigInteger'
        ].each { orig, dest ->
            assertScript """
                $orig b = 65 as $orig
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                    assert type == make($dest)
                })
                def pp = b++
                println '${orig}++ -> ' + pp.class + ' ' + pp
                assert pp.class == ${dest}
            """
        }
    }

    // GROOVY-6522
    void testInferenceWithImplicitClosureCoercion() {
        assertScript '''
            interface CustomCallable<T> {
                T call()
            }

            class Thing {
                static <T> T customType(CustomCallable<T> callable) {
                    callable.call()
                }

                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    lookup('test').each {
                        def call = it.expression
                        def type = call.getNodeMetaData(INFERRED_TYPE)
                        assert type.implementsInterface(LIST_TYPE)
                    }
                })
                static void run() {
                    test: customType { [] } // return type is not inferred - fails compile
                }
            }

            Thing.run()
        '''
    }

    void testInferenceWithImplicitClosureCoercionAndArrayReturn() {
        assertScript '''
            interface ArrayFactory<T> { T[] array() }

            public <T> T[] intArray(ArrayFactory<T> f) {
                f.array()
            }
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE.makeArray()
            })
            def array = intArray { new Integer[8] }
            assert array.length == 8
        '''
    }

    void testInferenceWithImplicitClosureCoercionAndListReturn() {
        assertScript '''
            interface ListFactory<T> { List<T> list() }

            public <T> List<T> list(ListFactory<T> f) {
                f.list()
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == LIST_TYPE
                assert type.genericsTypes[0].type == Integer_TYPE
            })
            def res = list { new LinkedList<Integer>() }
            assert res.size() == 0
        '''
    }

    // GROOVY-6835
    void testFlowTypingWithInstanceofAndInterfaceTypes() {
        assertScript '''
            class ShowUnionTypeBug {
                Map<String, Object> instanceMap = (Map<String,Object>)['a': 'Hello World']
                def findInstance(String key) {
                    Set<? extends CharSequence> allInstances = [] as Set
                    def instance = instanceMap.get(key)
                    if(instance instanceof CharSequence) {
                       allInstances.add(instance)
                    }
                    allInstances
                }
            }
            assert new ShowUnionTypeBug().findInstance('a') == ['Hello World'] as Set
        '''
    }

    void testInferenceWithImplicitClosureCoercionAndGenericTypeAsParameter() {
        assertScript '''
            interface Action<T> { void execute(T t) }

            public <T> void exec(T t, Action<T> f) {
                f.execute(t)
            }

            exec('foo') { println it.toUpperCase() }
        '''
    }

    // GROOVY-6574
    void testShouldInferPrimitiveBoolean() {
        assertScript '''
            def foo(Boolean o) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == boolean_TYPE
                })
                boolean b = o
                println b
            }
        '''
    }

    // GROOVY-7549
    void testShouldKeepDeclTypeWhenAssignedInaccessibleT() {
        config.targetDirectory = File.createTempDir()
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'a').mkdir()
            new File(parentDir, 'b').mkdir()

            def a = new File(parentDir, 'a/Main.groovy')
            a.write '''
                package a
                class Main {
                  static main(args) {
                    Face f = b.Maker.make() // returns b.Impl
                    assert f.meth() == 1234
                  }
                }
            '''
            def b = new File(parentDir, 'a/Face.groovy')
            b.write '''
                package a
                interface Face {
                  int meth()
                }
            '''
            def c = new File(parentDir, 'b/Impl.groovy')
            c.write '''
                package b
                @groovy.transform.PackageScope
                class Impl implements a.Face {
                  int meth() {
                    1234
                  }
                }
            '''
            def d = new File(parentDir, 'b/Maker.groovy')
            d.write '''
                package b
                class Maker {
                  static Impl make() { // probably should return a.Face
                    new Impl()
                  }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new CompilationUnit(config, null, loader)
            cu.addSources(a, b, c, d)
            cu.compile()

            loader.addClasspath(config.targetDirectory.absolutePath)
            loader.loadClass('a.Main', true).main()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    // GROOVY-5655
    void testByteArrayInference() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == byte_TYPE.makeArray()
            })
            def b = "foo".bytes
            new String(b)
        '''
    }

    // GROOVY-
    void testGetAnnotationFails() {
        assertScript '''
            import groovy.transform.*
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD])
            @interface Ann1 {}

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.FIELD])
            @interface Ann2 {}

            class A {
                @Ann2
                String field
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                lookup('second').each {
                    assert it.expression.getNodeMetaData(INFERRED_TYPE).name == 'Ann2'
                }
            })
            def doit(obj, String propName) {
                def field = obj.getClass().getDeclaredField(propName)
                if (field) {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        assert node.getNodeMetaData(INFERRED_TYPE).name == 'Ann1'
                    })
                    def annotation = field.getAnnotation Ann1
                    if(true) {
                        second: annotation = field.getAnnotation Ann2
                    }
                    return annotation
                }
                return null
            }

            assert Ann2.isAssignableFrom(doit(new A(), "field").class)
        '''
    }

    // GROOVY-9077
    void testInferredTypeForPropertyThatResolvesToMethod() {
        assertScript '''
            import groovy.transform.*

            void test() {
                def items = [1, 2] as LinkedList

                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    node = node.rightExpression
                    assert node.class.name.contains('PropertyExpression')
                    def target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert target != null
                    assert target.declaringClass.name == 'java.util.LinkedList'
                })
                def one = items.first

                @ASTTest(phase=CLASS_GENERATION, value={
                    node = node.rightExpression
                    assert node.class.name.contains('MethodCallExpression')
                    def target = node.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert target != null
                    assert target.declaringClass.name == 'java.util.LinkedList'
                })
                def alsoOne = items.peek()
            }

            test()
        '''
    }

    // GROOVY-10089
    void testInferredTypeForMapOfList() {
        assertScript '''
            void test(... attributes) {
                List one = [
                    [id:'x', options:[count:1]]
                ]
                List two = attributes.collect {
                    def node = Collections.singletonMap('children', one)
                    if (node) {
                        node = node.get('children').find { child -> child['id'] == 'x' }
                    }
                    // inferred type of node must be something like Map<String,List<...>>

                    [id: it['id'], name: node['name'], count: node['options']['count']]
                    //                                        ^^^^^^^^^^^^^^^ GroovyCastException (map ctor for Collection)
                }
            }

            test( [id:'x'] )
        '''
    }

    // GROOVY-10328
    void testInferredTypeForMapOrList() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                for (decl in node.code.statements*.expression) {
                    assert decl.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                }
            })
            void test(List<? super String> list, Map<String, ? super String> map) {
                def a = list.first()
                def b = list.get(0)
                def c = list[0]

                def x = map.get('foo')
                def y = map['foo']
                def z = map.foo
            }
        '''
    }
}
