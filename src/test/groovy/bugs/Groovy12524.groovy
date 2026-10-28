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
}
