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
package org.apache.groovy.ast.tools

import org.junit.jupiter.api.Test

import static org.apache.groovy.ast.tools.ClassNodeUtils.getPropNameForAccessor
import static org.junit.jupiter.api.Assertions.assertEquals

/**
 * Tests for {@link ClassNodeUtils}.
 */
final class ClassNodeUtilsTest {

    @Test
    void 'getPropNameForAccessor strips the accessor prefix'() {
        assertEquals('age', getPropNameForAccessor('getAge'))
        assertEquals('age', getPropNameForAccessor('setAge'))
        assertEquals('active', getPropNameForAccessor('isActive'))
        assertEquals('x', getPropNameForAccessor('getX'))
    }

    @Test
    void 'getPropNameForAccessor uses JavaBean decapitalization for acronyms'() {
        // GROOVY-12055 follow-up: must match the runtime rule (MetaClassImpl uses
        // BeanUtils.decapitalize), e.g. getURL -> URL, not uRL
        assertEquals('URL', getPropNameForAccessor('getURL'))
        assertEquals('URL', getPropNameForAccessor('setURL'))
        assertEquals('URLConnection', getPropNameForAccessor('getURLConnection'))
    }

    @Test
    void 'getPropNameForAccessor returns the original when not a valid accessor'() {
        assertEquals('foo', getPropNameForAccessor('foo'))
        assertEquals('get', getPropNameForAccessor('get'))
        assertEquals('is', getPropNameForAccessor('is'))
    }
}
