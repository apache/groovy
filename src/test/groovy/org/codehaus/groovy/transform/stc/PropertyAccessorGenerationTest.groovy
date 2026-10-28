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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.junit.jupiter.api.Test

import java.lang.reflect.Modifier

import static org.junit.jupiter.api.Assertions.assertFalse
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * Direct calls for accessor-generation guards that source cannot reach:
 * a property or method whose declaring class was never set.
 */
final class PropertyAccessorGenerationTest {

    @Test
    void hierarchyWalkStopsWhenPropertyHasNoDeclaringClass() {
        def owner = new ClassNode('Owner', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        def property = new PropertyNode('flag', Modifier.PUBLIC, ClassHelper.boolean_TYPE, owner, null, null, null)
        assert property.declaringClass == null

        def walk = StaticTypeCheckingVisitor.getDeclaredMethod('hierarchyGeneratesAccessor', PropertyNode, String)
        walk.accessible = true
        assertFalse((Boolean) walk.invoke(null, property, 'isFlag'))
    }

    @Test
    void missingMethodDeclaringClassDoesNotBlockGeneration() {
        def owner = new ClassNode('Owner', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        def existing = new MethodNode('getFlag', Modifier.PUBLIC | Modifier.FINAL, ClassHelper.boolean_TYPE,
                Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, null)
        assert existing.declaringClass == null

        def decide = StaticTypeCheckingVisitor.getDeclaredMethod('needsGeneratedAccessor', ClassNode, MethodNode)
        decide.accessible = true
        assertTrue((Boolean) decide.invoke(null, owner, existing))
    }
}
