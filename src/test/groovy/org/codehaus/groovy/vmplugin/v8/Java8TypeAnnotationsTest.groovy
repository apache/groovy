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
package org.codehaus.groovy.vmplugin.v8

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.junit.jupiter.api.Test

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

import static org.junit.jupiter.api.Assertions.assertEquals
import static org.junit.jupiter.api.Assertions.assertTrue

/**
 * GROOVY-12389: type-use annotations on precompiled classes are read through
 * {@code AnnotatedType} when the reflection API has it, which every JVM does;
 * a runtime without it (Android's ART) skips them instead of failing.
 */
final class Java8TypeAnnotationsTest {

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE_USE)
    static @interface Marked {}

    static class Subject extends @Marked Object {
        @Marked String field
        @Marked String method(@Marked String parameter) { parameter }
    }

    @Test
    void typeAnnotationsAreAvailableOnAJvm() {
        assertTrue(Java8.typeAnnotationsAvailable())
    }

    @Test
    void typeUseAnnotationsOfAPrecompiledClassAreStillSeen() {
        ClassNode node = ClassHelper.make(Subject)
        node.getMethods('method') // forces reflective configuration of the class node
        def marked = { ClassNode type -> type.typeAnnotations.any { it.classNode.name == Marked.name } }
        assertTrue(marked(node.getDeclaredField('field').type), 'field type')
        def method = node.getDeclaredMethods('method')[0]
        assertTrue(marked(method.returnType), 'return type')
        assertTrue(marked(method.parameters[0].type), 'parameter type')
        assertTrue(marked(node.unresolvedSuperClass), 'superclass')
        assertEquals(1, node.getDeclaredMethods('method').size())
    }
}
