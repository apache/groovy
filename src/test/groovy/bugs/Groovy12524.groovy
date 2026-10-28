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
package bugs

import org.junit.jupiter.api.Test

import static groovy.test.GroovyAssert.assertScript
import static groovy.test.GroovyAssert.shouldFail

/**
 * GROOVY-12524: compound assignment to a property defined by extension methods.
 */
final class Groovy12524 {

    @Test
    void testCompoundAssignExtensionProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                String test(File file) {
                    String result = file.text += 'b'
                    result
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = 'a'
                assert new C().test(file) == 'ab'
                assert file.text == 'ab'
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testCompoundAssignExtensionPropertyOnSubclass() {
        assertScript '''
            @groovy.transform.CompileStatic
            class F extends File {
                F(String path) { super(path) }
                String test() {
                    this.text += 'b'
                    text += 'c'
                    text
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = 'a'
                assert new F(file.absolutePath).test() == 'abc'
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testCompoundAssignEvaluatesReceiverTwice() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                int calls
                File file
                File next() { calls++; file }
                int test() {
                    next().text += 'Z'
                    calls
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = 'a'
                int dynamicCalls = 0
                def next = { dynamicCalls++; file }
                next().text += 'Z'
                file.text = 'a'

                C c = new C(file: file)
                assert c.test() == dynamicCalls
                assert file.text == 'aZ'
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testCompoundAssignPrimitiveExtensionProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            int test(int[] box) {
                int added = box.groovy12524 += 3
                int scaled = box.groovy12524 *= 2
                assert added == 7
                scaled
            }

            int[] box = [4] as int[]
            assert test(box) == 14
            assert box[0] == 14
        '''
    }

    @Test
    void testCompoundAssignBooleanExtensionProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            boolean test(boolean[] box) {
                box.groovy12524On |= true
                box.groovy12524On
            }

            boolean[] box = [false] as boolean[]
            assert test(box)
            assert box[0]
        '''
    }

    @Test
    void testTypeCheckedCompoundAssignExtensionProperty() {
        assertScript '''
            @groovy.transform.TypeChecked
            class C {
                String test(File file) {
                    file.text += 'b'
                    file.text
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = 'a'
                assert new C().test(file) == 'ab'
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testElvisAssignExtensionProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                String test(File file, String fallback) {
                    String result = file.text ?= fallback
                    result
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = ''
                assert new C().test(file, 'b') == 'b'
                assert file.text == 'b'
                assert new C().test(file, 'c') == 'b'
                assert file.text == 'b'
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testSafeNavigationCompoundAssign() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static String update(File file) {
                    file?.text += 'b'
                }
                static Object updateNull() {
                    File file = null
                    try {
                        file?.text += 'b'
                        return 'ok'
                    } catch (Throwable t) {
                        return t.class.name
                    }
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = 'a'
                assert C.update(file) == 'ab'
                assert file.text == 'ab'

                Object dynamicResult
                File missing = null
                try {
                    missing?.text += 'b'
                    dynamicResult = 'ok'
                } catch (Throwable t) {
                    dynamicResult = t.class.name
                }
                assert C.updateNull() == dynamicResult
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testAttributeCompoundAssignKeepsFieldAccess() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                int n = 1
                int test() {
                    this.@n += 4
                    n
                }
            }
            assert new C().test() == 5
        '''
    }

    @Test
    void testAttributeCompoundAssignOfExtensionPropertyIsRejected() {
        shouldFail '''
            @groovy.transform.CompileStatic
            void update(File file) {
                file.@text += 'b'
            }
        '''
    }

    @Test
    void testSpreadSafeCompoundAssignIsNotRewrittenAsExtensionProperty() {
        shouldFail '''
            @groovy.transform.CompileStatic
            int update(List<File> files) {
                files*.text += 'b'
                0
            }
        '''
    }

    @Test
    void testDelegateCompoundAssignExtensionProperty() {
        assertScript '''
            @groovy.transform.CompileStatic
            class C {
                static String test(File file) {
                    file.with {
                        text += 'b'
                    }
                    file.text
                }
            }

            File file = File.createTempFile('groovy12524', null)
            try {
                file.text = 'a'
                assert C.test(file) == 'ab'
            } finally {
                file.delete()
            }
        '''
    }

    @Test
    void testRealGetterWithExtensionSetterUsesExtensionGetterLookup() {
        assertScript '''
            import org.codehaus.groovy.runtime.m12n.Groovy12524Extension.Holder

            @groovy.transform.CompileStatic
            class H extends Holder {
                String test() {
                    label += 'b'
                    label
                }
            }

            Holder holder = new Holder()
            holder.value = 'a'
            holder.label += 'b'
            assert holder.value == 'ab'
            assert holder.label == 'ab'
            assert new H().test() == 'b'
        '''
    }

    @Test
    void testBooleanAndStringExtensionGetters() {
        assertScript '''
            import org.codehaus.groovy.runtime.m12n.Groovy12524Extension.Holder

            @groovy.transform.CompileStatic
            void update(Holder holder) {
                holder.tag += 'b'
            }

            Holder holder = new Holder()
            holder.value = 'a'
            update(holder)
            assert holder.value == 'ab'
        '''
    }

    @Test
    void testPrimitiveExtensionSetterWithoutGetterIsRejected() {
        shouldFail '''
            @groovy.transform.CompileStatic
            void update(int n) {
                n.groovy12524Raw += 1
            }
        '''
    }
}
