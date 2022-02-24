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
package org.codehaus.groovy.transform

import org.codehaus.groovy.control.CompilerConfiguration
import org.codehaus.groovy.control.MultipleCompilationErrorsException
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit
import org.junit.Test

import java.util.concurrent.locks.Lock

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail
import static org.junit.Assert.assertEquals
import static org.junit.Assert.assertFalse
import static org.junit.Assert.assertTrue

/**
 * Tests for the {@code @Delegate} AST transform.
 */
final class DelegateTransformTest {

    @Test // GROOVY-3380
    void testDelegateImplementingNonPublicInterface() {
        assertScript '''
            import org.codehaus.groovy.transform.ClassImplementingANonPublicInterface

            class DelegatingToClassImplementingANonPublicInterface {
                @Delegate ClassImplementingANonPublicInterface delegate = new ClassImplementingANonPublicInterface()
            }

            def constant = new DelegatingToClassImplementingANonPublicInterface().returnConstant()
            assert constant == "constant"
        '''
    }

    @Test // GROOVY-3380
    void testDelegateImplementingNonPublicInterfaceWithZipFileConcreteCase() {
        assertScript '''
            import java.util.zip.*

            class ZipWrapper {
               @Delegate ZipFile zipFile
            }

            new ZipWrapper()
        '''
    }

    @Test // GROOVY-5974
    void testDelegateExcludes() {
        assertScript '''
            class MapSet {
                @Delegate(interfaces=false, excludes=['remove','clear']) Map m = [a: 1]
                @Delegate Set s = new LinkedHashSet([2, 3, 4] as Set) // HashSet not good enough in JDK 1.5
                String toString() { m.toString() + ' ' + s }
            }

            def ms = new MapSet()
            assert ms.size() == 1
            assert ms.toString() == '[a:1] [2, 3, 4]'
            ms.remove(3)
            assert ms.size() == 1
            assert ms.toString() == '[a:1] [2, 4]'
            ms.clear()
            assert ms.toString() == '[a:1] []'
        '''
    }

    @Test
    void testDelegateCompileStatic() {
        assertScript '''
            @groovy.transform.CompileStatic
            class MapSet {
                @Delegate(interfaces=false, excludes=['remove','clear']) Map m = [a: 1]
                @Delegate Set s = new LinkedHashSet([2, 3, 4] as Set)
                String toString() { m.toString() + ' ' + s }
            }

            def ms = new MapSet()
            assert ms.size() == 1
            assert ms.toString() == '{a=1} [2, 3, 4]'
            ms.remove(3)
            assert ms.size() == 1
            assert ms.toString() == '{a=1} [2, 4]'
            ms.clear()
            assert ms.toString() == '{a=1} []'
        '''
    }

    @Test
    void testLock() {
        def res = new GroovyShell().evaluate('''
            import java.util.concurrent.locks.*

            class LockableMap {
               @Delegate private Map map = [:]

               @Delegate private Lock lock = new ReentrantLock()

               @Delegate(interfaces=false) private List list = []
            }

            new LockableMap()
        ''')

        res.lock()
        try {
            res[0] = 0
            res[1] = 1
            res[2] = 2

            res.add('in list')
        }
        finally {
            res.unlock()
        }

        assertEquals([0: 0, 1: 1, 2: 2], res.@map)
        assertEquals('in list', res.@list[0])

        assertTrue res instanceof Map
        assertTrue res instanceof Lock
        assertFalse res instanceof List
    }

    @Test
    void testMultiple() {
        def res = new GroovyShell().evaluate('''
            class X {
                def value = 10
            }

            class Y {
                @Delegate X  x  = new X ()
                @Delegate XX xx = new XX ()

                void setValue (v) {
                    this.@x.@value = 12
                }
            }

            class XX {
                def value2 = 11
            }

            new Y ()
        ''')

        assertEquals 10, res.value
        assertEquals 11, res.value2
        res.value = 123
        assertEquals 12, res.value
    }

