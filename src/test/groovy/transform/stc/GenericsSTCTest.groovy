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
import org.codehaus.groovy.antlr.AntlrParserPluginFactory

/**
 * Unit tests for static type checking : generics.
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
        ''', "[Static type checking] - Cannot find matching method java.util.List#add(int)"
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
        ''', '[Static type checking] - Cannot call <T> java.util.List <String>#leftShift(T) with arguments [int] '
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

    void testAddOnListWithParameterizedTypeLeftShift() {
        assertScript '''
            class Trie<T> {}
            List<Trie<String>> list = []
            list << new Trie<String>()
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
        ''', '[Static type checking] - Cannot call java.util.LinkedList <java.lang.Integer>#add(java.lang.Integer) with arguments [java.lang.String]'
    }

    void testAddOnListWithDiamondAndWrongTypeUsingLeftShift() {
        shouldFailWithMessages '''
            List<Integer> list = new LinkedList<>()
            list << 'Hello'
        ''', '[Static type checking] - Cannot call <T> java.util.LinkedList <java.lang.Integer>#leftShift(T) with arguments [java.lang.String]'
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
        ''', 'Cannot call java.util.LinkedList <String>#<init>(java.util.Collection <java.lang.Object extends java.lang.String>) with arguments [java.util.List <java.lang.Integer>]'
    }

    void testCompatibleGenericAssignmentWithInference() {
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

    // GROOVY-6126
    void testChoosesCorrectMethodOfParameterizedReturnType() {
        assertScript '''
            class Loader {
                public <T> T load(Class<T> entityClass, Serializable id) {entityClass.newInstance()}
                public void load(final Object entity, final Serializable id) {}
            }

            class MyClass<D> {
                Class<D> persistentClass
                Loader hibernateTemplate = new Loader()
                MyClass(Class<D> c) {
                    this.persistentClass = c
                }

                D load(Serializable id) {
                    id = convertIdentifier(id)
                    if (id != null) {
                        return hibernateTemplate.load(persistentClass, id)
                    }
                }

                Serializable convertIdentifier(Serializable s) {"1"}
            }
            class Foo{}

            MyClass<Foo> mc = new MyClass(Foo)
            Foo foo = mc.load("2")'''
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
        ''', '[Static type checking] - Cannot call java.util.HashMap <String, Integer>#put(java.lang.String, java.lang.Integer) with arguments [java.lang.String, java.lang.Object]'
    }

    void testPutMethodWithPrimitiveValueAndArrayPut() {
        assertScript '''
            Map<String, Integer> map = new HashMap<String,Integer>()
            map['hello'] = 1
        '''
    }

    void testShouldComplainAboutToInteger() {

        String code = '''
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
        '''

        if (config.pluginFactory instanceof AntlrParserPluginFactory) {
            shouldFailWithMessages code, 'Cannot find matching method java.lang.Object#getAt(int)'
        } else {
            shouldFailWithMessages code,
                    'Cannot find matching method java.lang.Object#getAt(int)',
                    'Cannot find matching method java.lang.Object#toInteger()'
        }
    }

    void testAssignmentOfNewInstance() {
        assertScript '''
            class Foo {
                static Class clazz = Date
                public static void main(String... args) {
                    @ASTTest(phase=INSTRUCTION_SELECTION, value={
                        assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                    })
                    def obj = clazz.newInstance()
                }
            }
        '''
    }

    // GROOVY-5415
    void testShouldUseMethodGenericType1() {
        assertScript '''import groovy.transform.stc.GenericsSTCTest.ClassA
        class ClassB {
            void bar() {
                def ClassA<Long> a = new ClassA<Long>();
                a.foo(this.getClass());
            }
        }
        new ClassB()
        '''
    }

    // GROOVY-5415
    void testShouldUseMethodGenericType2() {
        shouldFailWithMessages '''import groovy.transform.stc.GenericsSTCTest.ClassA
        class ClassB {
            void bar() {
                def ClassA<Long> a = new ClassA<Long>();
                a.bar(this.getClass());
            }
        }
        new ClassB()
        ''', 'Cannot call <X> groovy.transform.stc.GenericsSTCTest$ClassA <Long>#bar(java.lang.Class <Long>) with arguments [java.lang.Class <? extends java.lang.Object>]'
    }

    // GROOVY-5516
    void testAddAllWithCollectionShouldBeAllowed() {
        assertScript '''import org.codehaus.groovy.transform.stc.ExtensionMethodNode
            List<String> list = ['a','b','c']
            Collection<String> e = list.findAll { it }
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def dmt = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert dmt instanceof ExtensionMethodNode == false
                assert dmt.name == 'addAll'
                assert dmt.declaringClass == make(List)
            })
            boolean r = list.addAll(e)
        '''
    }

    void testAddAllWithCollectionShouldNotBeAllowed() {
        shouldFailWithMessages '''
            List<String> list = ['a','b','c']
            Collection<Integer> e = (Collection<Integer>) [1,2,3]
            boolean r = list.addAll(e)
        ''', 'Cannot call java.util.List <java.lang.String>#addAll(java.util.Collection <java.lang.Object extends java.lang.String>) with arguments [java.util.Collection <Integer>]'
    }

    // GROOVY-5528
    void testAssignmentToInterfaceFromUserClassWithGenerics() {
        assertScript '''class UserList<T> extends LinkedList<T> {}
        List<String> list = new UserList<String>()
        '''
    }

    // GROOVY-5559
    void testGStringInListShouldNotBeConsideredAsAString() {
        assertScript '''import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode as LUB
        def bar = 1
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == LIST_TYPE
            assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type instanceof LUB
        })
        def list = ["foo", "$bar"]
        '''

        shouldFailWithMessages '''import org.codehaus.groovy.ast.tools.WideningCategories.LowestUpperBoundClassNode as LUB
        def bar = 1
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == LIST_TYPE
            assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type instanceof LUB
        })
        List<String> list = ["foo", "$bar"]
        ''', 'You are trying to use a GString'

        shouldFailWithMessages '''
        def bar = 1
        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == LIST_TYPE
            assert node.getNodeMetaData(INFERRED_TYPE).genericsTypes[0].type == GSTRING_TYPE
        })
        List<String> list = ["$bar"] // single element means no LUB
        ''', 'You are trying to use a GString'
    }

    // GROOVY-5559: related behaviour
    void testGStringString() {
        assertScript '''
            int i = 1
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == GSTRING_TYPE
            })
            def str = "foo$i"
            assert str == 'foo1'
        '''
    }

    // GROOVY-5594
    void testMapEntryUsingPropertyNotation() {
        assertScript '''
        Map.Entry<Date, Integer> entry = null

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == make(Date)
        })
        def k = entry?.key

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
        })
        def v = entry?.value
        '''
    }

    void testInferenceFromMap() {
        assertScript '''
        Map<Date, Integer> map = [:]

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            def infType = node.getNodeMetaData(INFERRED_TYPE)
            assert infType == make(Set)
            def entryInfType = infType.genericsTypes[0].type
            assert entryInfType == make(Map.Entry)
            assert entryInfType.genericsTypes[0].type == make(Date)
            assert entryInfType.genericsTypes[1].type == Integer_TYPE
        })
        def entries = map?.entrySet()
        '''
    }

    void testInferenceFromListOfMaps() {
        assertScript '''
        List<Map<Date, Integer>> maps = []

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            def listType = node.getNodeMetaData(INFERRED_TYPE)
            assert listType == Iterator_TYPE
            def infType = listType.genericsTypes[0].type
            assert infType == make(Map)
            assert infType.genericsTypes[0].type == make(Date)
            assert infType.genericsTypes[1].type == Integer_TYPE
        })
        def iter = maps?.iterator()
        '''
    }

    void testAssignNullMapWithGenerics() {
        assertScript '''
            Map<String, Integer> foo = null
            Integer result = foo?.get('a')
        '''
    }

    void testAssignNullListWithGenerics() {
        assertScript '''
            List<Integer> foo = null
            Integer result = foo?.get(0)
        '''
    }

    void testAssignNullListWithGenericsWithSequence() {
        assertScript '''
            List<Integer> foo = [1]
            foo = null
            Integer result = foo?.get(0)
        '''

    }

    void testMethodCallWithArgumentUsingNestedGenerics() {
        assertScript '''
           ThreadLocal<Map<Integer, String>> cachedConfigs = new ThreadLocal<Map<Integer, String>>()
           def configs = new HashMap<Integer, String>()
           cachedConfigs.set configs
        '''
    }

    void testInferDiamondUsingAIC() {
        shouldFailWithMessages '''
            Map<String,Date> map = new HashMap<>() {}
        ''', 'Cannot use diamond <> with anonymous inner classes'
    }

    // GROOVY-5614
    void testInferDiamondForFields() {
        assertScript '''
            class Rules {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.initialExpression.getNodeMetaData(INFERRED_TYPE)
                    assert type == make(HashMap)
                    assert type.genericsTypes.length == 2
                    assert type.genericsTypes[0].type == Integer_TYPE
                    assert type.genericsTypes[1].type == make(Date)
                })

                final Map<Integer, Date> bindings1  = new HashMap<>();
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.initialExpression.getNodeMetaData(INFERRED_TYPE)
                    assert type == make(HashMap)
                    assert type.genericsTypes.length == 2
                    assert type.genericsTypes[0].type == STRING_TYPE
                    assert type.genericsTypes[1].type == STRING_TYPE
                })
                final Map<String, String> bindings2 = new HashMap<>();
            }
            def r = new Rules()

            r.bindings1[3] = new Date()
            assert r.bindings1.containsKey(3)

            r.bindings2['a'] = 'A'
            r.bindings2.put('b', 'B')

        '''
    }
    void testInferDiamondForAssignment() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == STRING_TYPE
                assert type.genericsTypes[1].type == STRING_TYPE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == STRING_TYPE
                assert type.genericsTypes[1].type == STRING_TYPE
            })
            Map<String, String> map = new HashMap<>()
        '''
    }
    void testInferDiamondForAssignmentWithDates() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
        '''
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalKeyUsingPut() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map.put('foo', new Date())
        ''', '[Static type checking] - Cannot call java.util.HashMap <java.util.Date, java.util.Date>#put(java.util.Date, java.util.Date) with arguments [java.lang.String, java.util.Date]'
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalKeyUsingSquareBracket() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map['foo'] = new Date()
        ''', 'Cannot call <K,V> java.util.HashMap <java.util.Date, java.util.Date>#putAt(java.util.Date, java.util.Date) with arguments [java.lang.String, java.util.Date]'
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalValueUsingPut() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map.put(new Date(), 'foo')
        ''', '[Static type checking] - Cannot call java.util.HashMap <java.util.Date, java.util.Date>#put(java.util.Date, java.util.Date) with arguments [java.util.Date, java.lang.String]'
    }
    void testInferDiamondForAssignmentWithDatesAndIllegalValueUsingSquareBracket() {
        shouldFailWithMessages '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def DATE = make(Date)
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
                type = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert type == make(HashMap)
                assert type.genericsTypes.length == 2
                assert type.genericsTypes[0].type == DATE
                assert type.genericsTypes[1].type == DATE
            })
            Map<Date, Date> map = new HashMap<>()
            map[new Date()] = 'foo'
        ''', 'Cannot call <K,V> java.util.HashMap <java.util.Date, java.util.Date>#putAt(java.util.Date, java.util.Date) with arguments [java.util.Date, java.lang.String]'
    }

    void testCallMethodWithParameterizedArrayList() {
        assertScript '''
        class MyUtility {
            def methodOne() {
                def someFiles = new ArrayList<File>()
                def someString = ''
                methodTwo someString, someFiles
            }

            def methodTwo(String s, List<File> files) {}
        }
        new MyUtility()
        '''
    }

    void testGenericTypeArrayOfDGMMethod() {
        assertScript '''
            int[] arr = [0,1,2,3]
            assert arr.findAll() == [1,2,3]
        '''
    }

    // GROOVY-5617
    void testIntermediateListAssignmentOfGStrings() {
        assertScript '''
        def test() {
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(List)
                assert type.genericsTypes.length==1
                assert type.genericsTypes[0].type == GSTRING_TYPE
            })
            List<GString> dates = ["${new Date()-1}", "${new Date()}", "${new Date()+1}"]
            dates*.toUpperCase()
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type == make(List)
                assert type.genericsTypes.length==1
                assert type.genericsTypes[0].type == GSTRING_TYPE
            })
            List<GString> copied = []
            copied.addAll(dates)
            List<String> upper = copied*.toUpperCase()
        }
        test()
        '''
    }

    // GROOVY-5650
    void testRegressionInGenericsTypeInference() {
        assertScript '''import groovy.transform.stc.GenericsSTCTest.JavaClassSupport as JavaClass
        List<JavaClass.StringContainer> containers = new ArrayList<>();
        containers.add(new JavaClass.StringContainer());
        List<String> strings = JavaClass.unwrap(containers);
        '''
    }

    // In Groovy, we do not throw warnings (in general) and in that situation, not for unchecked
    // assignments like in Java
    // In the following test, the LHS of the assignment uses generics, while the RHS does not.
    // As we have the concept of flow typing too, we are facing a problem: what inferred type is the RHS?
    void testUncheckedAssignment() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(List)
                assert ift.isUsingGenerics()
                def gts = ift.genericsTypes
                assert gts.length==1
                assert gts[0].type == STRING_TYPE
            })
            List<String> list = (List) null
        '''
    }

    void testUncheckedAssignmentWithSuperInterface() {
        assertScript '''
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def ift = node.getNodeMetaData(INFERRED_TYPE)
                assert ift == make(List)
                assert ift.isUsingGenerics()
                def gts = ift.genericsTypes
                assert gts.length==1
                assert gts[0].type == STRING_TYPE
            })
            Iterable<String> list = (List) null
        '''
    }

    void testIncompatibleGenericsForTwoArguments() {
        shouldFailWithMessages '''
            public <T> void printEqual(T arg1, T arg2) {
                println arg1 == arg2
            }
            printEqual(1, 'foo')
        ''', '#printEqual(T, T) with arguments [int, java.lang.String]'
    }
    void testIncompatibleGenericsForTwoArgumentsUsingEmbeddedPlaceholder() {
        shouldFailWithMessages '''
            public <T> void printEqual(T arg1, List<T> arg2) {
                println arg1 == arg2
            }
            printEqual(1, ['foo'])
        ''', '#printEqual(T, java.util.List <T>) with arguments [int, java.util.List <java.lang.String>]'
    }

    void testGroovy5748() {
        assertScript '''
            interface IStack<T> {
                INonEmptyStack<T, ? extends IStack<T>> push(T x)
            }

            interface IEmptyStack<T> extends IStack<T> {
                INonEmptyStack<T, IEmptyStack<T>> push(T x)
            }

            interface INonEmptyStack<T, TStackBeneath extends IStack<T>> extends IStack<T> {
                T getTop()

                TStackBeneath pop()

                INonEmptyStack<T, INonEmptyStack<T, TStackBeneath>> push(T x)
            }

            class EmptyStack<T> implements IEmptyStack<T> {
                INonEmptyStack<T, IEmptyStack<T>> push(T x) {
                    new NonEmptyStack<T, IEmptyStack<T>>(x, this)
                }
            }

            class NonEmptyStack<T, TStackBeneath extends IStack<T>>
                    implements INonEmptyStack<T, TStackBeneath> {
                private final TStackBeneath stackBeneathTop;
                private final T top

                NonEmptyStack(T top, TStackBeneath stackBeneathTop) {
                    this.top = top
                    this.stackBeneathTop = stackBeneathTop
                }

                T getTop() {
                    top
                }

                TStackBeneath pop() {
                    stackBeneathTop
                }

                INonEmptyStack<T, INonEmptyStack<T, TStackBeneath>> push(T x) {
                    new NonEmptyStack<T, INonEmptyStack<T, TStackBeneath>>(x, this)
                }
            }

            final IStack<Integer> stack = new EmptyStack<Integer>()

            def oneInteger = stack.push(1)
            assert oneInteger.getTop() == 1

            def twoIntegers = stack.push(1).push(2)
            assert twoIntegers.getTop() == 2

            def oneIntegerAgain = stack.push(1).push(2).pop()
            assert oneIntegerAgain.getTop() == 1 // BOOM!!!!
        '''
    }

    // GROOVY-5758
    void testShouldNotForbidAssignmentToString() {
        assertScript '''
            class A {
                public String foo
            }
            new A().foo = new ArrayList()
        '''
    }

    // GROOVY-5735
    void testCorrespondingParameterType() {
        assertScript '''
        public <T> void someMethod (java.lang.Class<T> clazz, T object) {}

        void method() {
            List<String> list = null
            someMethod(java.util.List.class, list)
        }

        method()
        '''
    }

    void testCorrectlyBoundedByWildcardGenericParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<?>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        '''
    }

    void testCorrectlyBoundedByExtendsGenericParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        '''
    }

    void testCorrectlyBoundedBySuperGenericParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            Foo.bar([new Object()])
        '''
    }

    void testCorrectlyBoundedByExtendsPlaceholderParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<? extends String>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux(['abc'])
        '''
    }

    void testCorrectlyBoundedBySuperPlaceholderParameterType() {
        assertScript '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<? super Object>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux([new Object()])
        '''
    }

    void testCorrectlyBoundedSubtypeGenericParameterType() {
        assertScript '''
            class Foo {
                static <T extends Collection<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        '''
    }

    void testOutOfBoundsByExtendsGenericParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            Foo.bar([new Object()])
        ''', 'Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.List <java.lang.Object>]'
    }

    void testOutOfBoundsBySuperGenericParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            Foo.bar(['abc'])
        ''', 'Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.List <java.lang.String>]'
    }

    void testOutOfBoundsByExtendsPlaceholderParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? extends CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<Object>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux([new Object()])
        ''', 'Cannot call <T extends java.util.List<? extends java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.List <Object>]'
    }

    void testOutOfBoundsBySuperPlaceholderParameterType() {
        shouldFailWithMessages '''
            class Foo {
                static <T extends List<? super CharSequence>> void bar(T a) {}
            }
            class Baz {
                static <T extends List<String>> void qux(T a) {
                    Foo.bar(a)
                }
            }
            Baz.qux(['abc'])
        ''', 'Cannot call <T extends java.util.List<? super java.lang.CharSequence>> Foo#bar(T) with arguments [java.util.List <String>] '
    }

    // GROOVY-5721
    void testExtractComponentTypeFromSubclass() {
        assertScript '''
        class MyList extends ArrayList<Double> {}

        List<Double> list1 = new ArrayList<Double>()
        list1 << 0.0d

        // OK
        Double d1 = list1.get(0)

        //---------------------------

        List<Double> list2 = new MyList()
        list2 << 0.0d

        //Groovyc: [Static type checking] - Cannot assign value of type java.lang.Object to variable of type java.lang.Double
        Double d2 = list2.get(0)

        //---------------------------

        MyList list3 = new MyList()
        list3 << 0.0d

        //Groovyc: [Static type checking] - Cannot assign value of type java.lang.Object to variable of type java.lang.Double
        Double d3 = list3.get(0)
        '''

    }

    // GROOVY-5724
    void testJunitHamcrest() {
        assertScript '''
            public class Matcher<T> {}
            public <T> void assertThat(T obj, Matcher<T> matcher) {}
            public <T> Matcher<T> notNullValue() {}
            String result = '12345'.substring(2)
            // assert
            assertThat(result, notNullValue())
        '''
    }

    // GROOVY-5836
    void testShouldFindMethodEvenIfUsingGenerics() {
        assertScript '''
            class Test<T> {
                void transform(boolean passThroughNulls, Closure<T> mapper) {}
                void transformAll(boolean passThroughNulls, Closure<T>... mappers) {
                    for (m in mappers) {
                        transform passThroughNulls, m
                    }
                }
            }
            new Test()
        '''
    }

    // GROOVY-5893
    @NotYetImplemented
    void testPlusInClosure() {
        assertScript '''
        def list = [1, 2, 3]

        @ASTTest(phase=INSTRUCTION_SELECTION,value={
            assert node.getNodeMetaData(INFERRED_TYPE) == int_TYPE
        })
        def sum = 0
        list.each { int i -> sum = sum+i }
        assert sum == 6

        sum = 0
        list.each { int i -> sum += i }
        assert sum == 6

        @ASTTest(phase=INSTRUCTION_SELECTION, value={
            assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
        })
        def sumWithInject = list.inject(0, { int x, int y -> x + y })
        sum = sumWithInject
        assert sum == 6
        '''
    }

    void testShouldNotCreateStackOverflow() {
        assertScript '''
            class Element {
              Iterator<List<Element>> multi() {
                [ [ ] ].iterator()
              }
            }
            new Element()
'''
    }

    void testRegressionInConstructorCheck() {
        assertScript '''
            new ArrayList(['a','b','c'].collect { String it -> it.toUpperCase()})
        '''
    }

    void testReturnTypeInferenceWithMethodUsingWildcard() {
        assertScript '''
            public Object createInstance(Class<?> projectComponentClass, String foo) { projectComponentClass.newInstance() }
            createInstance(LinkedList, 'a')
        '''
    }

    // GROOVY-6051
    void testGenericsReturnTypeInferenceShouldNotThrowNPE() {
        assertScript '''
        class Bar {
          public static List<Date> bar(List<Date> dummy) {}
        }
        class Foo extends Bar {
            static public Date genericItem() {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def inft = node.getNodeMetaData(INFERRED_TYPE)
                    assert inft == make(List)
                    assert inft.genericsTypes[0].type == make(Date)
                })
                def res = bar(null)

                res[0]
            }
        }
        new Foo()
        '''
    }

    // GROOVY-6035
    void testReturnTypeInferenceWithClosure() {
        assertScript '''import org.codehaus.groovy.ast.expr.ClosureExpression
        class CTypeTest {

          public static void test1(String[] args) {

            // Cannot assign value of type java.lang.Object to variable of type CTypeTest
            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def cl = node.rightExpression.arguments[0]
                assert cl instanceof ClosureExpression
                def type = cl.getNodeMetaData(INFERRED_TYPE)
                assert type == make(Closure)
                assert type.isUsingGenerics()
                assert type.genericsTypes
                assert type.genericsTypes[0].type.name == 'CTypeTest'

                type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.name == 'CTypeTest'
            })
            def s1 = cache  {
              return new CTypeTest();
            }

            CTypeTest s2 = cache {
                new CTypeTest()
            }

          }


          static <T> T cache(Closure<T> closure) {
            return closure.call();
          }

        }
        1
        '''
    }

    // GROOVY-6129
    void testShouldNotThrowNPE() {
        assertScript '''
            def map = new HashMap<>()
            map.put(1, 'foo')
            map.put('bar', new Date())
        '''
    }

    // GROOVY-6232
    void testDiamond() {
        assertScript '''
            class Foo<T>{  Foo(T a, T b){} }
            def bar() {
                Foo<Object> f = new Foo<>("a",new Object())
            }
            bar()
        '''
    }

    // GROOVY-6233
    void testConstructorArgumentsAgainstGenerics() {
        shouldFailWithMessages '''
            class Foo<T>{  Foo(T a, T b){} }
            def bar() {
                Foo<Map> f = new Foo<Map>("a",1)
            }
            bar()
        ''', '[Static type checking] - Cannot find matching method Foo#<init>(java.lang.String, int)'
    }

    // Groovy-5742
    void testNestedGenerics() {
        assertScript '''
            import static Next.*

            abstract class Base<A extends Base<A>> {}
            class Done extends Base<Done> { }
            class Next<H, T extends Base<T>> extends Base<Next<H, T>> {
                H head; T tail
                static <H, T extends Base<T>> Next<H, T> next(H h, T t) { new Next<H, T>(head:h, tail:t) }
                String toString() { "Next($head, ${tail.toString()})" }
            }

            Next<Integer, Next<String, Done>> x = next(3, next("foo", new Done()))
        '''
    }

    void testMethodLevelGenericsFromInterface() {
        assertScript '''
            interface A {
                public <T> T getBean(Class<T> c)
            }
            interface B extends A {}
            interface C extends B {}

            void foo(C c) {
                String s = c?.getBean("".class)
            }
            foo(null)
            true
        '''
    }

    // Groovy-5610
    void testMethodWithDefaultArgument() {
        assertScript '''
            class A{}
            class B extends A{}
            def foo(List<? extends A> arg, String value='default'){1}

            List<B> b = new ArrayList<>()
            assert foo(b) == 1
            List<A> a = new ArrayList<>()
            assert foo(a) == 1
        '''

        shouldFailWithMessages '''
            class A{}
            class B extends A{}
            def foo(List<? extends A> arg, String value='default'){1}

            List<Object> l = new ArrayList<>()
            assert foo(l) == 1
        ''',
        '#foo(java.util.List <A extends A>) with arguments [java.util.ArrayList <java.lang.Object>]'
    }

    void testMethodLevelGenericsForMethodCall() {
        // Groovy-5891
        assertScript '''
            public <T extends List<Integer>> T foo(Class<T> type, def x) {
                return type.cast(x)
            }
            def l = [1,2,3]
            assert foo(l.class, l) == l
        '''
        assertScript '''
            public <T extends Runnable> T foo(Class<T> type, def x) {
                return type.cast(x)
            }
            def cl = {1}
            assert foo(cl.class, cl) == cl
         '''
         assertScript '''
            public <T extends Runnable> T foo(Class<T> type, def x) {
                return type.cast(x) as T
            }
            def cl = {1}
            assert foo(cl.class, cl) == cl
         '''
         //GROOVY-5885
         assertScript '''
            class Test {
                public <X extends Test> X castToMe(Class<X> type, Object o) {
                    return type.cast(o);
                }
            }
            def t = new Test()
            assert t.castToMe(Test, t)  == t
         '''
    }

    // Groovy-5839
    void testMethodShadowGenerics() {
        shouldFailWithMessages '''
            public class GoodCodeRed<T> {
                Collection<GoodCodeRed<T>> attached = []
                public <T> void attach(GoodCodeRed<T> toAttach) {
                    attached.add(toAttach)
                }
                static void foo() {
                    def g1 = new GoodCodeRed<Long>()
                    def g2 = new GoodCodeRed<Integer>()
                    g1.attach(g2);
                }
            }
            GoodCodeRed.foo()
        ''',
        "Cannot call <T> GoodCodeRed <Long>#attach(GoodCodeRed <Long>) with arguments [GoodCodeRed <Integer>]"
    }

    void testHiddenGenerics() {
        // Groovy-6237
        assertScript '''
            class MyList extends LinkedList<Object> {}
            List<Object> o = new MyList()
        '''

        shouldFailWithMessages '''
            class Blah {}
            class MyList extends LinkedList<Object> {}
            List<Blah> o = new MyList()
        ''','Incompatible generic argument types. Cannot assign MyList to: java.util.List <Blah>'

        // Groovy-5873
        assertScript """
            abstract class Parent<T> {
                public T value
            }
            class Impl extends Parent<Integer> {}
            Impl impl = new Impl()
            Integer i = impl.value
        """

        // GROOVY-5920
        assertScript """
            class Data<T> {
              T value
            }

            class StringDataIterator implements Iterator<Data<String>> {
              boolean hasNext() { true }
              void    remove()  {}
              Data<String> next() {
                new Data<String>( value: 'tim' )
              }
            }

            class Runner {
              static main( args ) {
                Data<String> elem = new StringDataIterator().next()
                assert elem.value.length() == 3
              }
            }
            Runner.main(null);
        """
    }

    void testReturnTypeInferenceRemovalWithGenerics() {
        assertScript '''
            class SynchronousPromise<T> {
                Closure<T> callable
                Object value

                SynchronousPromise(Closure<T> callable) {
                    this.callable = callable
                }

                T get() throws Throwable {
                    @ASTTest(phase=INSTRUCTION_SELECTION,value={
                        assert node.getNodeMetaData(INFERRED_TYPE) == OBJECT_TYPE
                    })
                    value=callable.call()
                    return value
                }
            }

            def promise = new SynchronousPromise({ "Hello" })
            promise.get()
        '''
    }

    // GROOVY-6455
    void testDelegateWithGenerics() {
        assertScript '''
            @groovy.transform.CompileStatic
            class IntList {
                @Delegate List<Integer> delegate = new ArrayList<Integer>()
            }
            def l = new IntList()
            assert l == []
        '''
    }

    // GROOVY-6504
    void testInjectMethodWithInitialValueChoosesTheCollectionVersion() {
        assertScript '''import org.codehaus.groovy.transform.stc.ExtensionMethodNode
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'inject'
                assert method instanceof ExtensionMethodNode
                method = method.extensionMethodNode
                assert method.parameters[0].type == make(Collection)
            })
            def result = ['a','bb','ccc'].inject(0) { int acc, String str -> acc += str.length(); acc }
            assert  result == 6
        '''
    }

    // GROOVY-6504
    void testInjectMethodWithInitialValueChoosesTheCollectionVersionUsingDGM() {
        assertScript '''import org.codehaus.groovy.runtime.DefaultGroovyMethods
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def method = node.rightExpression.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                assert method.name == 'inject'
                assert method.parameters[0].type == make(Collection)
            })
            def result = DefaultGroovyMethods.inject(['a','bb','ccc'],0, { int acc, String str -> acc += str.length(); acc })
            assert  result == 6
        '''
    }

    // GROOVY-6760
    void testGenericsAtMethodLevelWithGenericsInTypeOfGenericType() {
        assertScript '''
            @Grab(group='com.netflix.rxjava', module='rxjava-core', version='0.18.1')
            import rx.Observable
            import java.util.concurrent.Callable

            static <T> Observable<T> observe(Callable<Iterable<T>> callable) {
                Observable.from(callable.call())
            }
            observe({ ["foo"] }) map {
                it.toUpperCase() // <- compiler doesn't know 'it' is a string
            } subscribe {
                assert it == "FOO"
            }
        '''
    }

    // GROOVY-6135
    void testGenericField() {
        assertScript '''
            class MyClass {
                static void main(args) {
                    Holder<Integer> holder = new Holder<Integer>()
                    holder.value = 5
                    assert holder.value > 4
                }
                private static class Holder<T> {
                    T value
                }
            }
        '''
    }

    //GROOVY-6723, GROOVY-6415
    void testIndirectMethodLevelGenerics() {
        assertScript '''
            class C1<A> {
                def void m1(A a) {C1.m2(a)}
                static <B> void m2(B b) {}
            }
            new C1().m1(null) // the call does not really matter
        '''
        assertScript '''
            class Test1 {
                static <A, B> void pair1(A a, B b) {}
                static <A, B> void pair2(A a, B b) {pair1(a, a)}
                static <A> List<A> list1(A a) {[a]}
                static <B> List<B> list2(B b) {list1(b)}
                static <A> List<A> list3(A a) {list1(a)}
            }
            Test1.pair2(1,2) // the call does not really matter
        '''

        assertScript '''
            class Foo {
                String method() {
                    return callT('abc')
                }

                private <T> T callT(T t) {
                    return callV(t)
                }

                private <V> V callV(V v) {
                    return v
                }
            }

            println new Foo().method()
        '''
    }

    // GROOVY-6358
    void testGenericsReturnedFromStaticMethodWithInnerClosureAndAsType() {
        assertScript '''
            import java.lang.reflect.Method

            interface Ifc {
               void method()
            }
            class Generator {
                static <T> T create (Class<T> clazz ){
                    return clazz.methods.collectEntries { Method method ->
                            [ (method.name) : { println "${method.name} called"} ]
                        }.asType(clazz)
                }
            }
            class User {
                static void main() {
                    Ifc ifc = Generator.create(Ifc)
                    ifc.method()
                }
            }

            User.main()
        '''
    }

    void testConcreteTypeInsteadOfGenerifiedInterface() {
        assertScript '''
            import groovy.transform.ASTTest
            import static org.codehaus.groovy.transform.stc.StaticTypesMarker.*
            import static org.codehaus.groovy.ast.ClassHelper.*

            interface Converter<F, T> {
            T convertC(F from)
            }
            class Holder<T> {
               T thing
               Holder(T thing) {
                this.thing = thing
               }
               def <R> Holder<R> convertH(Converter<? super T, ? extends R> func1) {
                  new Holder(func1.convertC(thing))
               }
            }
            class IntToFloatConverter implements Converter<Integer,Float> {
                public Float convertC(Integer from) { from.floatValue() }
            }
            void foo() {
                @ASTTest(phase=INSTRUCTION_SELECTION,value={
                    def holderType = node.getNodeMetaData(INFERRED_TYPE)
                    assert holderType.genericsTypes[0].type == Float_TYPE
                })
                def h1 = new Holder<Integer>(2).convertH(new IntToFloatConverter())
            }
            foo()
        '''
    }

    // GROOVY-6748
    void testCleanGenerics() {
        assertScript '''
            class Class1 {
                static <A, B> void method1(A a, B b) {
                    method2(a, b)
                }
                static <A, B> void method2(A a, B b) {}
                static <A, B> void method3(List<A> list1, List<B> list2) {
                    method1(list1.get(0), list2.get(0))
                }
            }
            new Class1().method3(["a"],["b"])
        '''
    }

    // GROOVY-6761
    void testInVariantAndContraVariantGenerics() {
        assertScript '''
            class Thing {
              public <O> void contravariant(Class<? super O> type, O object) {}
              public <O> void invariant(Class<O> type, O object) {}
              void m() {
                invariant(String, "foo")
                contravariant(String, "foo") // fails, can't find method
              }
            }
            new Thing().m()
        '''
    }

    // GROOVY-6731
    void testContravariantMethodResolution() {
        assertScript '''interface Function<T, R> {

    R apply(T t)

}
public <I, O> void transform(Function<? super I, ? extends O> function) { function.apply('')}

String result = null
transform(new Function<String, String>() {

    String apply(String input) {
        result = "ok"
    }
})

assert result == 'ok\''''
    }
    void testContravariantMethodResolutionWithImplicitCoercion() {
        assertScript '''interface Function<T, R> {

    R apply(T t)

}
public <I, O> void transform(Function<? super I, ? extends O> function) { function.apply('')}

String result = null
transform {
        result = "ok"
}


assert result == 'ok'
'''
    }

    void testGROOVY5981(){
        assertScript '''
            import javax.swing.*
            import java.awt.*

            class ComponentFixture<T extends Component> {}
            class JButtonFixture extends ComponentFixture<JButton> {}
            class ContainerFixture<T extends Container> extends ComponentFixture<T> {}
            abstract class ComponentAdapter<Fixture extends ComponentFixture> {
                Fixture getFixture() {
                    return fixture
                }
            }
            abstract class ContainerAdapter<Fixture extends ContainerFixture> extends ComponentAdapter<Fixture> {}

            class ButtonComponent extends ComponentAdapter<JButtonFixture> {
                void setFixtureResolver(final ContainerAdapter<? extends ContainerFixture> containerAdapter) {
                    final ContainerFixture containerFixture = containerAdapter.getFixture()
                }
            }

            new ButtonComponent()
        '''
    }

    // GROOVY-6856
    void testReturnTypeFitsInferredTypeWithBound() {
        assertScript '''
            class Wrapper {}

            class Foo<W extends Wrapper> {
                W doIt (List<W> l) {
                    l.iterator().next()
                }
            }
            Wrapper w = new Wrapper()
            assert new Foo<Wrapper>().doIt([w]) == w
        '''
    }

    void testReturnTypeChecking() {
        shouldFailWithMessages '''
            class Foo {
                List<String> run() {
                    [11, 12]
                }
            }
        ''', 'Incompatible generic argument types. Cannot assign java.util.List <java.lang.Integer> to: java.util.List <String>'
    }

    void testBoundedReturnTypeChecking() {
        assertScript '''
            class Foo {
                List<? extends Serializable> run() {
                    [1, 'a']
                }
            }
            null
        '''
    }


    //GROOVY-7804
    void testParameterlessClosureToGenericSAMTypeArgumentCoercion() {
        assertScript '''
            interface Supplier<T> {
                public <T> T get()
            }

            static <T> T doGet(Supplier<T> supplier) { supplier.get() }

            assert doGet { -> 'foo' } == 'foo'
        '''
    }

    //GROOVY-7713
    void testClosureReturnNull() {
        assertScript '''
            Closure<String> cl = {
                if (hashCode() > 0) {
                    return null
                }
                'foo'
            }
        '''
    }

    static class MyList extends LinkedList<String> {}

    public static class ClassA<T> {
        public <X> Class<X> foo(Class<X> classType) {
            return classType;
        }

        public <X> Class<X> bar(Class<T> classType) {
            return null;
        }
    }

    public static class JavaClassSupport {
        public static class Container<T> {
        }

        public static class StringContainer extends Container<String> {
        }

        public static <T> List<T> unwrap(Collection<? extends Container<T>> list) {
        }
    }

}

