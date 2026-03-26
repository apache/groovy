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
package groovy.grape

import org.junit.jupiter.api.Test

final class GrapeImplementationSelectionTest {
    @Test
    void testConfiguredImplementationMismatchSkipsServiceLoad() {
        // This module runs with both implementations available; selecting an implementation
        // that is not on this test runtime classpath should disable Grapes.
        System.setProperty('groovy.grape.impl', 'groovy.grape.nonexistent.DoesNotExist')
        String output = GrapeSelectionTestSupport.captureStderr {
            assert Grape.instance == null
        }

        assert output.contains("configured implementation 'groovy.grape.nonexistent.DoesNotExist' not found")
        assert output.contains('Grapes disabled')
    }
}
