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

import groovy.test.GroovyTestCase

/**
 * to prove GROOVY-467 is no longer an issue    
 */
class AmbiguousInvocationTest extends GroovyTestCase {
    def dummy1, dummy2

    void setUp() {
        super.setUp()
        dummy1 = new groovy.DummyMethodsJava()
        dummy2 = new groovy.DummyMethodsGroovy()
    }

    void testAmbiguousInvocationWithFloats() {
        assert "float args" == dummy1.foo("bar", 1.0f, 2.0f)
        assert "float args" == dummy1.foo("bar", (float) 1, (float) 2)
        assert "float args" == dummy1.foo("bar", (Float) 1, (Float) 2)
        assert "float args" == dummy2.foo("bar", 1.0f, 2.0f)
        assert "float args" == dummy2.foo("bar", (float) 1, (float) 2)
        assert "float args" == dummy2.foo("bar", (Float) 1, (Float) 2)
    }

    void testAmbiguousInvocationWithInts() {
        assert "int args" == dummy1.foo("bar", 1, 2)
        assert "int args" == dummy1.foo("bar", (int) 1, (int) 2)
        assert "int args" == dummy1.foo("bar", (Integer) 1, (Integer) 2)
        assert "int args" == dummy2.foo("bar", 1, 2)
        assert "int args" == dummy2.foo("bar", (int) 1, (int) 2)
        assert "int args" == dummy2.foo("bar", (Integer) 1, (Integer) 2)
    }
} 
