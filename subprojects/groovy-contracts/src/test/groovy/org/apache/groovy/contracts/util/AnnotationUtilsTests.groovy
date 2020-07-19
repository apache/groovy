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

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.builder.AstBuilder
import org.codehaus.groovy.control.CompilePhase
import org.apache.groovy.contracts.annotations.meta.Precondition
import org.apache.groovy.contracts.tests.basic.BaseTestClass
import org.junit.Test
import static org.junit.Assert.assertEquals

class AnnotationUtilsTests extends BaseTestClass {

    def source = '''
    @Contracted
    package tests

    import groovy.contracts.*

    class Tester {

        @Requires({ param != null })
        def method(def param) {}

    }'''

    @Test
    void find_annotations_with_meta_annos() {
        AstBuilder astBuilder = new AstBuilder()
        def astNodes = astBuilder.buildFromString(CompilePhase.SEMANTIC_ANALYSIS, false, source)

        ClassNode classNode = astNodes[1]
        MethodNode methodNode = classNode.getMethod("method", [new Parameter(ClassHelper.makeWithoutCaching("java.lang.Object"), "param")] as Parameter[])

        def annotationNodes = AnnotationUtils.hasMetaAnnotations(methodNode, Precondition.class.getName())
        assertEquals(1, annotationNodes.size())
    }
}
