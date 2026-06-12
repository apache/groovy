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
package org.codehaus.groovy.transform.stc

import org.codehaus.groovy.ast.ClassNode
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.ClassHelper.*

final class UnionTypeClassNodeTest {

    @Test
    void testBasicEquivalence() {
        ClassNode cn = new UnionTypeClassNode(LIST_TYPE, MAP_TYPE)

        assert cn.equals(cn)
        assert cn.isDerivedFrom(cn)
        assert cn.isDerivedFrom(OBJECT_TYPE)
    }

    @Test
    void testIsGroovyObject0() {
        ClassNode cn = new UnionTypeClassNode(GROOVY_OBJECT_TYPE, LIST_TYPE, MAP_TYPE)

        assert !cn.isDerivedFromGroovyObject()
    }

    @Test
    void testIsGroovyObject1() {
        ClassNode cn = new UnionTypeClassNode(GROOVY_INTERCEPTABLE_TYPE, GROOVY_OBJECT_SUPPORT_TYPE)

        assert cn.isDerivedFromGroovyObject()
    }

    @Test
    void testIsInterface0() {
        ClassNode cn = new UnionTypeClassNode(LIST_TYPE, OBJECT_TYPE)

        assert !cn.isInterface()
    }

    @Test
    void testIsInterface1() {
        ClassNode cn = new UnionTypeClassNode(LIST_TYPE, MAP_TYPE)

        assert cn.isInterface()
    }
}