    @Test
    void testUsingDateCompiles() {
        assertScript '''
            class C {
                @Delegate Date d = new Date()
            }
            new C()
        '''
    }

    @Test // GROOVY-3471
    void testDelegateOnAMapTypeFieldWithInitializationUsingConstructorProperties() {
        // this was resulting in NPE due to MetaClassImpl's special handling of Map
        assertScript '''
            class Test3471 { @Delegate Map mp }
            def t = new Test3471(mp: [:])
            assert t.keySet().isEmpty()
        '''
    }

    @Test // GROOVY-3323
    void testDelegateTransformCorrectlyDelegatesMethodsFromSuperInterfaces() {
        assert new DelegateBarImpl(new DelegateFooImpl()).bar() == 'bar impl'
        assert new DelegateBarImpl(new DelegateFooImpl()).foo() == 'foo impl'
    }

    @Test // GROOVY-3555
    void testDelegateTransformIgnoresDeprecatedMethodsByDefault() {
        def b1 = new DelegateBarForcingDeprecated(baz: new BazWithDeprecatedFoo())
        def b2 = new DelegateBarWithoutDeprecated(baz: new BazWithDeprecatedFoo())
        assert b1.bar() == 'bar'
        assert b2.bar() == 'bar'
        assert b1.foo() == 'foo'
        shouldFail(MissingMethodException) {
            assert b2.foo() == 'foo'
        }
    }

    @Test // GROOVY-4163
    void testDelegateTransformAllowsInterfacesAndDelegation() {
        assertScript '''
            class Temp implements Runnable {
                @Delegate
                private Thread runnable

                static main(args) {
                    def thread = Thread.currentThread()
                    def temp = new Temp(runnable: thread)
                }
            }
        '''
    }

    @Test
    void testDelegateToObjectShouldFail() {
        shouldFail '''
            class C {
                @Delegate b = new Object()
            }
        '''
    }

    @Test
    void testDelegateToSelfTypeShouldFail() {
        shouldFail '''
            class C {
                @Delegate C c = new C()
            }
        '''
    }

    @Test // GROOVY-4265
    void testShouldPreferDelegatedOverStaticSuperMethod() {
        assertScript '''
            class A {
                static foo(){"A->foo()"}
            }
            class B extends A {
                @Delegate C c = new C()
            }
            class C {
                def foo(){"C->foo()"}
            }
            assert new B().foo() == 'C->foo()'
        '''
    }

    @Test // GROOVY-4244
    void testSetPropertiesThroughDelegate() {
        def foo = new Foo4244()

        assert foo.nonFinalBaz == 'Initial value - nonFinalBaz'
        foo.nonFinalBaz = 'New value - nonFinalBaz'
        assert foo.nonFinalBaz == 'New value - nonFinalBaz'

        assert foo.finalBaz == 'Initial value - finalBaz'
        shouldFail(ReadOnlyPropertyException) {
            foo.finalBaz = 'New value - finalBaz'
        }
    }

    @Test // GROOVY-4619
    void testDelegateSuperInterfaces() {
        assert 'doSomething' in SomeClass4619.class.methods*.name
    }

    @Test // GROOVY-5112
    void testGenericsOnArray() {
        assertScript '''
            class ListWrapper {
                @Delegate
                List myList

                @Delegate
                URL homepage
            }
            new ListWrapper()
        '''
    }

    @Test // GROOVY-5732
    void testInterfacesFromSuperClasses() {
        assertScript '''
            interface I5732 {
                void aMethod()
            }

            abstract class AbstractBaseClass implements I5732 { }

            abstract class DelegatedClass extends AbstractBaseClass {
                void aMethod() {}
            }

            class Delegator {
                @Delegate private DelegatedClass delegate
            }

            assert I5732.isAssignableFrom(Delegator)
        '''
    }

