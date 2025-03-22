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

import org.junit.jupiter.api.Test

final class InstanceofTest {

    @Test
    void testIsInstance() {
        def o = 12

        assert (o instanceof Integer)
    }

    @Test
    void testNotInstance() {
        def o = 12

        assert !(o instanceof Double)
    }

    @Test
    void testImportedClass() {
        def m = ["xyz":2]

        assert  (m  instanceof Map)
        assert !(m !instanceof Map)
        assert !(m  instanceof Double)
        assert  (m !instanceof Double)
    }

    @Test
    void testFullyQualifiedClass() {
        def l = [1, 2, 3]

        assert (l instanceof java.util.List)
        assert !(l instanceof java.util.Map)
        assert (l !instanceof java.util.Map)
    }

    @Test
    void testBoolean() {
       assert true instanceof Object
       assert true==true instanceof Object
       assert true==false instanceof Object
       assert true==false instanceof Boolean
       assert !new Object() instanceof Boolean
    }

    // GROOVY-11229
    @Test
    void testVariable() {
        Number n = 12345;
        if (n instanceof Integer i) {
            assert i.intValue() == 12345
        } else {
            assert false : 'expected Integer'
        }
        if (n instanceof String s) {
            assert false : 'not String'
        } else {
            assert n.intValue() == 12345
        }
    }
}
