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

import groovy.test.GroovyShellTestCase

/**
 * Tests for the {@code @IndexedProperty} AST transformation.
 */
class IndexedPropertyTransformTest extends GroovyShellTestCase {

    void testStandardCase() {
        assertScript '''
            import groovy.transform.IndexedProperty
            class Demo {
                @IndexedProperty List<Integer> foo
                @IndexedProperty String[] bar
                @IndexedProperty List baz
            }

            def d = new Demo()
            d.foo = [11, 22, 33]
            d.setFoo(0, 100)
            d.setFoo(1, 200)
            assert d.foo == [100, 200, 33]
            assert d.getFoo(1) == 200
            d.bar = ['cat']
            d.setBar(0, 'dog')
            assert d.getBar(0) == 'dog'
            d.baz = ['cat']
            d.setBaz(0, 'dog')
            assert d.getBaz(0) == 'dog'
        '''
    }

    void testWithCompileStatic() {
        assertScript '''
            import groovy.transform.IndexedProperty
            @groovy.transform.CompileStatic
            class Demo {
                @IndexedProperty List<Integer> foo
                @IndexedProperty String[] bar
                @IndexedProperty List baz
            }

            def d = new Demo()
            d.foo = [11, 22, 33]
            d.setFoo(0, 100)
            d.setFoo(1, 200)
            assert d.foo == [100, 200, 33]
            assert d.getFoo(1) == 200
            d.bar = ['cat']
            d.setBar(0, 'dog')
            assert d.getBar(0) == 'dog'
            d.baz = ['cat']
            d.setBaz(0, 'dog')
            assert d.getBaz(0) == 'dog'
        '''
    }

    void testReadOnlyProperty() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            class Demo1 {
                @IndexedProperty List<Integer> foo
            }
            @Immutable
            class Demo2 {
                @IndexedProperty List<Integer> foo
            }

            assert Demo1.getMethod('getFoo', int)
            assert Demo1.getMethod('setFoo', int, Integer)
            assert Demo2.getMethod('getFoo', int)
            shouldFail(NoSuchMethodException) {
                Demo2.getMethod('setFoo', int, Integer)
            }

            @Immutable
            class Demo3 {
                @IndexedProperty String[] bar
            }
            assert Demo3.getMethod('getBar', int)
            shouldFail(NoSuchMethodException) {
                Demo3.getMethod('setBar', int, String)
            }
        '''
    }

    // GROOVY-12730
    void testRecordProperty() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            record Demo1(@IndexedProperty List<Integer> foo) {}
            @RecordType
            class Demo2 {
                @IndexedProperty List<Integer> foo
            }
            @RecordBase
            @TupleConstructor
            class Demo3 {
                @IndexedProperty Integer[] foo
            }

            assert Demo1.getMethod('getFoo', int)
            shouldFail(NoSuchMethodException) {
                Demo1.getMethod('setFoo', int, Integer)
            }
            assert Demo2.getMethod('getFoo', int)
            shouldFail(NoSuchMethodException) {
                Demo2.getMethod('setFoo', int, Integer)
            }
            assert Demo3.getMethod('getFoo', int)
            shouldFail(NoSuchMethodException) {
                Demo3.getMethod('setFoo', int, Integer)
            }

            assert new Demo1([1, 2]).getFoo(1) == 2
            assert new Demo2([1, 2]).getFoo(1) == 2
            assert new Demo3([1, 2] as Integer[]).getFoo(1) == 2
        '''
    }

    // GROOVY-12726
    void testImmutableAliasDropsIndexedSetterStub() {
        assertScript '''
            import groovy.transform.*
            import static groovy.test.GroovyAssert.shouldFail

            @AnnotationCollector([ImmutableBase, TupleConstructor, PropertyOptions])
            @interface MyImmutable {}

            @MyImmutable(defaults = false, propertyHandler = groovy.transform.options.ImmutablePropertyHandler)
            class ArrayBox {
                @IndexedProperty String[] tags
            }
            @MyImmutable(defaults = false, propertyHandler = groovy.transform.options.ImmutablePropertyHandler)
            class ListBox {
                @IndexedProperty List<String> items
            }

            def tags = new ArrayBox(new String[] {'a', 'b'})
            assert tags.getTags(1) == 'b'
            shouldFail(NoSuchMethodException) { ArrayBox.getMethod('setTags', int, String) }

            def items = new ListBox(['x', 'y'])
            assert items.getItems(0) == 'x'
            shouldFail(NoSuchMethodException) { ListBox.getMethod('setItems', int, String) }
        '''
    }

    // GROOVY-12726
    void testRecordLikeClassesExposeIndexedGetters() {
        assertScript '''
            import groovy.transform.*

            @RecordType
            class RecordBox {
                @IndexedProperty List<String> items
                @IndexedProperty String[] tags
            }
            @RecordBase
            @TupleConstructor
            class BaseBox {
                @IndexedProperty List<Integer> nums
            }

            assert RecordBox.getMethod('getItems', int)
            assert RecordBox.getMethod('getTags', int)
            assert BaseBox.getMethod('getNums', int)

            def record = new RecordBox(['a', 'b'], new String[] {'x'})
            assert record.getItems(1) == 'b'
            assert record.getTags(0) == 'x'

            def base = new BaseBox([1, 2])
            assert base.getNums(0) == 1
        '''
    }

    // GROOVY-12726
    void testStaticIndexedProperty() {
        assertScript '''
            import groovy.transform.IndexedProperty
            import static java.lang.reflect.Modifier.isStatic

            class Demo {
                @IndexedProperty static List<String> items = ['a', 'b']
            }

            assert isStatic(Demo.getMethod('getItems', int).modifiers)
            assert isStatic(Demo.getMethod('setItems', int, String).modifiers)
            Demo.setItems(0, 'z')
            assert Demo.getItems(0) == 'z'
        '''
    }

    // GROOVY-12726
    void testRejectsNonPropertyAndNonIndexableField() {
        def notProperty = shouldFail {
            assertScript '''
                import groovy.transform.IndexedProperty
                class Demo {
                    public @IndexedProperty List<String> items
                }
            '''
        }
        assert notProperty.contains("doesn't appear to be a property")

        def notIndexable = shouldFail {
            assertScript '''
                import groovy.transform.IndexedProperty
                class Demo {
                    @IndexedProperty String name
                }
            '''
        }
        assert notIndexable.contains('Non-Indexable property')
        assert notIndexable.contains('String')
    }

}