    @Test // GROOVY-5729
    void testDeprecationWithInterfaces() {
        assertScript '''
            interface I5729 {
                @Deprecated
                void aMethod()
            }

            class Delegator1 {
                @Delegate private I5729 delegate
            }
            assert I5729.isAssignableFrom(Delegator1)
            assert Delegator1.methods*.name.contains('aMethod')

            class Delegator2 {
                @Delegate(interfaces=false) private I5729 delegate
            }
            assert !I5729.isAssignableFrom(Delegator2)
            assert !Delegator2.methods*.name.contains('aMethod')

            class Delegator3 {
                @Delegate(interfaces=false, deprecated=true) private I5729 delegate
            }
            assert !I5729.isAssignableFrom(Delegator3)
            assert Delegator3.methods*.name.contains('aMethod')
        '''
    }

    @Test // GROOVY-5446
    void testDelegateWithParameterAnnotations() {
        assertScript '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.PARAMETER])
            public @interface SomeAnnotation {
            }

            class A {
                def method(@SomeAnnotation def param) { "Test" }
            }

            class A_Delegate {
                @Delegate(parameterAnnotations = true)
                A a = new A()
            }

            def originalMethod = A.getMethod('method', [Object.class] as Class[])
            def originalAnno = originalMethod.parameterAnnotations[0][0]

            def delegateMethod = A_Delegate.getMethod('method', [Object.class] as Class[])
            def delegateAnno = delegateMethod.parameterAnnotations[0][0]
            println delegateMethod.parameterAnnotations

            assert delegateAnno == originalAnno
        '''
    }

    @Test
    void testDelegateWithMethodAnnotations() {
        assertScript '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.METHOD])
            public @interface SomeAnnotation {
                int value()
            }

            class A {
                @SomeAnnotation(42)
                def method( def param) { "Test" }
            }

            class A_Delegate {
                @Delegate(methodAnnotations = true)
                A a = new A()
            }

            def originalMethod = A.getMethod('method', [Object.class] as Class[])
            def originalAnno = originalMethod.declaredAnnotations[0]

            def delegateMethod = A_Delegate.getMethod('method', [Object.class] as Class[])
            def delegateAnno = delegateMethod.declaredAnnotations[1]

            assert delegateAnno == originalAnno

            assert delegateAnno.value() == 42
            assert delegateAnno.value() == originalAnno.value()
        '''
    }

    @Test
    void testParameterAnnotationsShouldNotBeCarriedOverByDefault() {
        assertScript '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.PARAMETER])
            public @interface SomeAnnotation {
            }

            class A {
                def method(@SomeAnnotation def param) { "Test" }
            }

            class A_Delegate {
                @Delegate
                A a = new A()
            }

            def originalMethod = A.getMethod('method', [Object.class] as Class[])
            def originalAnno = originalMethod.parameterAnnotations[0][0]

            def delegateMethod = A_Delegate.getMethod('method', [Object.class] as Class[])
            assert delegateMethod.parameterAnnotations[0].length == 0
        '''
    }

    // this test reflects that we currently don't support carrying over
    // Closure Annotations rather than a desired design goal
    // TODO: support Closure Annotations and then remove/change this test
    @Test
    void testAnnotationWithClosureMemberIsNotSupported() {
        def err = shouldFail '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.METHOD])
            public @interface SomeAnnotation {
                Class value()
            }

            class A {
                @SomeAnnotation({ param != null })
                def method(def param) { "Test" }
            }

            class A_Delegate {
                @Delegate(methodAnnotations = true)
                A a = new A()
            }
        '''

        assert err.message.contains('@Delegate does not support keeping Closure annotation members.')
    }

    // this test reflects that we currently don't support carrying over
    // Closure Annotations rather than a desired design goal
    // TODO: support Closure Annotations and then remove/change this test
    @Test
    void testAnnotationWithClosureClassDescendantIsNotSupported() {
        def err = shouldFail '''
            import java.lang.annotation.*

            @Retention(RetentionPolicy.RUNTIME)
            @Target([ElementType.METHOD])
            public @interface SomeAnnotation {
                Class value()
            }

            class A {
                @SomeAnnotation(org.codehaus.groovy.runtime.GeneratedClosure.class)
                def method(def param) { "Test" }
            }

            class A_Delegate {
                @Delegate(methodAnnotations = true)
                A a = new A()
            }
        '''

        assert err.message.contains('@Delegate does not support keeping Closure annotation members.')
    }

