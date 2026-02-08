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
package org.apache.groovy.contracts.util

import org.apache.groovy.contracts.annotations.meta.Precondition
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.junit.Test

final class AnnotationUtilsTests extends BaseTestClass {

    @Test
    void hasMetaAnnotations1() {
        def astNodes = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, '''
            @Contracted
            package tests

            import groovy.contracts.*

            class Tester {
                @Requires({ param != null })
                def method(def param) {
                }
            }
        ''')

        ClassNode classNode = astNodes[1]
        MethodNode methodNode = classNode.getMethod('method', new Parameter(ClassHelper.OBJECT_TYPE, 'param'))
        List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, Precondition.getName())

        assert annotationNodes.size() == 1
    }

    @Test // GROOVY-10857
    void hasMetaAnnotations2() {
        def astNodes = new AstBuilder().buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, '''
            import java.lang.annotation.*
            import static java.lang.annotation.ElementType.*
            import static java.lang.annotation.RetentionPolicy.*

            @A @Documented @Retention(RUNTIME) @Target(TYPE)
            @interface A {
            }
            @A @Documented @Retention(RUNTIME) @Target([FIELD,METHOD,PARAMETER])
            @interface B {
            }
            interface I<T> {
                @B T m()
            }
        ''')

        ClassNode classNode = astNodes[3] // interface I
        MethodNode methodNode = classNode.getMethod('m')
        List<AnnotationNode> annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, 'A')

        assert annotationNodes.size() == 1

        annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, Precondition.getName())

        assert annotationNodes.isEmpty()
    }
}
