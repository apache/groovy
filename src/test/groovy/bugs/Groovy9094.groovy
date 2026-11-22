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
 * GROOVY-9094: assigning {@code 255 / 2} to a primitive {@code double}
 * must yield {@code 127.5} in both dynamic and static compilation,
 * matching the wrapper and untyped cases (historically the dynamic
 * primitive path truncated to {@code 127.0}, and {@code @TypeChecked}
 * rejected a {@code Double} target).
 */
final class Groovy9094 {

    @Test
    void testDynamicPrimitiveDoubleDivisionIsNotTruncated() {
        assertScript '''
            double x = 255 / 2
            Double y = 255 / 2
            def z = 255 / 2
            assert x == 127.5d
            assert y == 127.5
            assert z == 127.5
            assert (255 / 2) == 127.5
        '''
    }

    @Test
    void testCompileStaticAndTypeCheckedAreConsistent() {
        assertScript '''
            @groovy.transform.CompileStatic
            def csPrimitive() {
                double x = 255 / 2
                x
            }
            @groovy.transform.CompileStatic
            def csWrapper() {
                Double x = 255 / 2
                x
            }
            @groovy.transform.TypeChecked
            def tcPrimitive() {
                double x = 255 / 2
                x
            }
            @groovy.transform.TypeChecked
            def tcWrapper() {
                Double x = 255 / 2
                x
            }
            assert csPrimitive() == 127.5d
            assert csWrapper() == 127.5
            assert tcPrimitive() == 127.5d
            assert tcWrapper() == 127.5
        '''
    }
}
