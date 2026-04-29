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
            ''',
            'foo/JavaUser.java': '''
                package foo;

                public class JavaUser {
                    public static String firstTag(Box b)        { return b.getTags(0); }
                    public static int firstScore(Box b)         { return b.getScores(0); }
                    public static void setFirstTag(Box b, String s)   { b.setTags(0, s); }
                    public static void setFirstScore(Box b, int n)    { b.setScores(0, n); }
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
    }
}
