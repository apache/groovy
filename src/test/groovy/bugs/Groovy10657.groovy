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
 * A subscript assignment must not leave the right-hand value in a compiler
 * temporary for the rest of the enclosing method. {@code putAt} itself may
 * retain the value; the temporary must not.
 */
final class Groovy10657 {

    private static final String HARNESS = '''
        import java.lang.ref.WeakReference

        class Sink {
            Object stored
            boolean retain
            void putAt(String key, Object value) {
                if (retain) stored = value
            }
        }

        boolean awaitCollected(ref) {
            for (int i = 0; i < 8; i++) {
                if (ref.get() == null) return true
                System.gc()
                byte[][] pressure = new byte[32][]
                for (int j = 0; j < pressure.length; j++) {
                    pressure[j] = new byte[1 << 17]
                }
            }
            return ref.get() == null
        }
    '''

    @Test
    void testStatementAssignmentReleasesRhs() {
        assertScript HARNESS + '''
            boolean releases(Sink sink) {
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                sink["lhs"] = payload
                payload = null
                return awaitCollected(ref)
            }
            assert releases(new Sink())
        '''
    }

    @Test
    void testExpressionAssignmentReleasesRhs() {
        assertScript HARNESS + '''
            boolean releases(Sink sink) {
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                def kept = (sink["lhs"] = payload)
                payload = null
                kept = null
                return awaitCollected(ref)
            }
            assert releases(new Sink())
        '''
    }

    @Test
    void testArrayAssignmentReleasesRhs() {
        assertScript HARNESS + '''
            boolean releases() {
                Object[] array = new Object[1]
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                array[0] = payload
                payload = null
                array[0] = null
                return awaitCollected(ref)
            }
            assert releases()
        '''
    }

    @Test
    void testCompileStaticAssignmentReleasesRhs() {
        assertScript HARNESS + '''
            @groovy.transform.CompileStatic
            boolean releases(Sink sink) {
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                sink["lhs"] = payload
                payload = null
                return awaitCollected(ref)
            }
            assert releases(new Sink())
        '''
    }

    @Test
    void testAssignmentValueIsRhsNotPutAtReturn() {
        assertScript HARNESS + '''
            def sink = new Sink(retain: true)
            assert (sink["lhs"] = "rhs") == "rhs"
            assert sink.stored == "rhs"

            def log = []
            def receiver = null
            receiver?[0] = { log << "rhs"; "v" }()
            assert log == ["rhs"]
        '''
    }
}
