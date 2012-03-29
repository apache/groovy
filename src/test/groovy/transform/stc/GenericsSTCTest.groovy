/*
 * Copyright 2003-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform.stc

import org.codehaus.groovy.control.customizers.CompilationCustomizer
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.ast.ClassCodeVisitorSupport
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.transform.stc.StaticTypesMarker
import org.codehaus.groovy.ast.ClassHelper

/**
 * Unit tests for static type checking : generics.
 *
 * @author Cedric Champeau
 */
class GenericsSTCTest extends StaticTypeCheckingTestCase {

    void testDeclaration() {
        assertScript '''
            List test = new LinkedList<String>()
        '''
    }

    void testDeclaration5() {
        assertScript '''
            Map<String,Integer> obj = new HashMap<String,Integer>()
        '''
    }

    void testDeclaration6() {
        shouldFailWithMessages '''
            Map<String,String> obj = new HashMap<String,Integer>()
        ''', 'Incompatible generic argument types. Cannot assign java.util.HashMap <String, Integer> to: java.util.Map <String, String>'
    }

    void testAddOnList() {
        shouldFailWithMessages '''
            List<String> list = []
            list.add(1)
        ''', "Cannot find matching method java.util.List#add(int)"
    }

    void testAddOnList2() {
        assertScript '''
            List<String> list = []
            list.add 'Hello'
        '''

        assertScript '''
            List<Integer> list = []
            list.add 1
        '''
    }

    void testAddOnListWithDiamond() {
        assertScript '''
            List<String> list = new LinkedList<>()
            list.add 'Hello'
        '''
    }
    
    void testAddOnListUsingLeftShift() {
        shouldFailWithMessages '''
            List<String> list = []
            list << 1
        ''', 'Cannot find matching method java.util.List#leftShift(int)'
    }

    void testAddOnList2UsingLeftShift() {
        assertScript '''
            List<String> list = []
            list << 'Hello'
        '''

        assertScript '''
            List<Integer> list = []
            list << 1
        '''
    }

    void testAddOnListWithDiamondUsingLeftShift() {
        assertScript '''
            List<String> list = new LinkedList<>()
            list << 'Hello'
        '''
    }

    void testListInferrenceWithNullElems() {
        assertScript '''
            List<String> strings = ['a', null]
            assert strings == ['a',null]
        '''
    }

    void testListInferrenceWithAllNullElems() {
        assertScript '''
            List<String> strings = [null, null]
            assert strings == [null,null]
        '''
    }

