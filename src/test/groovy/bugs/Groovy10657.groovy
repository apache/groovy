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
            assert (receiver?[0] = { log << "rhs"; "v" }()) == "v"
            assert log == ["rhs"]
        '''
    }

    @Test
    void testMapAndListAssignmentValueIsRhs() {
        assertScript '''
            def map = [a: "old"]
            assert (map["a"] = "new") == "new"
            assert map.a == "new"

            def list = ["old"]
            assert (list[0] = "rhs") == "rhs"
            assert list == ["rhs"]

            @groovy.transform.CompileStatic
            String statically(List<String> values) {
                (values[0] = "rhs")
            }
            def typed = ["old"]
            assert statically(typed) == "rhs"
            assert typed == ["rhs"]
        '''
    }

    @Test
    void testElvisSubscriptReleasesReceiver() {
        assertScript HARNESS + '''
            boolean releases() {
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                def list = [payload]
                list[0] ?= "kept"
                list = null
                payload = null
                return awaitCollected(ref)
            }
            assert releases()
        '''
    }

    @Test
    void testCompoundSubscriptReleasesResult() {
        assertScript HARNESS + '''
            class Cell {
                Object plus(Object other) { other }
            }
            class Box {
                Cell cell = new Cell()
                Object stored
                Object getAt(int i) { cell }
                void putAt(int i, Object value) { stored = value }
            }

            boolean releases() {
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                def box = new Box()
                assert (box[0] += payload) === payload
                assert box.stored === payload
                payload = null
                box.stored = null
                return awaitCollected(ref)
            }
            assert releases()
        '''
    }

    @Test
    void testPostfixSubscriptReleasesReceiver() {
        assertScript HARNESS + '''
            class Item {
                byte[] payload
                Item next() { this }
            }
            class Box {
                Object value
                Object getAt(int i) { value }
                void putAt(int i, Object v) { value = v }
            }

            boolean releases() {
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                def box = new Box(value: new Item(payload: payload))
                payload = null
                box[0]++
                box = null
                return awaitCollected(ref)
            }
            assert releases()
        '''
    }

    @Test
    void testArrayExpressionAssignmentReleasesRhs() {
        assertScript HARNESS + '''
            boolean releases() {
                Object[] array = new Object[1]
                byte[] payload = new byte[1 << 20]
                def ref = new WeakReference(payload)
                def kept = (array[0] = payload)
                assert kept === payload
                payload = null
                kept = null
                array[0] = null
                return awaitCollected(ref)
            }
            assert releases()
        '''
    }
}
