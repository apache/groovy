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
package org.codehaus.groovy.classgen

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.control.SourceUnit
import org.junit.jupiter.api.Test

import static org.codehaus.groovy.ast.ClassHelper.OBJECT_TYPE
import static org.codehaus.groovy.ast.tools.GeneralUtils.block
import static org.objectweb.asm.Opcodes.ACC_PUBLIC

final class ExtendedVerifierTest {

    @Test
    void testNoTypeUseAnnotationsForVoidMethod() {
        ClassNode cn = new ClassNode('DummyClass', ACC_PUBLIC, OBJECT_TYPE)
        MethodNode mn = cn.addMethod("dummyMethod", ACC_PUBLIC, ClassHelper.VOID_TYPE, Parameter.EMPTY_ARRAY, ClassNode.EMPTY_ARRAY, block())
        mn.addAnnotation(ClassHelper.makeWithoutCaching('groovy.lang.Grapes'))
        new ExtendedVerifier(new SourceUnit((String)'dummySU', null, null, null, null)).visitClass(cn)
        assert mn.returnType.annotations.isEmpty()
    }
}
