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

import groovy.transform.PackageScope
import org.codehaus.groovy.control.customizers.ImportCustomizer
import org.codehaus.groovy.tools.javac.JavaAwareCompilationUnit

/**
 * Unit tests for static type checking : fields and properties.
 */
class FieldsAndPropertiesSTCTest extends StaticTypeCheckingTestCase {

    @Override
    protected void configure() {
        config.addCompilationCustomizers(
            new ImportCustomizer().addImports('groovy.transform.PackageScope')
        )
    }

    void testAssignFieldValue() {
        assertScript '''
            class C { int x }
            C c = new C()
            c.x = 1
        '''
        shouldFailWithMessages '''
            class C { int x }
            C c = new C()
            c.x = '1'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testAssignFinalFieldValue() {
        assertScript '''
            class C {
                final x = null
            }
            new C().x
        '''
        assertScript '''
            class C {
                final x
                C() {
                    x = null
                }
            }
            new C().x
        '''
        assertScript '''
            class C {
                final x
                C(def x) {
                    this.x = x
                }
            }
            new C(null).x
        '''
        assertScript '''
            class C {
                final x;
                {
                    x = null
                }
            }
            new C().x
        '''
        assertScript '''
            class C {
                final x;
                {
                    this.x = x
                }
            }
            new C().x
        '''
        assertScript '''
            class C {
                static final x
                static {
                    this.x = null
                }
            }
            new C().x
        '''
    }

    void testAssignFinalFieldValue2() {
        shouldFailWithMessages '''
            int[] array = []
            array.length = 1
        ''',
        'Cannot set read-only property: length'

        shouldFailWithMessages '''
            class C { final x }
            new C().x = null
        ''',
        'Cannot set read-only property: x'

        // GROOVY-5450
        shouldFailWithMessages '''
            class C { final x }
            new C().@x = null
        ''',
        'Cannot set read-only property: x'

        shouldFailWithMessages '''
            class C { final x }
            new C().with { x = null }
        ''',
        'Cannot set read-only property: x'

        shouldFailWithMessages '''
            class C { final x }
            new C().with { delegate.x = null }
        ''',
        'Cannot set read-only property: x'

        shouldFailWithMessages '''
            class C { final x }
            new C().setX(null)
        ''',
        'Cannot find matching method C#setX(<unknown parameter type>).'
    }

    void testInferenceFromFieldType() {
        assertScript '''
            class C {
                String name = 'Cedric'
            }
            C c = new C()
            def x = c.name
            x.toUpperCase() // type of x should be inferred from field type
        '''
    }

    void testAssignFieldValueWithAttributeNotation() {
        assertScript '''
            class C {
                int x
            }
            C c = new C()
            c.@x = 1
        '''
    }

    void testAssignFieldValueWithWrongTypeAndAttributeNotation() {
         shouldFailWithMessages '''
             class C {
                 int x
             }
             C c = new C()
             c.@x = '1'
         ''',
         'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testInferenceFromAttributeType() {
        assertScript '''
            class C {
                String name = 'Cedric'
            }
            C c = new C()
            def x = c.@name
            x.toUpperCase() // type of x should be inferred from field type
        '''
    }

    void testShouldComplainAboutMissingProperty() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.x = 0
        ''',
        'No such property: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingProperty2() {
        shouldFailWithMessages '''
            class C {
            }
            C c = new C()
            c.x = 0
        ''',
        'No such property: x for class: C'
    }

    // GROOVY-11319
    void testShouldComplainAboutMissingProperty3() {
        shouldFailWithMessages '''
            class C {
                private int getX() { 1 }
            }
            class D extends C {
                void test() {
                    super.x
                }
            }
            new D().test()
        ''',
        'No such property: x for class: C'
    }

    // GROOVY-11319
    void testShouldComplainAboutMissingProperty4() {
        shouldFailWithMessages '''
            class C {
                private void setX(int i) {
                    assert false : 'cannot access'
                }
            }
            class D extends C {
                void test() {
                    super.x = 1
                }
            }
            new D().test()
        ''',
        'No such property: x for class: C'
    }

    void testShouldComplainAboutMissingProperty5() {
        shouldFailWithMessages '''
            class C {
                private x
            }
            class D extends C {
                void test() {
                    this.x
                }
            }
            new D().test()
        ''',
        'No such property: x for class: D'
    }

    void testShouldComplainAboutMissingAttribute() {
        shouldFailWithMessages '''
            Object o = new Object()
            o.@x = 0
        ''',
        'No such attribute: x for class: java.lang.Object'
    }

    void testShouldComplainAboutMissingAttribute2() {
        shouldFailWithMessages '''
            class C {
            }
            C c = new C()
            c.@x = 0
        ''',
        'No such attribute: x for class: C'
    }

    void testShouldComplainAboutMissingAttribute3() {
        shouldFailWithMessages '''
            class C {
                def getX() { }
            }
            C c = new C()
            println c.@x
        ''',
        'No such attribute: x for class: C'
    }

    void testShouldComplainAboutMissingAttribute4() {
        shouldFailWithMessages '''
            class C {
                def setX(x) { }
            }
            C c = new C()
            c.@x = 0
        ''',
        'No such attribute: x for class: C'
    }

    void testShouldComplainAboutMissingAttribute5() {
        shouldFailWithMessages '''
            class C {
                private x
            }
            class D extends C {
                void test() {
                    this.@x
                }
            }
            new D().test()
        ''',
        'Cannot access field: x of class: C'
    }

    void testPropertyWithInheritance() {
        assertScript '''
            class C {
                int x
            }
            class D extends C {
            }
            D d = new D()
            assert d.x == 0
            d.x = 2
            assert d.x == 2
        '''
    }

    void testPropertyTypeWithInheritance() {
        shouldFailWithMessages '''
            class C {
                int x
            }
            class D extends C {
            }
            D d = new D()
            d.x = '2'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testPropertyWithInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class C extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            C c = new C()
            c.x = 2
        '''
    }