    @Test // GROOVY-5445
    void testDelegateToSuperProperties() {
        assertScript '''
            class Foo {
                @Delegate Bar delegate = new Bar()
                def foo() {
                    bar = "bar"
                    baz = "baz"
                }
            }

            class Bar extends Baz { String bar }
            class Baz { String baz }

            def f = new Foo()
            f.foo()
            assert f.bar + f.baz == 'barbaz'
        '''
    }

    @Test // GROOVY-7243
    void testInclude() {
        assertScript '''
            class Book {
                String title
                String author

                String getTitleAndAuthor() {
                    "${title} : ${author}"
                }

                String getAuthorAndTitle() {
                    "${author} : ${title}"
                }
            }

            class OwnedBook {
                String owner

                @Delegate(includes=['author', 'getTitleAndAuthor'])
                Book book
            }

            Book book = new Book(title: 'Ulysses', author: 'James Joyce')
            OwnedBook ownedBook = new OwnedBook(owner: 'John Smith', book: book)

            ownedBook.author = 'John Smith'
            assert book.author == 'John Smith'

            assert ownedBook.getTitleAndAuthor() == 'Ulysses : John Smith'

            try {
                ownedBook.getAuthorAndTitle()
                assert false, 'Non-included methods should not be delegated'
            } catch(groovy.lang.MissingMethodException expected) {
            }

            try {
                ownedBook.title = 'Finnegans Wake'
                assert false, 'Non-included properties should not be delegated'
            } catch(groovy.lang.MissingPropertyException expected) {
            }
        '''
    }

    @Test // GROOVY-6329
    void testIncludeAndExcludeByType() {
        assertScript '''
            interface OddInclusionsTU<T, U> {
                boolean addAll(Collection<? extends T> t)
                boolean add(U u)
                T remove(int index)
            }

            interface OddInclusionsU<U> extends OddInclusionsTU<Integer, U> { }

            interface OddInclusions extends OddInclusionsU<Integer> { }

            interface OtherInclusions {
                void clear()
            }

            interface EvenExclusions extends OddInclusions, OtherInclusions { }

            class MixedNumbers {
                // collection variant of addAll and remove will work on odd list
                @Delegate(includeTypes=OddInclusions) List<Integer> odds = [1, 3]
                // clear will work on other list
                @Delegate(includeTypes=OtherInclusions) List<Integer> others = [0]
                // all other methods will work on even list
                @Delegate(excludeTypes=EvenExclusions) List<Integer> evens = [2, 4, 6]
                def getAll() { evens + odds + others }
            }

            def list = new MixedNumbers()
            assert list.all == [2, 4, 6, 1, 3, 0]
            list.add(5)
            list.addAll([7, 9])
            list.addAll(1, [8])
            list.remove(0)
            assert list.indexOf(8) == 1
            list.clear()
            assert list.all == [2, 8, 4, 6, 3, 5, 7, 9]
        '''
    }

    @Test // GROOVY-5211
    void testAvoidFieldNameClashWithParameterName() {
        assertScript '''
            class A {
                def foo(a) { a * 2 }
            }

            class B {
                @Delegate A a = new A()
            }

            assert new B().foo(10) == 20
        '''
    }

    @Test
    void testUnknownTypes() {
        shouldFail MultipleCompilationErrorsException, '''
            class Foo {
                @Delegate(includeTypes=XYZZY) List a = []
            }
        '''
        shouldFail MultipleCompilationErrorsException, '''
            class Foo {
                @Delegate(excludeTypes=42) List a = []
            }
        '''
    }

