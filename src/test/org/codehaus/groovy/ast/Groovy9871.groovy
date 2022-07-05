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

import org.codehaus.groovy.ast.expr.AnnotationConstantExpression
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.expr.ListExpression
import org.junit.Test

final class Groovy9871 {

    @Test
    void testAnnotationConstantExpression() {
        ClassNode cn = new ClassNode(org.codehaus.groovy.runtime.ResourceGroovyMethods)
        // method with @NamedParam annotations that should be wrapped in @NamedParams container
        MethodNode mn = cn.getMethod('traverse', new Parameter(ClassHelper.make(File), 'file'), new Parameter(ClassHelper.MAP_TYPE, 'options'))

        List<AnnotationNode> annotations = mn.parameters[1].annotations

        assert annotations.size() == 1
        assert annotations[0].classNode.name == 'groovy.transform.NamedParams'
        assert annotations[0].members.value instanceof ListExpression

        List<Expression> expressions = annotations[0].members.value.expressions

        assert expressions.size() > 1 // 12 currently
        assert expressions[0] instanceof AnnotationConstantExpression
        assert expressions[0].type.name == 'groovy.transform.NamedParam'
    }
}
