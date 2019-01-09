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
 * Checks that default values from annotation definitions appear within stubs.
 */
class AnnotationDefaultValuesStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'Tx.groovy': '''
                import java.lang.annotation.*
                @Retention(RetentionPolicy.RUNTIME)
                @Target([ElementType.TYPE, ElementType.METHOD])
                @interface Tx {
                    String value() default 'def'
                    int i() default 3
                    boolean bool() default false
                    float f() default 3.0f
                    double d() default 3.0d
                    long l() default 3l
                    byte me() default 3
                    short s() default 3
                    char c1() default 65
                    char c2() default 'A' as char
                    char c3() default (char)'A'
                    Class clazz() default String
                    Color color() default Color.RED
                    String[] values() default ['def']
                    int[] is() default [3]
                    long[] ls() default []
                    boolean[] bs() default [true]
                    Class[] clazzes() default [String, Integer.class]
                    Color[] colors() default [Color.GREEN, Color.BLUE]
                }
            ''',

            'PersonServiceJava.java': '''
                @Tx
                public class PersonServiceJava { }
            ''',

            'Color.java': '''
                public enum Color {
                    RED, BLUE, GREEN;
                }
            '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('Tx')
        assert stubSource.contains('java.lang.String value() default "def";')
        assert stubSource.contains('int i() default 3;')
        assert stubSource.contains('boolean bool() default false;')
        assert stubSource.contains('float f() default 3.0f;')
        assert stubSource.contains('double d() default 3.0d;')
        assert stubSource.contains('long l() default 3L;')
        assert stubSource.contains('byte me() default 3;')
        assert stubSource.contains('short s() default 3;')
        assert stubSource.contains("char c1() default 'A';")
        assert stubSource.contains("char c2() default 'A';")
        assert stubSource.contains("char c3() default 'A';")
        assert stubSource.contains('java.lang.Class clazz() default String.class;')
        assert stubSource.contains('Color color() default Color.RED;')
        assert stubSource.contains('String[] values() default { "def" };')
        assert stubSource.contains('int[] is() default { 3 };')
        assert stubSource.contains('long[] ls() default {  };')
        assert stubSource.contains('boolean[] bs() default { true };')
        assert stubSource.contains('Class[] clazzes() default { String.class, Integer.class };')
        assert stubSource.contains('Color[] colors() default { Color.GREEN, Color.BLUE };')
    }
}