    void testPropertyWithInheritanceFromAnotherSourceUnit2() {
        shouldFailWithMessages '''
            class C extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass {
            }
            C c = new C()
            c.x = '2'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testPropertyWithSuperInheritanceFromAnotherSourceUnit() {
        assertScript '''
            class C extends groovy.transform.stc.FieldsAndPropertiesSTCTest.BaseClass2 {
            }
            C c = new C()
            c.x = 2
        '''
    }

    // GROOVY-9955
    void testStaticPropertyWithInheritanceFromAnotherSourceUnit() {
        assertScript """import ${Public.canonicalName}
            assert Public.answer == 42
            assert Public.CONST == 'XX'
            assert Public.VALUE == null
            Public.VALUE = 'YY'
            assert Public.VALUE == 'YY'
            Public.@VALUE = 'ZZ'
            assert Public.@VALUE == 'ZZ'
            Public.VALUE = null
        """
    }

    // GROOVY-10695
    void testStaticPropertyOfSelfType() {
        for (qual in ['', 'this.', 'C.']) {
            assertScript """
                class C {
                    private static Object value
                    static Object getValue() {
                        ${qual}value
                    }
                    static void setValue(v) {
                        ${qual}value = v
                    }
                }
                C.setValue(null) // StackOverflowError
            """
        }
    }

    void testDateProperties() {
        assertScript '''
            Date d = new Date()
            def time = d.time
            d.time = 0
        '''
    }

    void testGetterForProperty() {
        assertScript '''
            class C {
                String p
            }
            def x = new C().getP()
            x = x?.toUpperCase()
        '''
    }

    // GROOVY-5232
    void testSetterForProperty() {
        assertScript '''
            class Person {
                String name
                static Person create() {
                    def p = new Person()
                    p.setName("Guillaume")
                    return p
                }
            }
            Person.create()
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldPass() {
        assertScript '''
            class C {
                int bar = 1
            }
            new C()
        '''
    }

    // GROOVY-5443
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypes() {
        shouldFailWithMessages '''
            class C {
                int bar = new Date()
            }
            new C()
        ''',
        'Cannot assign value of type java.util.Date to variable of type int'
    }

    // GROOVY-5443, GROOVY-10277
    void testFieldInitShouldNotPassBecauseOfIncompatibleTypesWithClosure() {
        shouldFailWithMessages '''
            class C {
                Closure<List> bar = { Date date -> date.getTime() }
            }
            new C()
        ''',
        'Cannot return value of type long for closure expecting java.util.List'

        shouldFailWithMessages '''
            class C {
                java.util.function.Supplier<String> bar = { -> 123 }
            }
            new C()
        ''',
        'Cannot return value of type int for closure expecting java.lang.String'
    }

    // GROOVY-9882
    void testFieldInitShouldPassForCompatibleTypesWithClosure() {
        assertScript '''
            class C {
                java.util.function.Supplier<String> bar = { 'abc' }
            }
            assert new C().bar.get() == 'abc'
        '''
    }

    void testClosureParameterMismatch() {
        shouldFailWithMessages '''
            class C {
                java.util.function.Supplier<String> bar = { baz -> '' }
            }
        ''',
        'Wrong number of parameters for method target: get()'

        shouldFailWithMessages '''
            class C {
                java.util.function.Consumer<String> bar = { -> null }
            }
        ''',
        'Wrong number of parameters for method target: accept(java.lang.String)'
    }

    // GROOVY-5585
    void testClassPropertyOnInterface() {
        assertScript '''
            Class test(Serializable arg) {
                Class<?> clazz = arg.class
                clazz
            }
            assert test('foo') == String
        '''
        assertScript '''
            Class test(Serializable arg) {
                Class<?> clazz = arg.getClass()
                clazz
            }
            assert test('foo') == String
        '''
    }

    // GROOVY-10380
    void testGetterUsingPropertyNotation() {
        assertScript '''
            class C {
                def getFoo(foo = 'foo') { foo }
            }
            def c = new C()
            def v = c.foo
            assert v == 'foo'
        '''
    }

    void testSetterUsingPropertyNotation() {
        assertScript '''
            class C {
                boolean set
                void setFoo(foo) { set = (foo == 'foo') }
            }
            def c = new C()
            c.foo = 'foo'
            assert c.set
        '''
    }

    void testSetterUsingPropertyNotation2() {
        assertScript '''
            interface FooAware { void setFoo(String arg) }
            class C implements FooAware {
                void setFoo(String foo) { }
            }
            void test(FooAware fa) {
                fa.foo = 'foo'
            }
            def c = new C()
            test(c)
        '''
    }

    // GROOVY-11372
    void testSetterUsingPropertyNotation3() {
        assertScript '''
            def baos = new ByteArrayOutputStream()
            assert baos.size() == 0
            baos.bytes= new byte[1]
            assert baos.size() == 1
        '''
    }

    void testListDotProperty1() {
        assertScript '''class Elem { int value }
            List<Elem> list = new LinkedList<Elem>()
            list.add(new Elem(value:123))
            list.add(new Elem(value:456))
            assert list.value == [ 123, 456 ]
            list.add(new Elem(value:789))
            assert list.value == [ 123, 456, 789 ]
        '''
        assertScript '''class Elem { String value }
            List<Elem> list = new LinkedList<Elem>()
            list.add(new Elem(value:'123'))
            list.add(new Elem(value:'456'))
            assert list.value == [ '123', '456' ]
            list.add(new Elem(value:'789'))
            assert list.value == [ '123', '456', '789' ]
        '''
    }

    void testListDotProperty2() {
        assertScript '''
            class C { int x }
            def list = [new C(x:1), new C(x:2)]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                def type = node.getNodeMetaData(INFERRED_TYPE)
                assert type.toString(false) == 'java.util.List<java.lang.Integer>'
            })
            def x = list.x
            assert x == [1,2]
        '''
        assertScript '''
            void test(List list) {
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    def type = node.getNodeMetaData(INFERRED_TYPE)
                    assert type.toString(false) == 'java.lang.Class<? extends java.lang.Object>'
                })
                def c = list.class
            }
            test([])
        '''
    }

    // GROOVY-5700
    void testMapPropertyAccess1() {
        assertScript '''
            def map = [key: 123]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def val = map.key
            assert val == 123
        '''
    }

    // GROOVY-8788
    void testMapPropertyAccess2() {
        assertScript '''
            def map = [key: 123]
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def val = map['key']
            assert val == 123
        '''
    }

    // GROOVY-5988
    void testMapPropertyAccess3() {
        assertScript '''
            String key = 'name'
            Map<String, Integer> map = [:]
            map[key] = 123
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Integer_TYPE
            })
            def val = map[key]
            assert val == 123
        '''
    }

    // GROOVY-5797
    void testMapPropertyAccess4() {
        assertScript '''
            def test(Map foo) {
                def map = [baz: 1]
                map[ foo.bar ]
            }
            assert test(bar:'baz') == 1
        '''
    }

    // GROOVY-11367, GROOVY-11369, GROOVY-11372, GROOVY-11384
    void testMapPropertyAccess5() {
        assertScript '''
            def map = [:]
            assert map.entry      == null
            assert map.empty      == null
            assert map.class      == null
            assert map.metaClass  != null
            assert map.properties != null

            map.entry      = null
            map.empty      = null // not read-only property
            map.class      = null // not read-only property
            map.metaClass  = null // not read-only property

            assert  map.containsKey('entry')
            assert  map.containsKey('empty')
            assert  map.containsKey('class')
            assert !map.containsKey('metaClass')
        '''
        shouldFailWithMessages '''
            def map = [:]
            map.properties = null
        ''',
        'Cannot set read-only property: properties'

        assertScript '''
            Map<String,Number> map = [:]

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Number_TYPE
            })
            def e = map.entry

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Number_TYPE
            })
            def a = map.empty

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == Number_TYPE
            })
            def b = map.class

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == METACLASS_TYPE
            })
            def c = map.metaClass

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == MAP_TYPE
            })
            def d = map.properties
        '''
        assertScript '''
            void test(Map map) {
                def str = ''
                str += map.empty
                str += map.with{ empty }
                str += map.with{ delegate.empty }
                str += map.with{ {->owner.empty}() }
                assert str == 'entryentryentryentry'
            }
            test( [:].withDefault{ 'entry' } )
        '''
    }

    // GROOVY-8074
    void testMapPropertyAccess6() {
        assertScript '''
            class C extends HashMap {
                def foo = 1
            }
            def map = new C()
            map.put('foo', 11)
            assert map.foo == 1
            assert map['foo'] == 1
        '''
        assertScript """
            def map = new ${MapType.name}()
            map.put('foo', 11)
            assert map.foo == 1
            assert map['foo'] == 1
            map.put('bar', 22)
            assert map.bar == 22
            assert map['bar'] == 22
            map.put('baz', 33)
            assert map.baz == 33
            assert map['baz'] == 33
        """
    }

    // GROOVY-5001, GROOVY-5491, GROOVY-6144
    void testMapPropertyAccess7() {
        String types = '''
            class A { }
            class B { }
            class C extends HashMap<String,A> {
                B b = new B()
            }
        '''
        assertScript types + '''
            def map = new C()
            map.put('a', new A())
            assert map.get('a') != null
            assert map.get('b') == null
            A a = map.a
            B b = map.b
            a = map['a']
            b = map['b']
            assert a instanceof A
            assert b instanceof B
        '''
        assertScript types + '''
            def test(C map) {
                A a = map.a
                B b = map.b
                a = map['a']
                b = map['b']
                assert a instanceof A
                assert b instanceof B
            }
            test(new C().tap{ put('a', new A()) })
        '''
    }

    // GROOVY-5517
    void testMapPropertyAccess8() {
        String type = '''
            @groovy.transform.stc.POJO
            @groovy.transform.CompileStatic
            class C extends HashMap {
                public static int version = 666
            }
        '''
        assertScript type + '''
            def map = new C()
            map.foo = 123
            def value = map.foo
            assert value == 123
            map['foo'] = 4.5
            value = map['foo']
            assert value == 4.5
            value = map.version
            assert value == 666
        '''
        assertScript type + '''
            def test(C map) {
                map.foo = 123
                def value = map.foo
                assert value == 123
                map['foo'] = 4.5
                value = map['foo']
                assert value == 4.5
                value = map.version
                assert value == 666
            }
            test(new C())
        '''
    }

    // GROOVY-11367
    void testMapPropertyAccess9() {
        for (mode in ['','static']) {
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode v = 'field'
                    public        $mode w = 'field'
                    protected     $mode x = 'field'
                    @PackageScope $mode y = 'field'
                    private       $mode z = 'field'
                }
                def map = new M()
                assert map.m === map.@m
                assert map.v == 'field'
                assert map.w == 'field'
                assert map.x == 'entry'
                assert map.y == 'entry'
                assert map.z == 'entry'
                map.with {
                    assert v == 'field'
                    assert w == 'field'
                    assert x == 'entry'
                    assert y == 'entry'
                    assert z == 'entry'
                }
            """
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode getV() { 'getter' }
                    public        $mode getW() { 'getter' }
                    protected     $mode getX() { 'getter' }
                    @PackageScope $mode getY() { 'getter' }
                    private       $mode getZ() { 'getter' }
                }
                def map = new M()
                assert map.v == 'getter'
                assert map.w == 'getter'
                assert map.x == 'entry'
                assert map.y == 'entry'
                assert map.z == 'entry'
                map.with {
                    assert v == 'getter'
                    assert w == 'getter'
                    assert x == 'entry'
                    assert y == 'entry'
                    assert z == 'entry'
                }
            """
        }
    }