    @Test // GROOVY-6542
    void testLineNumberInStackTrace() {
        def err = shouldFail '''\
            @groovy.transform.ASTTest(phase=CANONICALIZATION, value={
                def property = node.getDeclaredField('thingie')
                def method = node.getDeclaredMethod('blowup')
                def call = method.code.expression

                assert property.lineNumber > 0
                assert call.lineNumber == property.lineNumber
            })
            class Upper {
                @Delegate Lower thingie

                Upper() {
                    thingie = new Lower()
                }
            }

            class Lower {
                def foo() {
                    println("Foo!")
                }

                def blowup(String a) {
                    throw new Exception("blow up with ${a}")
                }

                def blowup() {
                    throw new Exception("blow up")
                }
            }

            def up = new Upper()
            up.foo()
            up.blowup("bar")
        '''

        String stackTrace = err.asString()
        assert stackTrace =~ /at Upper\.blowup\(TestScript\d+\.groovy:10\)/
    }

    @Test
    void testShouldNotReuseRawClassNode() {
        assertScript '''
            import org.codehaus.groovy.transform.DelegateMap
            class Foo {
                DelegateMap dm = new DelegateMap()
            }
            def foo = new Foo()
            assert foo.dm.x == '123'
        '''
    }

    @Test // GROOVY-7118
    void testDelegateOfMethodHavingPlaceholder() {
        assertScript '''
            interface FooInt {
              public <T extends Throwable> T get(Class<T> clazz) throws Exception
            }

            class Foo implements FooInt {
              public <T extends Throwable> T get(Class<T> clazz) throws Exception {
                clazz.newInstance()
              }
            }

            class FooMain {
                @Delegate Foo foo = new Foo()
            }

            @groovy.transform.CompileStatic
            class FooMain2 {
                @Delegate Foo foo = new Foo()
            }

            assert new FooMain().get(Exception).class == Exception
            assert new FooMain2().get(Exception).class == Exception

            import org.codehaus.groovy.transform.Bar
            class BarMain {
                @Delegate Bar bar = new Bar()
            }
            assert new BarMain().get(Exception).class == Exception
        '''
    }

    @Test // GROOVY-7261
    void testShouldWorkWithLazyTransform() {
        assertScript '''
            class Foo {
                private @Delegate @Lazy ArrayList list = ['bar', 'baz']
                // fragile: $list is an internal implementation detail that may change
                def getInternalDelegate() { $list }
            }

            def f = new Foo()
            assert f.internalDelegate == null
            assert f.size() == 2
            assert f.internalDelegate == ['bar', 'baz']
        '''
    }

    @Test // GROOVY-6454
    void testMethodsWithInternalNameShouldNotBeDelegatedTo() {
        assertScript '''
            class HasMethodWithInternalName {
                void $() {
                }
            }

            class DelegatesToHasMethodWithInternalName {
                @Delegate
                HasMethodWithInternalName hasMethodWithInternalName
            }

            assert !new DelegatesToHasMethodWithInternalName().respondsTo('$')
        '''
    }

    @Test // GROOVY-6454
    void testMethodsWithInternalNameShouldBeDelegatedToIfRequested() {
        assertScript '''
            interface HasMethodWithInternalName {
                void $()
            }

            class DelegatesToHasMethodWithInternalName {
                @Delegate(allNames = true)
                HasMethodWithInternalName hasMethodWithInternalName
            }

            assert new DelegatesToHasMethodWithInternalName().respondsTo('$')
        '''
    }

    @Test // GROOVY-6454
    void testProperitesWithInternalNameShouldBeDelegatedToIfRequested() {
        assertScript '''
            class HasPropertyWithInternalName {
                def $
            }

            class DelegatesToHasPropertyWithInternalName {
                @Delegate(allNames = true)
                HasPropertyWithInternalName hasPropertyWithInternalName
            }

            def delegates = new DelegatesToHasPropertyWithInternalName()
            assert delegates.respondsTo('get$')
            assert delegates.respondsTo('set$')
        '''
    }

    @Test
    void testDelegateToGetterMethod() {
        // given:
        def delegate = { new DelegateFooImpl() }
        // when:
        def foo = new FooToMethod(delegate)
        // then:
        assert foo.foo() == delegate().foo()
    }

