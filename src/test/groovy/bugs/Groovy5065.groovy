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

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

/**
 * GROOVY-5065: overriding a method on a Java class metaClass used
 * not to take effect on new instances until the instance metaClass
 * was also mutated. The original report used {@code String.toString}.
 */
final class Groovy5065 {

    @AfterEach
    void tearDown() {
        String.metaClass = null
    }

    @Test
    void testMetaClassOverrideAppliesToNewJavaInstance() {
        String.metaClass.toString = { -> 'meta method' }
        def s = new String('original string')
        assert s.toString() == 'meta method'
    }

    @Test
    void testMetaClassOverrideDoesNotRequireLaterInstanceMutation() {
        String.metaClass.toString = { -> 'meta method' }
        def s = new String('original string')
        assert s.toString() == 'meta method'
        s.metaClass.randomChange = {}
        assert s.toString() == 'meta method'
    }
}