    // GROOVY-11403
    void testMapPropertyAccess10() {
        for (mode in ['','static']) {
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode v = 'field'
                    public        $mode w = 'field'
                    protected     $mode x = 'field'
                    @PackageScope $mode y = 'field'
                    private       $mode z = 'field'
                    void test() {
                        assert m    === this.@m
                        assert v     == 'field'
                        assert w     == 'field'
                        assert x     == 'field'
                        assert y     == 'field'
                        assert z     == 'field';
                        {->
                            assert v == 'field'
                            assert w == 'field'
                            assert x == 'entry'
                            assert y == 'entry'
                            assert z == 'entry'
                        }()
                    }
                }
                new M().test()
            """
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode getV() { 'getter' }
                    public        $mode getW() { 'getter' }
                    protected     $mode getX() { 'getter' }
                    @PackageScope $mode getY() { 'getter' }
                    private       $mode getZ() { 'getter' }
                    void test() {
                        assert v     == 'getter'
                        assert w     == 'getter'
                        assert x     == 'entry'
                        assert y     == 'entry'
                        assert z     == 'entry';
                        {->
                            assert v == 'getter'
                            assert w == 'getter'
                            assert x == 'entry'
                            assert y == 'entry'
                            assert z == 'entry'
                        }()
                    }
                }
                new M().test()
            """
        }
    }

    // GROOVY-11402, GROOVY-11403
    void testMapPropertyAccess11() {
        for (mode in ['','static']) {
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode v = 'field'
                    public        $mode w = 'field'
                    protected     $mode x = 'field'
                    @PackageScope $mode y = 'field'
                    private       $mode z = 'field'
                    void test() {
                        assert this.m    === this.@m
                        assert this.v     == 'field'
                        assert this.w     == 'field'
                        assert this.x     == 'field'
                        assert this.y     == 'field'
                        assert this.z     == 'field';
                        {->
                            assert this.v == 'field'
                            assert this.w == 'field'
                            assert this.x == 'entry'
                            assert this.y == 'entry'
                            assert this.z == 'entry'
                        }()
                    }
                }
                new M().test()
            """
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode getV() { 'getter' }
                    public        $mode getW() { 'getter' }
                    protected     $mode getX() { 'getter' }
                    @PackageScope $mode getY() { 'getter' }
                    private       $mode getZ() { 'getter' }
                    void test() {
                        assert this.v     == 'getter'
                        assert this.w     == 'getter'
                        assert this.x     == 'entry'
                        assert this.y     == 'entry'
                        assert this.z     == 'entry';
                        {->
                            assert this.v == 'getter'
                            assert this.w == 'getter'
                            assert this.x == 'entry'
                            assert this.y == 'entry'
                            assert this.z == 'entry'
                        }()
                    }
                }
                new M().test()
            """
        }
    }

    // GROOVY-11403
    void testMapPropertyAccess12() {
        for (mode in ['','static']) {
            assertScript """
                class M {
                    @Delegate final Map m = [:].withDefault{ 'entry' }
                    def           $mode v = 'field'
                    public        $mode w = 'field'
                    protected     $mode x = 'field'
                    @PackageScope $mode y = 'field'
                    private       $mode z = 'field'
                    void test1() {
                        assert this.m    === this.@m
                        assert this.v     == 'field'
                        assert this.w     == 'field'
                        assert this.x     == 'field'
                        assert this.y     == 'field'
                        assert this.z     == 'field';
                        {->
                            assert this.v == 'field'
                            assert this.w == 'field'
                            assert this.x == 'entry'
                            assert this.y == 'entry'
                            assert this.z == 'entry'
                        }()
                    }
                }
                class MM extends M {
                    void test2() {
                        assert this.v     == 'field'
                        assert this.w     == 'field'
                        assert this.x     == 'entry'
                        assert this.y     == 'entry'
                        assert this.z     == 'entry';
                        {->
                            assert this.v == 'field'
                            assert this.w == 'field'
                            assert this.x == 'entry'
                            assert this.y == 'entry'
                            assert this.z == 'entry'
                        }()
                    }
                }
                new MM().with { test1(); test2() }
            """
        }
    }

    // GROOVY-11403
    void testMapPropertyAccess13() {
        for (mode in ['','static']) {
            assertScript """
                class M {
                    @Delegate private final Map<String,String> m = [:]
                    def           $mode v
                    public        $mode w
                    protected     $mode x
                    @PackageScope $mode y
                    private       $mode z
                    void test1() {
                        this.v     = 'value'
                        this.w     = 'value'
                        this.x     = 'value'
                        this.y     = 'value'
                        this.z     = 'value'
                    }
                    void test2() {
                        {->
                            this.v = 'value'
                            this.w = 'value'
                            this.x = 'value'
                            this.y = 'value'
                            this.z = 'value'
                        }()
                    }
                }
                def map = new M()
                map.test1()
                assert map.isEmpty()
                map.test2()
                assert map.keySet().toSorted() == ['x','y','z']

                class MM extends M {
                    void test3() {
                        this.v     = 'value'
                        this.w     = 'value'
                        this.x     = 'value'
                        this.y     = 'value'
                        this.z     = 'value'
                    }
                    void test4() {
                        {->
                            this.v = 'value'
                            this.w = 'value'
                            this.x = 'value'
                            this.y = 'value'
                            this.z = 'value'
                        }()
                    }
                }
                def sub = new MM()
                sub.test1()
                assert sub.isEmpty()
                sub.test2()
                assert sub.keySet().toSorted() == ['x','y','z']
                sub.clear()
                sub.test3()
                assert sub.keySet().toSorted() == ['x','y','z']
                sub.clear()
                sub.test4()
                assert sub.keySet().toSorted() == ['x','y','z']
            """
            // unlike fields, only public setter before map put
            assertScript """
                class M {
                    @Delegate private final Map<String,String> m = [:]
                    def           $mode void setV(value) { print 'v' }
                    public        $mode void setW(value) { print 'w' }
                    protected     $mode void setX(value) { print 'x' }
                    @PackageScope $mode void setY(value) { print 'y' }
                    private       $mode void setZ(value) { print 'z' }
                    void test1() {
                        this.v     = 'value'
                        this.w     = 'value'
                        this.x     = 'value'
                        this.y     = 'value'
                        this.z     = 'value'
                    }
                    void test2() {
                        {->
                            this.v = 'value'
                            this.w = 'value'
                            this.x = 'value'
                            this.y = 'value'
                            this.z = 'value'
                        }()
                    }
                }
                def map = new M()
                map.test1()
                assert map.keySet().toSorted() == ['x','y','z']
                map.clear()
                map.test2()
                assert map.keySet().toSorted() == ['x','y','z']

                class MM extends M {
                    void test3() {
                        this.v     = 'value'
                        this.w     = 'value'
                        this.x     = 'value'
                        this.y     = 'value'
                        this.z     = 'value'
                    }
                    void test4() {
                        {->
                            this.v = 'value'
                            this.w = 'value'
                            this.x = 'value'
                            this.y = 'value'
                            this.z = 'value'
                        }()
                    }
                }
                def sub = new MM()
                sub.test1()
                assert sub.keySet().toSorted() == ['x','y','z']
                sub.clear()
                sub.test2()
                assert sub.keySet().toSorted() == ['x','y','z']
                sub.clear()
                sub.test3()
                assert sub.keySet().toSorted() == ['x','y','z']
                sub.clear()
                sub.test4()
                assert sub.keySet().toSorted() == ['x','y','z']
            """
        }
    }

    // GROOVY-11368
    void testMapPropertyAccess14() {
        String type = '''
            class M implements Map<String,String> {
                @Delegate Map<String,String> impl = [:]
            }
        '''
        assertScript type + '''
            def map = new M()
            assert map.entry == null
            assert map.empty == null
            assert map.class == null
            assert map.metaClass != null
        '''
        assertScript type + '''
            def test(M map) { // no diff
                assert map.entry == null
                assert map.empty == null
                assert map.class == null
                assert map.metaClass != null
            }
            test(new M())
        '''
    }

    // GROOVY-11367
    void testMapPropertyAccess15() {
        String map = "def map = new ${MapType.name}()"
        assertScript map + '''
            map.foo = 11 // public setter
            assert map.foo == 11
            assert map.getFoo() == 11
            assert map.get('foo') == null
        '''
        assertScript map + '''
            map.bar = 22 // (not) protected setter
            assert map.bar == 22
            assert map.@bar == 2
        '''
        assertScript map + '''
            map.baz = 33 // (not) package-private setter
            assert map.baz == 33
            assert map.@baz == 3
        '''
    }

    // GROOVY-11387
    void testMapPropertyAccess16() {
        assertScript '''
            def map = new HashMap<String,String>()
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
            })
            def xxx = map.table
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
            })
            def yyy = map.with{
                @ASTTest(phase=INSTRUCTION_SELECTION, value={
                    assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
                })
                def zzz = table
            }
            assert xxx == null
            assert yyy == null
        '''
    }

    // GROOVY-11387
    void testMapPropertyAccess17() {
        assertScript '''
            class HttpHeaders extends HashMap<String,List<String>> {
                protected static final String ACCEPT = 'Accept'
            }
            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
            })
            def accept = HttpHeaders.ACCEPT
            assert accept == 'Accept'
        '''
    }

    // GROOVY-11401
    void testMapPropertyAccess18() {
        assertScript '''
            class C {
                private Object obj = 'field'
                def m() {
                    Map<String,String> map = [:].withDefault{'entry'}
                    map.with {
                        @ASTTest(phase=INSTRUCTION_SELECTION, value={
                            assert node.getNodeMetaData(INFERRED_TYPE) == STRING_TYPE
                        })
                        def xxx = obj
                    }
                }
            }
            assert new C().m() == 'entry'
        '''
    }

    void testTypeCheckerDoesNotThinkPropertyIsReadOnly() {
        assertScript '''
            // a base class defining a read-only property
            class C {
                private String foo = 'foo'
                String getFoo() { foo }
                String getFooFromC() { foo }
            }

            // a subclass defining its own field
            class D extends C {
                D(String msg) {
                    this.foo = msg
                }
                private String foo
                public  String getFoo() { this.foo }
            }
            def d = new D('bar')
            assert d.foo == 'bar'
            assert d.fooFromC == 'foo'
        '''
    }

    // GROOVY-5779
    void testShouldNotUseNonStaticProperty() {
        assertScript '''import java.awt.Color
            Color c = Color.red // should not be interpreted as Color.getRed()
        '''
    }

    // GROOVY-5725
    void testAccessFieldDefinedInInterface() {
        assertScript '''
            class C implements groovy.transform.stc.FieldsAndPropertiesSTCTest.InterfaceWithField {
                void test() {
                    assert boo == "I don't fancy fields in interfaces"
                }
            }
            new C().test()
        '''
    }

    void testOuterPropertyAccess1() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() {
                        p
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.m()
            assert x == 1
        '''
    }

    void testOuterPropertyAccess2() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() {
                        getP()
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.m()
            assert x == 1
        '''
    }

    // GROOVY-10414
    void testOuterPropertyAccess3() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() {
                        setP(2)
                        getP()
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.m()
            assert x == 2
        '''
    }

    // GROOVY-8050
    void testOuterPropertyAccess4() {
        shouldFailWithMessages '''
            class Outer {
                class Inner {
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.p
        ''',
        'No such property: p for class: Outer$Inner'
    }

    // GROOVY-8050
    void testOuterPropertyAccess5() {
        shouldFailWithMessages '''
            class Outer {
                class Inner {
                }
                def p = 1
            }
            def i = new Outer.Inner(new Outer())
            def x = i.getP()
        ''',
        'Cannot find matching method Outer$Inner#getP()'
    }

    // GROOVY-9598
    void testOuterPropertyAccess6() {
        shouldFailWithMessages '''
            class Outer {
                static class Inner {
                    def m() {
                        p
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner()
            def x = i.m()
        ''',
        'The variable [p] is undeclared.'
    }

    void testOuterPropertyAccess7() {
        shouldFailWithMessages '''
            class Outer {
                static class Inner {
                    def m() {
                        this.p
                    }
                }
                def p = 1
            }
            def i = new Outer.Inner()
            def x = i.m()
        ''',
        'No such property: p for class: Outer$Inner'
    }

    // GROOVY-7024
    void testOuterPropertyAccess8() {
        assertScript '''
            class Outer {
                static Map props = [bar: 10, baz: 20]
                enum Inner {
                    FOO('foo');
                    Inner(String name) {
                        props[name] = 30
                    }
                }
            }
            def foo = Outer.Inner.FOO
            assert Outer.props == [bar: 10, baz: 20, foo: 30]
        '''
    }

    void testOuterPropertyAccess9() {
        assertScript '''
            class Outer {
                static final int ONE = 1
                enum Inner {
                    CONST(1 + ONE)
                    final int value
                    Inner(int value) {
                        this.value = value
                    }
                }
            }
            assert Outer.Inner.CONST.value == 2
        '''
    }

    void testOuterPropertyAccess10() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() { p }
                }
                String p = 'field'
                String getP() { 'property' }
            }
            String which = new Outer.Inner(new Outer()).m()
            assert which == 'property'
        '''
    }

    // GROOVY-11199
    void testOuterPropertyAccess11() {
        assertScript '''
            class Outer {
                class Inner {
                    def m() { p = 'method' }
                }
                String p = 'field'
                String getP() { 'property' }
            }
            String which = new Outer.Inner(new Outer()).m()
            assert which == 'method'
        '''
    }

    // GROOVY-10981, GROOVY-10985
    void testOuterPropertyAccess12() {
        for (propertySource in [
                'def get(String name){if(name=="VALUE")return 2}',
                'def getProperty(String name){if(name=="VALUE")return 2}',
                '@Delegate private Map<String,Object> map = [VALUE:2]']) {
            assertScript """
                class Outer {
                    static private int VALUE = 1
                    static class Inner {
                        $propertySource
                        def test(int i) {
                            return VALUE
                        }
                    }
                }
                Object value = new Outer.Inner().test(0)
                assert value == 2
            """
        }
    }

    // GROOVY-11029
    void testSuperPropertyAccess1() {
        assertScript '''
            class C {
                Object thing
            }
            class D extends C {
                @Override
                Object getThing() {
                    super.thing
                }
                @Override
                void setThing(object) {
                    super.thing = object
                }
            }
            def d = new D()
            d.thing = 'value'
            assert d.thing == 'value'
        '''
    }

    void testSuperPropertyAccess2() {
        assertScript '''
            abstract class A implements java.util.function.IntSupplier {
                final int prop = 1
            }
            class C {
                final int prop = 2
                A m() {
                    new A() {
                        int getAsInt() {
                            prop
                        }
                    }
                }
            }
            Number which = new C().m().getAsInt()
            assert which == 1 // super before outer
        '''
    }

    // GROOVY-9562
    void testSuperPropertyAccess3() {
        assertScript '''
            abstract class A {
                final int prop = 1
            }
            abstract class B {
                final int prop = 2
                abstract int baz()
            }
            class C extends A {
                B bar() {
                    new B() {
                        int baz() {
                            prop
                        }
                    }
                }
            }
            Number which = new C().bar().baz()
            assert which == 2 // super before outer
        '''
    }

    // GROOVY-10981
    void testSuperPropertyAccess4() {
        for (mode in ['', 'public', 'private', 'protected', '@PackageScope']) {
            assertScript """
                abstract class A {
                    $mode Object p = 'field'
                    CharSequence getP() { 'property' }
                }
                class C extends A {
                    def m() {
                        final int len = p.length()
                        if (p instanceof String) {
                            p.toLowerCase()
                            p.toUpperCase()
                        }
                    }
                }
                String which = new C().m()
                assert which == 'PROPERTY'
            """
        }
    }

    // GROOVY-11005
    void testSuperPropertyAccess5() {
        File parentDir = File.createTempDir()
        config.with {
            targetDirectory = File.createTempDir()
            jointCompilationOptions = [memStub: true]
        }
        try {
            def a = new File(parentDir, 'Pogo.groovy')
            a.write '''
                class Pogo {
                    String value
                    String getValue() { value }
                }
            '''
            def b = new File(parentDir, 'Test.groovy')
            b.write '''
                class Test extends Pogo {
                    void test() {
                        value = 'string'
                    }
                }
            '''

            def loader = new GroovyClassLoader(this.class.classLoader)
            def cu = new JavaAwareCompilationUnit(config, loader)
            cu.addSources(a, b)
            cu.compile()

            loader.loadClass('Test').newInstance().test()
        } finally {
            parentDir.deleteDir()
            config.targetDirectory.deleteDir()
        }
    }

    void testPrivateFieldAccessInClosure1() {
        assertScript '''
            class C {
                private int x
                void test() {
                    {-> x = 666 }()
                    assert x == 666
                }
            }
            new C().test()
        '''
    }

    void testPrivateFieldAccessInClosure2() {
        assertScript '''
            class C {
                private static int x
                static test() {
                    {-> x = 666 }()
                    assert x == 666
                }
            }
            C.test()
        '''
    }

    // GROOVY-9683
    void testPrivateFieldAccessInClosure3() {
        assertScript '''
            class C {
                private static X = 'xxx'
                void test() {
                    {->
                        assert X == 'xxx'
                    }()
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
                    Map m = [:];
                    {->
                        assert X == 'xxx'
                        m[X] = 123
                    }()
                    assert m == [xxx:123]
                }
            }
            new C().test()

            class D extends C {
            }
            new D().test()
        '''
    }

    // GROOVY-9768
    void testPrivateFieldAccessInClosure5() {
        assertScript '''import groovy.transform.stc.FirstParam
            class C {
                private <T> void foo(
                        @DelegatesTo.Target T target,
                        @DelegatesTo(strategy=Closure.OWNER_FIRST)
                        @ClosureParams(FirstParam.class) Closure action) {
                    action.setResolveStrategy(Closure.OWNER_FIRST)
                    action.setDelegate(target)
                    action.call(target)
                }
                void test() {
                    def map = [V: 'val']
                    foo(map) {
                        assert V == 'val'
                        assert X == 'xxx'
                        assert it.X == null
                    }
                }
                private static final X = 'xxx'
            }
            new C().test()
        '''
    }

    // GROOVY-11144
    void testPrivateFieldAccessInClosure6() {
        assertScript '''
            abstract class A {
                private static final String NAME = 'name'
                final String name
                A() {
                    name = { -> NAME }()
                }
            }
            class C extends A {
            }
            assert new C().name == 'name'
        '''
    }

    // GROOVY-11198
    void testPrivateFieldAccessInEnumInit() {
        for (mode in ['public', 'private', 'protected', '@PackageScope']) {
            assertScript """
                class C {
                    $mode static int ONE = 1
                    enum E {
                        FOO(1 + ONE)
                        final number
                        E(int number) {
                            this.number = number
                        }
                    }
                }
                assert C.E.FOO.number == 2
            """
        }
    }

    // GROOVY-11358
    void testPrivateFieldAccessOfAbstract() {
        shouldFailWithMessages '''
            abstract class A {
                private int f
            }
            void test(A a) {
                int i = a.@f // MissingFieldException
                int j = a.f  // MissingPropertyException
            }
            test(new A() {})
        ''',
        'Cannot access field: f of class: A', 'No such property: f for class: A'

        shouldFailWithMessages '''
            abstract class A {
                private int f
            }
            void test(A a) {
                a.@f = 1 // MissingFieldException
                a.f  = 2 // MissingPropertyException
            }
            test(new A() {})
        ''',
        'Cannot access field: f of class: A', 'No such property: f for class: A'
    }

    // GROOVY-5737
    void testGeneratedFieldAccessInClosure() {
        assertScript '''
            @groovy.util.logging.Log
            class GreetingActor {
                def receive = {
                    log.info "test"
                }
            }
            new GreetingActor()
        '''
    }

    // GROOVY-6277
    void testPublicFieldVersusPrivateGetter() {
        assertScript '''
            class C {
                private String getWho() { 'C' }
            }
            class D extends C {
                public String who = 'D'
            }
            String result = new D().who
            assert result == 'D'
        '''
    }

    // GROOVY-9973
    void testPrivateFieldVersusPublicGetter() {
        assertScript '''
            class C {
                private int f
                int getP() { f }
                Integer m() { 123456 - p }
                Integer m(int i) { i - p }
            }
            def c = new C()
            assert c.m() == 123456 // BUG! exception in phase 'class generation' ...
            assert c.m(123) == 123 // ClassCastException: class org.codehaus.groovy.ast.Parameter cannot be cast to ...
        '''
    }

    // GROOVY-11381
    void testPrivateFieldVersusPublicGetterOnInterface() {
        assertScript '''
            interface I {
                default int getP() { 42 }
            }
            class C implements I {
                private String p
            }
            def c = new C()
            assert c.p == 42
            Number c_p = c.p // Cannot assign value of type String to variable of type Number
        '''
    }

    void testPrivateFieldVersusPublicSetterOnInterface() {
        assertScript '''
            interface I {
                Object[] set = new Object[1]
                default void setP(int p) { set[0] = true }
            }
            class C implements I {
                private String p
            }
            class D extends C {
            }
            def d = new D()
            d.p = 42
            assert I.set[0]
        '''
        shouldFailWithMessages '''
            interface I {
                default void setP(int p) { }
            }
            class C implements I {
                private String p
            }
            def c = new C()
            c.p = 'xxx'
        ''',
        'Cannot assign value of type java.lang.String to variable of type int'
    }

    void testProtectedAccessorFromSamePackage() {
        assertScript '''
            class C {
                protected String getWho() { 'C' }
            }
            class D {
                def m(C c) {
                    def x = c.who
                    x.toLowerCase()
                }
            }
            String result = new D().m(new C())
            assert result == 'c'
        '''
    }

    // GROOVY-6610
    void testPrivateStaticFieldAccessBeforeThis() {
        assertScript '''
            class Outer {
                static class Inner {
                    public final String value
                    Inner(String string) {
                        value = string
                    }
                    Inner() {
                        this(VALUE.toString())
                    }
                }
                private static Integer VALUE = 42
                static main(args) {
                    assert new Inner().value == '42'
                }
            }
        '''
    }

    // GROOVY-7890
    void testNonStaticPropertyAndStaticMethodClosure() {
        shouldFailWithMessages '''
            class C {
                List<String> replace
                static String m(String s) {
                    s.collectReplacements {
                        (it in replace) ? 'o' : null
                    }
                }
            }
        ''',
        'The variable [replace] is undeclared'

        assertScript '''
            class C {
              List<String> replace
              String m(String s) {
                s.collectReplacements {
                  (it in replace) ? 'o' : null
                }
              }
            }
            String result = new C(replace:['a','b','c']).m('foobar')
            assert result == 'foooor'
        '''
    }

    // GROOVY-5872
    void testAssignNullToFieldWithGenericsShouldNotThrowError() {
        assertScript '''
            class C {
                List<String> list = null // should not throw an error
            }
            new C()
        '''
    }

    void testSetterInWith() {
        assertScript '''
            class C {
                private int y
                void setFoo(int x) { y = x }
                int value() { y }
            }
            def c = new C()
            c.with {
                setFoo(5)
            }
            assert c.value() == 5
        '''
    }

    void testSetterInWithUsingPropertyNotation() {
        assertScript '''
            class C {
                private int y
                void setFoo(int x) { y = x }
                int value() { y }
            }
            def c = new C()
            c.with {
                foo = 5
            }
            assert c.value() == 5
        '''
    }

    void testSetterInWithUsingPropertyNotationAndClosureSharedVariable() {
        assertScript '''
            class C {
                private int y
                void setFoo(int x) { y = x }
                int value() { y }
            }
            def c = new C()
            def csv = 0
            c.with {
                foo = 5
                csv = 10
            }
            assert c.value() == 5
            assert csv == 10
        '''
    }

    // GROOVY-9653
    void testSetterInWithUsingPropertyNotation_DelegateAndOwnerHaveSetter() {
        assertScript '''
            class C {
                final result = new D().with {
                    something = 'value' // ClassCastException: D cannot be cast to C
                    return object
                }
                void setSomething(value) { }
            }

            class D {
                void setSomething(value) { }
                Object getObject() { 'works' }
            }

            assert new C().result == 'works'
        '''
    }

    // GROOVY-6230
    void testAttributeWithGetterOfDifferentType() {
        assertScript '''import java.awt.Dimension
            def d = new Dimension(800,600)

            @ASTTest(phase=INSTRUCTION_SELECTION,value={
                def rit = node.rightExpression.getNodeMetaData(INFERRED_TYPE)
                assert rit == int_TYPE
            })
            int width = d.@width
            assert width == 800
            assert (d.@width).getClass() == Integer
        '''
    }

    // GROOVY-6489
    void testShouldNotThrowUnmatchedGenericsError() {
        assertScript '''
            public class Foo {
                private List<String> names;
                public List<String> getNames() {
                    return names;
                }
                public void setNames(List<String> names) {
                    this.names = names;
                }
            }
            class FooWorker {
                public void doSomething() {
                    new Foo().with {
                        names = new ArrayList()
                    }
                }
            }
            new FooWorker().doSomething()
        '''
    }

    void testShouldFailWithIncompatibleGenericTypes() {
        shouldFailWithMessages '''
            public class Foo {
                private List<String> names;

                public List<String> getNames() {
                    return names;
                }

                public void setNames(List<String> names) {
                    this.names = names;
                }
            }

            class FooWorker {
                public void doSomething() {
                    new Foo().with {
                        names = new ArrayList<Integer>()
                    }
                }
            }

            new FooWorker().doSomething()
        ''',
        'Incompatible generic argument types. Cannot assign java.util.ArrayList<java.lang.Integer> to: java.util.List<java.lang.String>'
    }

    void testAICAsStaticProperty() {
        assertScript '''
            class Foo {
                static x = new Object() {}
            }
            assert Foo.x instanceof Object
        '''
    }

    void testPropertyWithMultipleSetters() {
        assertScript '''
            import org.codehaus.groovy.ast.expr.*
            import org.codehaus.groovy.ast.stmt.*

            class C {
                private field
                void setX(Integer a) {field=a}
                void setX(String b) {field=b}
                def getX(){field}
            }

            @ASTTest(phase=INSTRUCTION_SELECTION, value={
                lookup('test1').each { stmt ->
                    def exp = stmt.expression
                    assert exp instanceof BinaryExpression
                    def left = exp.leftExpression
                    def md = left.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert md
                    assert md.name == 'setX'
                    assert md.parameters[0].originType == Integer_TYPE
                }
                lookup('test2').each { stmt ->
                    def exp = stmt.expression
                    assert exp instanceof BinaryExpression
                    def left = exp.leftExpression
                    def md = left.getNodeMetaData(DIRECT_METHOD_CALL_TARGET)
                    assert md
                    assert md.name == 'setX'
                    assert md.parameters[0].originType == STRING_TYPE
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

    // GROOVY-9893
    void testPropertyWithMultipleSetters2() {
        assertScript '''
            abstract class A { String which
                void setX(String s) { which = 'String' }
            }
            class C extends A {
                void setX(boolean b) { which = 'boolean' }
            }

            def c = new C()
            c.x = 'value'
            assert c.which == 'String'
        '''
    }

    // GROOVY-9893
    void testPropertyWithMultipleSetters3() {
        assertScript '''
            interface I {
                void setX(String s)
            }
            abstract class A implements I { String which
                void setX(boolean b) { which = 'boolean' }
            }
            A a = new A() {
                void setX(String s) { which = 'String' }
            }
            a.x = 'value'
            assert a.which == 'String'
        '''
    }

    // GROOVY-9893
    void testPropertyWithMultipleSetters4() {
        assertScript '''
            trait T { String which
                void setX(String s) { which = 'String' }
            }
            class C implements T {
                void setX(boolean b) { which = 'boolean' }
            }

            def c = new C()
            c.x = 'value'
            assert c.which == 'String'
        '''
    }

    void testPropertyAssignmentAsExpression() {
        assertScript '''
            class C {
                int x = 2
            }
            def c = new C()
            def x = c.x = 3
            assert x == 3
        '''
    }

    void testPropertyAssignmentInSubClassAndMultiSetter() {
        10.times {
            assertScript '''
                class C {
                    int which
                    C() {
                        contentView = 42L
                        assert which == 2
                    }
                    void setContentView(Date value) { which = 1 }
                    void setContentView(Long value) { which = 2 }
                }
                class D extends C {
                    void m() {
                        contentView = 42L
                        assert which == 2
                        contentView = new Date()
                        assert which == 1
                    }
                }
                new D().m()
            '''
        }
    }

    void testPropertyAssignmentInSubClassAndMultiSetterThroughDelegation() {
        10.times {
            assertScript '''
                class C {
                    int which
                    void setContentView(Date value) { which = 1 }
                    void setContentView(Long value) { which = 2 }
                }
                class D extends C {
                }
                new D().with {
                    contentView = 42L
                    assert which == 2
                    contentView = new Date()
                    assert which == 1
                }
            '''
        }
    }

    void testShouldAcceptPropertyAssignmentEvenIfSetterOnlyBecauseOfSpecialType() {
        assertScript '''
            class BooleanSetterOnly {
                void setFlag(boolean b) {}
            }
            def b = new BooleanSetterOnly()
            b.flag = 'foo'
        '''
        assertScript '''
            class StringSetterOnly {
                void setFlag(String b) {}
            }
            def b = new StringSetterOnly()
            b.flag = false
        '''
        assertScript '''
            class ClassSetterOnly {
                void setFlag(Class b) {}
            }
            def b = new ClassSetterOnly()
            b.flag = 'java.lang.String'
        '''
    }

    // GROOVY-6590
    void testShouldFindStaticPropertyOnPrimitiveType() {
        assertScript '''
            int i=1
            i.MAX_VALUE
        '''
        assertScript '''
            def i="d"
            i=1
            i.MAX_VALUE
        '''
    }

    // GROOVY-9855
    void testShouldInlineStringConcatInTypeAnnotation() {
        assertScript '''
            @SuppressWarnings(C.PREFIX + 'checked') // not 'un'.plus('checked')
            class C {
                public static final String PREFIX = 'un'
            }
            new C()
        '''
    }

    void testImplicitPropertyOfDelegateShouldNotPreferField() {
        assertScript '''
            Calendar.instance.with {
                Date d1 = time // Date getTime() vs. long time
            }
        '''
    }

    void testPropertyStyleSetterArgShouldBeCheckedAgainstParamType() {
        shouldFailWithMessages '''
            class Foo {
                Bar bar;

                void setBar(int x) {
                    this.bar = new Bar(x: x)
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo()
            foo.bar = new Bar()
        ''',
        'Cannot assign value of type Bar to variable of type int'

        assertScript '''
            class Foo {
                Bar bar;

                void setBar(int x) {
                    this.bar = new Bar(x: x)
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo()
            foo.bar = 1
            assert foo.bar.x == 1
        '''
    }

    void testPropertyStyleGetterUsageShouldBeCheckedAgainstReturnType() {
        shouldFailWithMessages '''
            class Foo {
                Bar bar;

                int getBar() {
                    bar.x
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo(bar: new Bar(x: 1))
            Bar bar = foo.bar
        ''',
        'Cannot assign value of type int to variable of type Bar'

        assertScript '''
            class Foo {
                Bar bar;

                int getBar() {
                    bar.x
                }
            }

            class Bar {
                int x
            }

            Foo foo = new Foo(bar: new Bar(x: 1))
            int x = foo.bar
            assert x == 1
        '''
    }

    //--------------------------------------------------------------------------

    static interface InterfaceWithField {
        String boo = "I don't fancy fields in interfaces"
    }

    static class MapType extends HashMap<String,Number> {
        def foo = 1
        protected bar = 2
        @PackageScope baz = 3
        protected void setBar(bar) {}
        @PackageScope void setBaz(baz) {}
    }

    static class BaseClass {
        int x
    }

    static class BaseClass2 extends BaseClass {
    }

    @PackageScope static class PackagePrivate {
        public static Number getAnswer() { 42 }
        public static final String CONST = 'XX'
        public static String VALUE
    }

    static class Public extends PackagePrivate {
    }
}
