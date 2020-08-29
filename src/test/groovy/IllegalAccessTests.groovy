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
package groovy

import org.junit.Before
import org.junit.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static org.junit.Assume.assumeTrue

/**
 * Tests for permissive member access.  Typically such access is only allowed in
 * Java via means such as reflection.
 *
 * In JDK versions < 9, Groovy supports permissive access and no warnings are given by the JDK.
 * In JDK versions >= 9, Groovy supports permissive access but the JDK gives illegal access warnings.
 * At some point, the JDK may further restrict permissive access and Groovy's support for this feature may be limited.
 */
final class IllegalAccessTests {

    @Before
    void setUp() {
        assumeTrue(isAtLeastJdk('9.0') && !Boolean.getBoolean('groovy.force.illegal.access'))
    }

    @Test
    void testReadPrivateField() {
        assertScript '''
            def items = [1, 2, 3]
            assert items.size == 3 // "size" is private
        '''
    }

    @Test
    void testReadPackageProtectedField() {
        // TODO: move A to another package
        assertScript '''
            class A {
                @groovy.transform.PackageScope int i
            }
            class B extends A {
                def eye() { super.i }
            }
            assert new B().eye() == 0
        '''
    }

    @Test // GROOVY-9596
    void testReadProtectedFieldFromSuperClass() {
        // in is a protected field in FilterReader
        assertScript '''
            class MyFilterReader extends FilterReader {
                MyFilterReader(Reader reader) {
                    super(new BufferedReader(reader))
                }
                String nextLine() {
                    ((BufferedReader) this.in).readLine()?.trim() // "in" is protected
                }
            }

            def input =
                "    works \\t\\n" +
                "hello there    \\n" +
                "hi\\n" +
                "\\n"
            def reader = new CharArrayReader(input.toCharArray())
            reader = new MyFilterReader(reader)
            assert reader.nextLine() == 'works'
        '''
    }
}
