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

import static org.codehaus.groovy.ast.AnnotationNode.*
import static org.junit.jupiter.api.Assertions.*

final class AnnotationNodeTest {

    // GROOVY-6526
    @Test
    void testIsTargetAllowed1() {
        def node = new AnnotationNode(ClassHelper.OVERRIDE_TYPE)

        assertTrue(node.isTargetAllowed(METHOD_TARGET))

        for (target in [TYPE_TARGET, CONSTRUCTOR_TARGET, FIELD_TARGET, PARAMETER_TARGET,
                        LOCAL_VARIABLE_TARGET, ANNOTATION_TARGET, PACKAGE_TARGET,
                        TYPE_PARAMETER_TARGET, TYPE_USE_TARGET, RECORD_COMPONENT_TARGET]) {
            assertFalse(node.isTargetAllowed(target))
        }
    }

    // GROOVY-6526
    @Test
    void testIsTargetAllowed2() {
        def node = new AnnotationNode(ClassHelper.DEPRECATED_TYPE)

        for (target in [TYPE_TARGET, CONSTRUCTOR_TARGET, METHOD_TARGET, FIELD_TARGET,
                        LOCAL_VARIABLE_TARGET, PARAMETER_TARGET, PACKAGE_TARGET]) {
            assertTrue(node.isTargetAllowed(target))
        }

        for (target in [TYPE_PARAMETER_TARGET, TYPE_USE_TARGET, RECORD_COMPONENT_TARGET]) {
            assertFalse(node.isTargetAllowed(target))
        }
    }

    // GROOVY-11838
    @Test
    void testIsTargetAllowed3() {
        def node = new AnnotationNode(new ClassNode("A", 0x2000, ClassHelper.Annotation_TYPE))

        for (target in [TYPE_TARGET, CONSTRUCTOR_TARGET, METHOD_TARGET, FIELD_TARGET,
                        PARAMETER_TARGET, LOCAL_VARIABLE_TARGET, ANNOTATION_TARGET,
                        PACKAGE_TARGET, RECORD_COMPONENT_TARGET]) {
            assertTrue(node.isTargetAllowed(target))
        }
        assertFalse(node.isTargetAllowed(TYPE_USE_TARGET))
        assertFalse(node.isTargetAllowed(TYPE_PARAMETER_TARGET))
    }

    @Test
    void testRetentionPolicy1() {
        def node = new AnnotationNode(ClassHelper.make(gls.annotations.HasExplicitClassRetention))

        assertTrue (node.hasClassRetention())
        assertFalse(node.hasSourceRetention())
        assertFalse(node.hasRuntimeRetention())
    }

    @Test
    void testRetentionPolicy2() {
        def node = new AnnotationNode(ClassHelper.OVERRIDE_TYPE)

            assertFalse(node.hasClassRetention())
            assertTrue (node.hasSourceRetention())
            assertFalse(node.hasRuntimeRetention())
    }

    @Test
    void testRetentionPolicy3() {
        def node = new AnnotationNode(ClassHelper.DEPRECATED_TYPE)

        assertFalse(node.hasClassRetention())
        assertFalse(node.hasSourceRetention())
        assertTrue (node.hasRuntimeRetention())
    }

    @Test
    void testRetentionPolicy4() {
        def node = new AnnotationNode(new ClassNode("A", 0x2000, ClassHelper.Annotation_TYPE))

        assertTrue (node.hasClassRetention())
        assertFalse(node.hasSourceRetention())
        assertFalse(node.hasRuntimeRetention())
    }

    @Test
    void testGetText() {
        def node = new AnnotationNode(ClassHelper.OVERRIDE_TYPE)

        assertEquals('@java.lang.Override', node.getText())
    }

    @Test
    void testGetText2() {
        def node = new AnnotationNode(ClassHelper.DEPRECATED_TYPE)
        node.addMember('since', new ConstantExpression('1.2.3'))

        assertEquals('@java.lang.Deprecated(since="1.2.3")', node.getText())
    }
}
