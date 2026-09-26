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
package org.apache.groovy.lsp.internal.feature

import java.lang.reflect.Modifier
import org.junit.jupiter.api.Test
import org.codehaus.groovy.ast.expr.VariableExpression
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.transform.stc.StaticTypesMarker

final class TypeInferenceTest {

    @Test
    void typeInferenceMarkersFieldsAndInitializers() {
        def owner = new ClassNode('demo.Infer', Modifier.PUBLIC, ClassHelper.OBJECT_TYPE)
        def marked = field(owner, 'n', 0, ClassHelper.OBJECT_TYPE, 1)
        marked.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.STRING_TYPE)
        assert TypeInference.of(marked).name == 'java.lang.String'

        def typed = new VariableExpression('s', ClassHelper.STRING_TYPE)
        assert !typed.dynamicTyped
        assert TypeInference.of(typed, null).name == 'java.lang.String'

        def count = field(owner, 'count', 0, ClassHelper.dynamicType(), 2)
        count.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, ClassHelper.STRING_TYPE)
        def dynamic = new VariableExpression('count')
        dynamic.accessedVariable = count
        assert dynamic.dynamicTyped
        assert TypeInference.of(dynamic, null).name == 'java.lang.String'

        def concrete = field(owner, 'name', 0, ClassHelper.STRING_TYPE, 3)
        assert !TypeInference.usedInitializer(concrete)

        def uninit = field(owner, 'pending', 0, ClassHelper.dynamicType(), 4)
        assert ClassHelper.isDynamicTyped(TypeInference.of(uninit))
    }

    private static FieldNode field(ClassNode owner, String name, int modifiers, ClassNode type, int line) {
        def node = new FieldNode(name, modifiers, type, owner, null)
        node.lineNumber = line
        node.columnNumber = 1
        node.lastLineNumber = line
        node.lastColumnNumber = 2
        node
    }
}
