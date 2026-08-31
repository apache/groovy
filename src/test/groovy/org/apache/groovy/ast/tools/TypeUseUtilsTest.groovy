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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.junit.jupiter.api.Test

import static org.apache.groovy.ast.tools.TypeUseUtils.describeTypeUse
import static org.apache.groovy.ast.tools.TypeUseUtils.isParameterizedEnclosingType
import static org.apache.groovy.ast.tools.TypeUseUtils.isParameterizedTypeUsage
import static org.apache.groovy.ast.tools.TypeUseUtils.isParameterizedTypeUsageIncludingEnclosing
import static org.apache.groovy.ast.tools.TypeUseUtils.isRawGenericType
import static org.apache.groovy.ast.tools.TypeUseUtils.isReifiable
import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Tests for {@link TypeUseUtils}.
 */
final class TypeUseUtilsTest {

    @Test
    void 'declaration formals are not a parameterized usage'() {
        ClassNode list = ClassHelper.LIST_TYPE
        assertFalse(isParameterizedTypeUsage(list))
        assertFalse(list.redirectNode)
    }

    @Test
    void 'use-site arguments are a parameterized usage'() {
        ClassNode usage = ClassHelper.LIST_TYPE.plainNodeReference
        usage.genericsTypes = [new GenericsType(ClassHelper.STRING_TYPE)] as GenericsType[]
        assertTrue(isParameterizedTypeUsage(usage))
        assertTrue(isParameterizedEnclosingType(usage))
        assertFalse(isReifiable(usage))
        assertEquals('List<java.lang.String>', describeTypeUse(usage))
    }

    @Test
    void 'raw generic type is reifiable'() {
        ClassNode raw = ClassHelper.LIST_TYPE.plainNodeReference
        raw.genericsTypes = null
        assertTrue(isRawGenericType(raw))
        assertTrue(isReifiable(raw))
        assertFalse(isParameterizedTypeUsage(raw))
    }

    @Test
    void 'unbounded wildcard is reifiable'() {
        ClassNode star = ClassHelper.LIST_TYPE.plainNodeReference
        GenericsType wildcard = new GenericsType(ClassHelper.OBJECT_TYPE, null, null)
        wildcard.wildcard = true
        star.genericsTypes = [wildcard] as GenericsType[]
        assertTrue(isReifiable(star))
        assertTrue(isReifiable(star.makeArray()))
    }

    @Test
    void 'type parameter and diamond are not reifiable'() {
        ClassNode placeholder = ClassHelper.makeWithoutCaching('T')
        placeholder.genericsPlaceHolder = true
        placeholder.redirect = ClassHelper.OBJECT_TYPE
        assertFalse(isReifiable(placeholder))
        assertEquals('T', describeTypeUse(placeholder))

        ClassNode diamond = ClassHelper.LIST_TYPE.plainNodeReference
        diamond.genericsTypes = GenericsType.EMPTY_ARRAY
        assertFalse(isReifiable(diamond))
    }

    @Test
    void 'generic type name of Map Entry is not a parameterized class literal'() {
        ClassNode map = ClassHelper.MAP_TYPE
        ClassNode entry = ClassHelper.makeWithoutCaching('java.util.Map$Entry')
        entry.redirect = ClassHelper.make(Map.Entry)
        // Declaration formals on Map must not make Map.Entry a class-literal error.
        assertFalse(isParameterizedTypeUsage(map))
        assertFalse(isParameterizedTypeUsageIncludingEnclosing(entry))
    }

    @Test
    void 'rare type with parameterized enclosing is a parameterized class literal'() {
        ClassNode outer = ClassHelper.LIST_TYPE.plainNodeReference
        outer.genericsTypes = [new GenericsType(ClassHelper.STRING_TYPE)] as GenericsType[]
        ClassNode inner = ClassHelper.makeWithoutCaching('java.util.List$Inner')
        inner.outerClassType = outer
        assertTrue(isParameterizedTypeUsageIncludingEnclosing(inner))
        assertEquals('List<java.lang.String>.Inner', describeTypeUse(inner))
    }
}