    @Test // GROOVY-5752
    void testDelegationShouldAccountForPrimitiveBooleanProperties() {
        assertScript '''
            class A {
                boolean a
                boolean b
                boolean isB() { b }
                boolean c
                boolean getC() { c }
            }

            class B {
                @Delegate A a = new A(a: true, b: true, c: true)
            }

            def a = new A(a: true, b: true, c: true)
            assert a.getA()
            assert a.isA()
            assert a.isB()
            assert a.getC()

            def b = new B()
            assert b.getA()
            assert b.isA()
            assert b.isB()
            assert b.getC()
        '''
    }

    @Test //GROOVY-8132
    void testOwnerPropertyPreferredToDelegateProperty() {
        assertScript '''
            class Foo {
                String pls
                @groovy.lang.Delegate
                Bar bar
            }

            class Bar {
                String pls
            }
            assert new Foo(pls: 'ok').pls == 'ok'
        '''
    }

    @Test
    void testOwnerMethodPreferredToDelegateMethod() {
        assertScript '''
            class Foo {
                String pls() { 'foo pls' }
                @groovy.lang.Delegate
                Bar bar
            }

            class Bar {
                String pls() { 'bar pls' }
            }
            assert new Foo(bar: new Bar()).pls() == 'foo pls'
        '''
    }

    @Test // GROOVY-8204
    void testDelegateToArray() {
        assertScript '''
            class BugsMe {
                @Delegate
                String[] content = ['foo', 'bar']
            }

            assert new BugsMe().content.join() == 'foobar'
            assert new BugsMe().content.length == 2
            assert new BugsMe().length == 2
        '''
    }

    @Test // GROOVY-9289
    void testExcludesWithInvalidPropertyNameResultsInError() {
        def err = shouldFail '''
            class WMap {
                String name
                @Delegate(excludes = "name")
                Map<String, String> data

                WMap(String name, Map<String, String> data) {
                    this.name = name
                    this.data = data
                }
            }

            new WMap('example', [name: 'weird'])
        '''

        assert err.message.contains("Error during @Delegate processing: 'excludes' property or method 'name' does not exist.")
    }

    @Test // GROOVY-8825
    void testDelegateToPrecompiledGroovyGeneratedMethod() {
        assertScript '''
            import org.codehaus.groovy.transform.CompiledClass8825
            class B {
                @Delegate(methodAnnotations = true)
                private final CompiledClass8825 delegate = new CompiledClass8825()
            }
            assert new B().s == '456'
        '''
    }

    @Test // GROOVY-9414
    void testDelegateToPropertyViaGetter() {
        assertScript '''
            class Bar {
                String name
            }
            class BarDelegate {
                @Delegate(includes = 'getName') Bar bar = new Bar(name: 'Baz')
            }
            assert new BarDelegate().name == 'Baz'
        '''
    }

    @Test // GROOVY-4516
    void testParameterWithDefaultArgument1() {
        assertScript '''
            class C {
                def m(boolean b = true) {
                    b
                }
            }
            class D {
                @Delegate private C c = new C()
            }

            assert new D().m() == true
            assert new D().m(false) == false
        '''
    }

    @Test // GROOVY-4516
    void testParameterWithDefaultArgument2() {
        assertScript '''
            class C {
                def m(x = 'x', y = 'y', int z) {
                    '' + x + y + z
                }
            }
            class D {
                @Delegate private C c = new C()
            }

            assert new D().m(1) == 'xy1'
            assert new D().m(1,2) == '1y2'
            assert new D().m(1,2,3) == '123'
        '''
    }

    @Test
    void testParameterWithDefaultArgument3() {
        assertScript '''
            class C {
                def m(x = 'x', y = why(), int z) {
                    '' + x + y + z
                }
                private why() {
                    'y'
                }
            }
            class D {
                @Delegate private C c = new C()
            }

            assert new D().m(1) == 'xy1'
            assert new D().m(1,2) == '1y2'
            assert new D().m(1,2,3) == '123'
        '''
    }

