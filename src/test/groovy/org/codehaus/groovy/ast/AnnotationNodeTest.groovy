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
package org.codehaus.groovy.ast

import org.codehaus.groovy.ast.expr.ConstantExpression
import org.junit.jupiter.api.Test

import static org.junit.jupiter.api.Assertions.*

final class AnnotationNodeTest {

    // GROOVY-11838
    @Test
    void testIsTargetAllowed() {
        def node = new AnnotationNode(ClassHelper.OVERRIDE_TYPE)

        assertTrue(node.isTargetAllowed(AnnotationNode.TYPE_TARGET))
        assertTrue(node.isTargetAllowed(AnnotationNode.FIELD_TARGET))
        assertTrue(node.isTargetAllowed(AnnotationNode.METHOD_TARGET))

        assertFalse(node.isTargetAllowed(AnnotationNode.TYPE_USE_TARGET))
        assertFalse(node.isTargetAllowed(AnnotationNode.TYPE_PARAMETER_TARGET))
    }

    @Test
    void testGetText() {
        def node = new AnnotationNode(ClassHelper.OVERRIDE_TYPE)

        assertEquals('@java.lang.Override', node.getText())
    }

    @Test
    void testGetText2() {
        def node = new AnnotationNode(ClassHelper.make(Deprecated))
        node.addMember('since', new ConstantExpression('1.2.3'))

        assertEquals('@java.lang.Deprecated(since="1.2.3")', node.getText())
    }
}
