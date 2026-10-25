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
 * A hand-written {@code isX()} in a record with a primitive {@code boolean}
 * component {@code x} was taken as the component's accessor, so no {@code x()}
 * accessor was generated.
 */
final class Groovy12451 {

    @Test
    void testAccessorGeneratedAlongsideIsser() {
        assertScript '''
            import groovy.transform.CompileStatic

            import static groovy.test.GroovyAssert.isAtLeastJdk

            @CompileStatic
            record S8(boolean done) {
                boolean isDone() { done }
            }

            @CompileStatic
            class M8 {
                static boolean check() { new S8(true).done() }
            }

            assert M8.check()
            if (isAtLeastJdk('16.0')) { // native record
                assert S8.recordComponents*.accessor*.name == ['done']
            }
        '''
    }

    @Test
    void testGeneratedRecordMethodsUseAccessor() {
        assertScript '''
            record R(boolean done, boolean other) {
                boolean isDone() { !done }
            }

            def r = new R(true, false)
            assert r.done()
            assert !r.isDone()
            assert r.toList() == [true, false]
            assert r.toMap() == [done: true, other: false]
            assert r[0]
            assert R.declaredMethods.count { it.name == 'isDone' } == 1
        '''
    }

    @Test
    void testEmulatedRecord() {
        assertScript '''
            import groovy.transform.RecordOptions
            import groovy.transform.RecordTypeMode

            @RecordOptions(mode=RecordTypeMode.EMULATE)
            record R(boolean done) {
                boolean isDone() { !done }
            }

            def r = new R(true)
            assert r.done()
            assert !r.isDone()
            assert r.toList() == [true]
        '''
    }
}
