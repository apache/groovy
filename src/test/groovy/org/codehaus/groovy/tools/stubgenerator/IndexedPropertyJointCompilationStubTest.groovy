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
package org.codehaus.groovy.tools.stubgenerator

/**
 * Captures the joint-compilation surface for {@code @IndexedProperty}.
 *
 * <p>Field-level annotation; the stubber emits {@code getXxx(int)} and
 * {@code setXxx(int, T)} placeholders for array and {@code List<T>}
 * fields, with the component type derived from the field type at
 * CONVERSION (no later phase information needed).
 *
 * <p>The full transform replaces the placeholder bodies via the
 * metadata-key handoff. Setter emission is suppressed by the stubber
 * when the enclosing class is annotated with
 * {@code groovy.transform.ImmutableBase} or {@code KnownImmutable}; this
 * approximates the full transform's per-field {@code IMMUTABLE_BREADCRUMB}
 * check (which is not yet set at CONVERSION).
 */
final class IndexedPropertyJointCompilationStubTest extends StringSourcesStubTestCase {

    @Override
    Map<String, String> provideSources() {
        [
            'foo/Box.groovy': '''
                package foo

                class Box {
                    @groovy.transform.IndexedProperty
                    String[] tags = new String[3]

                    @groovy.transform.IndexedProperty
                    List<Integer> scores = [0, 0, 0]
                }

                @groovy.transform.AnnotationCollector([
                    groovy.transform.ImmutableBase,
                    groovy.transform.TupleConstructor,
                    groovy.transform.PropertyOptions
                ])
                @interface MyImmutable {}

                @MyImmutable(defaults = false, propertyHandler = groovy.transform.options.ImmutablePropertyHandler)
                class ImmutableBox {
                    @groovy.transform.IndexedProperty
                    List<String> items
                }

                @MyImmutable(defaults = false, propertyHandler = groovy.transform.options.ImmutablePropertyHandler)
                class ImmutableArrayBox {
                    @groovy.transform.IndexedProperty
                    String[] tags
                }

                @groovy.transform.RecordType
                class RecordBox {
                    @groovy.transform.IndexedProperty
                    List<String> items
                }

                @groovy.transform.RecordBase
                @groovy.transform.TupleConstructor
                class BaseBox {
                    @groovy.transform.IndexedProperty
                    List<Integer> nums
                }
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static String firstTag(Box b)        { return b.getTags(0); }
                    public static int firstScore(Box b)         { return b.getScores(0); }
                    public static void setFirstTag(Box b, String s)   { b.setTags(0, s); }
                    public static void setFirstScore(Box b, int n)    { b.setScores(0, n); }
                    public static String secondRecordItem(RecordBox b) { return b.getItems(1); }
                    public static int secondBaseNum(BaseBox b) { return b.getNums(1); }
                    public static String firstImmutableTag(ImmutableArrayBox b) { return b.getTags(0); }
                }
            '''
        ]
    }

    @Override
    void verifyStubs() {
        // Stub view: indexed accessors visible.
        String boxStub = stubJavaSourceFor('foo.Box')
        assert boxStub =~ /java\.lang\.String\s+getTags\(\s*int\s+\w+\s*\)/
        assert boxStub =~ /void\s+setTags\(\s*int\s+\w+\s*,\s*java\.lang\.String\s+\w+\s*\)/
        assert boxStub =~ /java\.lang\.Integer\s+getScores\(\s*int\s+\w+\s*\)/
        assert boxStub =~ /void\s+setScores\(\s*int\s+\w+\s*,\s*java\.lang\.Integer\s+\w+\s*\)/

        // Runtime view: real bodies present, indexed access works.
        Class boxClass = loader.loadClass('foo.Box')
        def box = boxClass.newInstance()

        // Use the indexed accessors via Groovy (which dispatches to the
        // generated methods); confirms the metadata-key handoff replaced
        // the stubber's placeholder body with the real body.
        boxClass.getMethod('setTags', int.class, String).invoke(box, 0, 'alpha')
        assert boxClass.getMethod('getTags', int.class).invoke(box, 0) == 'alpha'

        boxClass.getMethod('setScores', int.class, Integer).invoke(box, 0, 42)
        assert boxClass.getMethod('getScores', int.class).invoke(box, 0) == 42

        Class immutableBoxClass = loader.loadClass('foo.ImmutableBox')
        assert immutableBoxClass.declaredMethods.every { it.name != 'setItems' }
        def ibox = immutableBoxClass.getConstructor(List).newInstance(['x', 'y'])
        assert immutableBoxClass.getMethod('getItems', int.class).invoke(ibox, 1) == 'y'

        String arrayStub = stubJavaSourceFor('foo.ImmutableArrayBox')
        assert arrayStub =~ /java\.lang\.String\s+getTags\(\s*int\s+\w+\s*\)/
        Class arrayBoxClass = loader.loadClass('foo.ImmutableArrayBox')
        assert arrayBoxClass.declaredMethods.every { it.name != 'setTags' }
        def abox = arrayBoxClass.getConstructor(String[]).newInstance([new String[] {'p', 'q'}] as Object[])
        assert arrayBoxClass.getMethod('getTags', int.class).invoke(abox, 1) == 'q'

        // Records are immutable, so the stub exposes the indexed getter only.
        // TODO: also assert the RecordBox and BaseBox class files have no indexed
        // setter, as ImmutableBox does above. GROOVY-12730 owns that record behavior.
        String recordStub = stubJavaSourceFor('foo.RecordBox')
        assert recordStub =~ /java\.lang\.String\s+getItems\(\s*int\s+\w+\s*\)/
        assert !(recordStub =~ /void\s+setItems\(\s*int\s+\w+\s*,\s*java\.lang\.String\s+\w+\s*\)/)
        Class recordClass = loader.loadClass('foo.RecordBox')
        def record = recordClass.getConstructor(List).newInstance(['a', 'b'])
        assert recordClass.getMethod('getItems', int.class).invoke(record, 1) == 'b'

        String baseStub = stubJavaSourceFor('foo.BaseBox')
        assert baseStub =~ /java\.lang\.Integer\s+getNums\(\s*int\s+\w+\s*\)/
        assert !(baseStub =~ /void\s+setNums\(\s*int\s+\w+\s*,\s*java\.lang\.Integer\s+\w+\s*\)/)
        Class baseClass = loader.loadClass('foo.BaseBox')
        def base = baseClass.getConstructor(List).newInstance([1, 2])
        assert baseClass.getMethod('getNums', int.class).invoke(base, 0) == 1
    }
}
