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
package org.apache.groovy.toml.util

import groovy.toml.TomlRuntimeException
import org.junit.jupiter.api.Test
import tools.jackson.dataformat.toml.TomlMapper

import static groovy.test.GroovyAssert.shouldFail

class TomlRootValuesTest {

    @Test
    void testCarrierMustStayABareKey() {
        // GROOVY-12738
        // A table is not wrapped by write(). Passing one here is the case where
        // Jackson expands the carrier into a path ("v.a = 1") instead of "v = ...".
        def mapper = TomlMapper.builder().configureForJackson2().build()
        def e = shouldFail(TomlRuntimeException) {
            TomlRootValues.writeWithEmptyKey(mapper, [a: 1])
        }
        assert e.message.contains('document root')
    }
}
