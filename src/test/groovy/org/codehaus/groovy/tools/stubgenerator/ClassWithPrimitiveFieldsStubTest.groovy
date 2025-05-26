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
 * Checks the stub generator defines initialization expressions for primitive fields.
 */
final class ClassWithPrimitiveFieldsStubTest extends StringSourcesStubTestCase {

    Map<String, String> provideSources() {
        [
            'Dummy.java': '''
                public class Dummy {
                }
            ''',

            'SomeClass.groovy': '''
                class SomeClass {
                    public static final String s
                    public static final Integer foo
                    public static final double d
                    public static final long l
                    public static final int bar
                    public static final short baz
                    public static final char c
                    public static final byte b
                    public static final boolean flag
                    public static final Boolean flag2
                }
            '''
        ]
    }

    void verifyStubs() {
        def stubSource = stubJavaSourceFor('SomeClass')
        assert stubSource.contains('java.lang.String s = java.lang.String.valueOf((java.lang.String)null)')
        assert stubSource.contains('java.lang.Integer foo = null')
        assert stubSource.contains('double d = java.lang.Double.valueOf(0.0d)')
        assert stubSource.contains('final long l = java.lang.Long.valueOf(0L)')
        assert stubSource.contains('int bar = java.lang.Integer.valueOf(0)')
        assert stubSource.contains('short baz = java.lang.Short.valueOf(')
        assert stubSource.contains("char c = java.lang.Character.valueOf('\0')")
        assert stubSource.contains('byte b = java.lang.Byte.valueOf((byte)0)')
        assert stubSource.contains('boolean flag = java.lang.Boolean.valueOf(false)')
        assert stubSource.contains('java.lang.Boolean flag2 = null')
    }
}