    @Test // GROOVY-4320
    void testParameterWithDefaultArgument4() {
        assertScript '''
            interface I {
                String event(String name)
                String event(String name, List values)
            }
            class C implements I {
                String event(String name, List values = []) {
                    return "$name:$values"
                }
            }
            class D {
                @Delegate private C c = new C() // The method with default parameters "..." defines a method "event(java.lang.String)" that is already defined.
            }

            String result = new D().event('x')
            assert result == 'x:[]'
        '''
    }

    @Test // GROOVY-4320
    void testParameterWithDefaultArgument5() {
        def config = new CompilerConfiguration(
            targetDirectory: File.createTempDir(),
            jointCompilationOptions: [memStub: true]
        )
        def parentDir = File.createTempDir()
        try {
            new File(parentDir, 'p').mkdir()

            def a = new File(parentDir, 'p/I.java')
            a.write '''
                package p;
                import java.util.List;
                public interface I {
                    String event(String name);
                    String event(String name, List values);
                }
            '''
            def b = new File(parentDir, 'p/C.groovy')
            b.write '''
                package p
                class C implements I {
                    private final I i
                    C(I i) { this.i = i }

                    String event(String name, List values = []) {
                        return "$name:$values"
                    }
                }
            '''
            def c = new File(parentDir, 'p/Main.groovy')
            c.write '''
                package p
                class Main {
                    @Delegate private C c = new C(this)

                    void test() {
                        String result = this.event('x')
                        assert result == 'x:[]'
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b, c)
            cu.compile()

            loader.loadClass('p.Main').newInstance().test()
        } finally {
            config.targetDirectory.deleteDir()
            parentDir.deleteDir()
        }
    }

    @Test // GROOVY-5204
    void testParameterWithDefaultArgument6() {
        assertScript '''
            class C {
                def m(p = null) {
                    'C'
                }
            }
            class D {
                @Delegate private C c = new C()
                def m() {
                    'D'
                }
            }

            assert new D().m() == 'D'
            assert new D().m('') == 'C'
        '''
    }
}

//------------------------------------------------------------------------------

interface DelegateFoo {
    def foo()
}

class DelegateFooImpl implements DelegateFoo {
    def foo() { 'foo impl' }
}

interface DelegateBar extends DelegateFoo {
    def bar()
}

class DelegateBarImpl implements DelegateBar {
    @Delegate DelegateFoo foo;

    DelegateBarImpl(DelegateFoo f) { this.foo = f}

    def bar() { 'bar impl'}
}

class BazWithDeprecatedFoo {
    @Deprecated foo() { 'foo' }
    def bar() { 'bar' }
}

class DelegateBarWithoutDeprecated {
    @Delegate BazWithDeprecatedFoo baz
}

class DelegateBarForcingDeprecated {
    @Delegate(deprecated=true) BazWithDeprecatedFoo baz
}

class Foo4244 {
    @Delegate Bar4244 bar = new Bar4244()
}

class FooToMethod {
    private final Closure<DelegateFoo> strategy

    FooToMethod(Closure<DelegateFoo> strategy) {
        this.strategy = strategy
    }

    @Delegate
    DelegateFoo getStrategy() { strategy() }
}

class Bar4244 {
    String nonFinalBaz = "Initial value - nonFinalBaz"
    final String finalBaz = "Initial value - finalBaz"
}

interface SomeInterface4619 {
    void doSomething()
}

interface SomeOtherInterface4619 extends SomeInterface4619 {}

class SomeClass4619 {
    @Delegate
    SomeOtherInterface4619 delegate
}

interface BarInt {
    public <T extends Throwable> T get(Class<T> clazz) throws Exception
}

class Bar implements BarInt {
    public <T extends Throwable> T get(Class<T> clazz) throws Exception {
        clazz.newInstance()
    }
}

class CompiledClass8825 {
    final String s = '456'
}

// DO NOT MOVE INSIDE THE TEST SCRIPT OR IT WILL NOT TEST
// WHAT IT IS SUPPOSED TO TEST ANYMORE !
class DelegateMap {
    protected final @Delegate Map props = [x:'123']
}
