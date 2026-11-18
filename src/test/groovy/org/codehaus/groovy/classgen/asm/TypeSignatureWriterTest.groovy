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
package org.codehaus.groovy.classgen.asm

import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.GenericsType
import org.junit.jupiter.api.Test

final class TypeSignatureWriterTest {

    @Test
    void writesArgumentsThatMatchTheDeclaration() {
        ClassNode map = ClassHelper.MAP_TYPE.getPlainNodeReference()
        map.genericsTypes = [
                ClassHelper.STRING_TYPE.asGenericsType(),
                ClassHelper.Integer_TYPE.asGenericsType()
        ] as GenericsType[]
        String signature = write(map)
        assert signature.contains('java/util/Map')
        assert signature.contains('Ljava/lang/String;')
        assert signature.contains('Ljava/lang/Integer;')
    }

    @Test
    void dropsArgumentsAResolvedClassDoesNotDeclare() {
        ClassNode string = ClassHelper.STRING_TYPE.getPlainNodeReference()
        string.genericsTypes = [ClassHelper.STRING_TYPE.asGenericsType()] as GenericsType[]
        String signature = write(string)
        assert signature.contains('java/lang/String')
        assert !signature.contains('<')
    }

    @Test
    void writesAParameterizedEnclosingTypeInNestedForm() {
        ClassNode outer = ClassHelper.make(HashMap).getPlainNodeReference()
        outer.genericsTypes = [
                ClassHelper.STRING_TYPE.asGenericsType(),
                ClassHelper.Integer_TYPE.asGenericsType()
        ] as GenericsType[]
        ClassNode inner = ClassHelper.makeWithoutCaching('java.util.HashMap$Node')
        inner.setOuterClassType(outer)
        String signature = write(inner)
        assert signature.contains('java/util/HashMap')
        assert signature.contains('.Node')
        assert signature.contains('Ljava/lang/String;')
    }

    @Test
    void leavesArgumentsOnAPlaceholderAndDropsThemOnAModulessPrimary() {
        ClassNode placeholder = ClassHelper.makeWithoutCaching('T')
        placeholder.genericsPlaceHolder = true
        placeholder.genericsTypes = [ClassHelper.STRING_TYPE.asGenericsType()] as GenericsType[]
        String kept = write(placeholder)
        assert kept.contains('<') || kept.contains('T')

        ClassNode carrier = new ClassNode('Carrier', 0, ClassHelper.OBJECT_TYPE)
        assert carrier.module == null
        carrier.genericsTypes = [ClassHelper.STRING_TYPE.asGenericsType()] as GenericsType[]
        String dropped = write(carrier)
        assert !dropped.contains('<')
    }

    private static String write(ClassNode type) {
        StringBuilder out = new StringBuilder()
        TypeSignatureWriter.writeParameterizedClass(out, type)
        return out.toString()
    }
}