    void testAddOnListWithDiamondAndWrongType() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list.add 'Hello'
        ''', 'Cannot find matching method java.util.LinkedList#add(java.lang.String)'
    }

    void testAddOnListWithDiamondAndWrongTypeUsingLeftShift() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list << 'Hello'
        ''', 'Cannot find matching method java.util.LinkedList#leftShift(java.lang.String)'
    }

    void testAddOnListWithDiamondAndNullUsingLeftShift() {
        assertScript '''
            List<Integer> list = new LinkedList<>()
            list << null
        '''
    }

    void testReturnTypeInference() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<Integer>()
            Integer result = foo.method()
        '''
    }

    void testReturnTypeInferenceWithDiamond() {
        assertScript '''
            class Foo<U> {
                U method() { }
            }
            Foo<Integer> foo = new Foo<>()
            Integer result = foo.method()
        '''
    }

    void testReturnTypeInferenceWithMethodGenerics() {
        assertScript '''
            List<Long> list = Arrays.asList([0L,0L] as Long[])
        '''
    }

    void testReturnTypeInferenceWithMethodGenericsAndVarArg() {
        assertScript '''
            List<Long> list = Arrays.asList(0L,0L)
        '''
    }

    void testDiamondInferrenceFromConstructor() {
        assertScript '''
            Set< Long > s2 = new HashSet<>()
        '''
    }

    void testDiamondInferrenceFromConstructorWithoutAssignment() {
        assertScript '''
            new HashSet<>(Arrays.asList(0L,0L));
        '''
    }

    void testDiamondInferrenceFromConstructor2() {
        shouldFailWithMessages '''
            Set< Number > s3 = new HashSet<>(Arrays.asList(0L,0L));
        ''', 'Cannot assign java.util.HashSet <java.lang.Long> to: java.util.Set <Number>'
    }

    void testDiamondInferrenceFromConstructor3() {
        assertScript '''
            Set<Number> s4 = new HashSet<Number>(Arrays.asList(0L,0L))
        '''
    }


    void testLinkedListWithListArgument() {
        assertScript '''
            List<String> list = new LinkedList<String>(['1','2','3'])
        '''
    }

    void testLinkedListWithListArgumentAndWrongElementTypes() {
        shouldFailWithMessages '''
            List<String> list = new LinkedList<String>([1,2,3])
        ''', 'Cannot find matching method java.util.LinkedList#<init>(java.util.List <java.lang.Integer>)'
    }

    void testCompatibleGenericAssignmentWithInferrence() {
        shouldFailWithMessages '''
            List<String> elements = ['a','b', 1]
        ''', 'Incompatible generic argument types. Cannot assign java.util.List <java.io.Serializable> to: java.util.List <String>'
    }

    void testGenericAssignmentWithSubClass() {
        assertScript '''
            List<String> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        '''
    }

    void testGenericAssignmentWithSubClassAndWrongGenericType() {
        shouldFailWithMessages '''
            List<Integer> list = new groovy.transform.stc.GenericsSTCTest.MyList()
        ''', 'Incompatible generic argument types'
    }

    void testAddShouldBeAllowedOnUncheckedGenerics() {
        assertScript '''
            List list = []
            list.add 'Hello'
            list.add 2
            list.add 'the'
            list.add 'world'
        '''
    }

    void testAssignmentShouldFailBecauseOfLowerBound() {
        shouldFailWithMessages '''
            List<? super Number> list = ['string']
        ''', 'Number'
    }

    void testGroovy5154() {
        assertScript '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    FooBound fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            new Foo()
        '''
    }

    void testGroovy5154WithSubclass() {
        assertScript '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    FooBound2 fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            class FooBound2 extends FooBound {}
            new Foo()
        '''
    }

    void testGroovy5154WithIncorrectType() {
        shouldFailWithMessages '''
            class Foo {
                def say() {
                    FooWithGenerics f
                    Object fb
                    f.say(fb)
                }
            }

            class FooWithGenerics {
                def <T extends FooBound> void say(T p) {
                }
            }
            class FooBound {
            }
            new Foo()
        ''', 'Cannot find matching method FooWithGenerics#say(java.lang.Object)'
    }

    void testVoidReturnTypeInferrence() {
      assertScript '''
        Object m() {
          def s = '1234'
          println 'Hello'
        }
      '''
    }

    // GROOVY-5237
    void testGenericTypeArgumentAsField() {
        assertScript '''
            class Container<T> {
                private T initialValue
                Container(T initialValue) { this.initialValue = initialValue }
                T get() { initialValue }
            }
            Container<Date> c = new Container<Date>(new Date())
            long time = c.get().time
        '''
    }

    void testReturnAnntationClass() {
        assertScript '''
            import java.lang.annotation.Documented
            Documented annotation = Deprecated.getAnnotation(Documented)
        '''
    }

    void testReturnListOfParameterizedType() {
        assertScript '''
            class A {}
            class B extends A { void bar() {} }
            public <T extends A> List<T> foo() { [] }

            List<B> list = foo()
            list.add(new B())
        '''
    }

    void testMethodCallWithClassParameterUsingClassLiteralArg() {
        assertScript '''
            class A {}
            class B extends A {}
            class Foo {
                void m(Class<? extends A> clazz) {}
            }
            new Foo().m(B)
        '''
    }
  
    void testMethodCallWithClassParameterUsingClassLiteralArgWithoutWrappingClass() {
        assertScript '''
            class A {}
            class B extends A {}
            void m(Class<? extends A> clazz) {}
            m(B)
        '''
    }

    void testConstructorCallWithClassParameterUsingClassLiteralArg() {
        assertScript '''
            class A {}
            class B extends A {}
            class C extends B {}
            class Foo {
                Foo(Class<? extends A> clazz) {}
            }
            new Foo(B)
            new Foo(C)
        '''
    }

    void testConstructorCallWithClassParameterUsingClassLiteralArgAndInterface() {
        assertScript '''
            interface A {}
            class B implements A {}
            class C extends B {}
            class Foo {
                Foo(Class<? extends A> clazz) {}
            }
            new Foo(B)
            new Foo(C)
        '''
    }

    void testPutMethodWithPrimitiveValue() {
        assertScript '''
            Map<String, Integer> map = new HashMap<String,Integer>()
            map.put('hello', 1)
        '''
    }

    void testPutMethodWithWrongValueType() {
        shouldFailWithMessages '''
            Map<String, Integer> map = new HashMap<String,Integer>()
            map.put('hello', new Object())
        ''', 'Cannot find matching method java.util.HashMap#put(java.lang.String, java.lang.Object)'
    }

    void testPutMethodWithPrimitiveValueAndArrayPut() {
        assertScript '''
            Map<String, Integer> map = new HashMap<String,Integer>()
            map['hello'] = 1
        '''
    }

    void testShouldComplainAboutToInteger() {
        shouldFailWithMessages '''
            class Test {
                static test2() {
                    if (new Random().nextBoolean()) {
                        def a = new ArrayList<String>()
                        a << "a" << "b" << "c"
                        return a
                    } else {
                        def b = new LinkedList<Number>()
                        b << 1 << 2 << 3
                        return b
                    }
                }

                static test() {
                    def result = test2()
                    result[0].toInteger()
                    //result[0].toString()
                }
            }
            new Test()
        ''', 'Cannot find matching method java.io.Serializable#toInteger()'
    }

    void testAssignmentOfNewInstance() {
        Expression expr = null
        config.addCompilationCustomizers(
                new CompilationCustomizer(CompilePhase.INSTRUCTION_SELECTION) {
                    @Override
                    void call(final org.codehaus.groovy.control.SourceUnit source, final org.codehaus.groovy.classgen.GeneratorContext context, final org.codehaus.groovy.ast.ClassNode classNode) {
                        def visitor = new ClassCodeVisitorSupport() {
                            @Override
                            protected org.codehaus.groovy.control.SourceUnit getSourceUnit() {
                                source
                            }

                            @Override
                            void visitVariableExpression(final VariableExpression expression) {
                                super.visitVariableExpression(expression)
                                if (expression.name=='obj') expr = expression
                            }
                        }
                        visitor.visitClass(classNode)
                    }
                }
        )
        shell = new GroovyShell(config)
        assertScript '''
            class Foo {
                static Class clazz = Date
                public static void main(String... args) {
                    def obj = clazz.newInstance()
                }
            }
        '''
        assert expr != null
        assert expr.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE) == ClassHelper.OBJECT_TYPE
    }

    static class MyList extends LinkedList<String> {}
}

