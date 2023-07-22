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

import org.junit.Test

import java.awt.Font
import java.lang.annotation.RetentionPolicy

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.isAtLeastJdk
import static groovy.test.GroovyAssert.shouldFail

/**
 * Tests for permissive member access.  Typically such access is only allowed in
 * Java via means such as reflection.
 *
 * In JDK versions < 9, Groovy supports permissive access and no warnings are given by the JDK.
 * In JDK versions in 9..15, Groovy supports permissive access but the JDK gives illegal access warnings.
 * In JDK versions >= 16, permissive access is restricted and Groovy's support for this feature is limited.
 */
final class IllegalAccessTests {

    static class ProtectedConstructor {
        protected ProtectedConstructor() {}
        void run() {}
    }

    //--------------------------------------------------------------------------

    @Test
    void testClone1() {
        assertScript '''
            def broadcastSeq(Object value) {
                value.clone()
            }

            assert broadcastSeq(new Tuple1('abc'))
        '''
    }

    @Test
    void testClone2() {
        assertScript '''
            class Value {
                @Override
                public Value clone() {
                    return new Value()
                }
            }
            def broadcastSeq(Object value) {
                value.clone()
            }

            assert broadcastSeq(new Value())
        '''
    }

    @Test
    void testClone3() {
        Object obj = new Tuple1('abc')
        assert obj.clone().getClass() === Tuple1.class
    }

    @Test
    void testClone4() {
        assertScript '''
            int[] nums = new int[] {1,2,3}
            int[] copy = nums.clone()
            assert copy !== nums
            assert copy == nums
        '''
    }

    // GROOVY-10747
    @Test
    void testClone5() {
        ['Object', 'Dolly'].each { typeName ->
            assertScript """
                class Dolly implements Cloneable {
                    public ${typeName} clone() {
                        return super.clone()
                    }
                    String name
                }

                def dolly = new Dolly(name: "The Sheep")
                def clone = dolly.clone()
                assert clone instanceof Dolly
            """
        }
    }

    // GROOVY-10747
    @Test
    void testClone6() {
        shouldFail CloneNotSupportedException, '''
            class Dolly {
                String name
            }

            def dolly = new Dolly(name: "The Sheep")
            dolly.clone()
        '''
    }

    @Test
    void testClone7() {
        ['Object', 'Dolly'].each { typeName ->
            assertScript """
                import static org.codehaus.groovy.runtime.InvokerHelper.*
                class Dolly implements Cloneable {
                    public ${typeName} clone() {
                        return super.clone()
                    }
                    String name
                }

                def dolly = new Dolly(name: "The Sheep")
                def clone = invokeMethod(dolly, 'clone', EMPTY_ARGS)
                assert clone instanceof Dolly
            """
        }
    }

    @Test
    void testClone8() {
        shouldFail CloneNotSupportedException, '''
            import static org.codehaus.groovy.runtime.InvokerHelper.*
            class Dolly {
                String name
            }

            def dolly = new Dolly(name: "The Sheep")
            invokeMethod(dolly, 'clone', EMPTY_ARGS)
        '''
    }

    @Test
    void testAsType1() {
        [run: {}] as TimerTask
    }

    @Test
    void testAsType2() {
        assertScript """import ${this.class.name}.ProtectedConstructor
            [run: {}] as ProtectedConstructor
        """
    }

    @Test
    void testGetProperty() {
        try {
            java.awt.Toolkit.defaultToolkit.systemClipboard
        } catch (java.awt.HeadlessException ignore) {
        }
    }

    @Test
    void testGetProperties() {
        String str = ''
        assert str.properties
    }

    @Test
    void testBigIntegerMultiply1() {
        assert 2G * 1
    }

    @Test
    void testBigIntegerMultiply2() {
        def a = 333g; int b = 2
        BigDecimal c = a * b
        assert c == 666
    }

    @Test
    void testReadPrivateJavaField() {
        String script = '''
            def items = [1, 2, 3]
            assert items.size == 3 // "size" is private
        '''
        if (isAtLeastJdk('16.0') && !Boolean.getBoolean('groovy.force.illegal.access')) {
            shouldFail MissingPropertyException, script
        } else {
            assertScript script
        }
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

    // GROOVY-9596
    @Test
    void testReadProtectedFieldOfSuperClass() {
        assertScript '''
            char[] input = '1234567890\\r\\nabcdefghij\\r\\n'.toCharArray()

            def reader = new FilterReader(new BufferedReader(new CharArrayReader(input))) {
                @Override
                int read() {
                    this.in.read() // "in" is protected field of super class
                }
            }
            assert reader.readLine() == '1234567890'
        '''
if (!isAtLeastJdk('16.0')) // TODO
        assertScript '''
            class MyFilterReader extends FilterReader {
                MyFilterReader(Reader reader) {
                    super(new BufferedReader(reader))
                }
                String nextLine() {
                    ((BufferedReader) in).readLine()?.trim()
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

    @Test
    void testPackagePrivateInnerClassMember() {
        def m = new HashMap(); m.a = 1
        m.entrySet().iterator().next().toString()
    }

    @Test
    void testAccessPublicMemberOfPrivateClass() {
        def m = Collections.unmodifiableMap([:])
        assert m.toString() != null
        assert m.get(0) == null
    }

    @Test
    void testFavorMethodWithExactParameterType() {
        def em1 = new EnumMap(RetentionPolicy.class)
        def em2 = new EnumMap(RetentionPolicy.class)
        assert em1 == em2
    }

    @Test
    void testShouldChoosePublicGetterInsteadOfPrivateField1() {
        def f = Integer.class.getDeclaredField('MIN_VALUE')
        assert f.modifiers != 0
    }

    @Test
    void testShouldChoosePublicGetterInsteadOfPrivateField2() {
        def f = new Font('Monospaced', Font.PLAIN, 12)
        assert f.name
    }
}
