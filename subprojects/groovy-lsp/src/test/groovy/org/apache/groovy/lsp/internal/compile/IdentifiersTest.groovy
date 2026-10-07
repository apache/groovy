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
package org.apache.groovy.lsp.internal.compile

import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.ClassHelper
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.FieldNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.Parameter
import org.codehaus.groovy.ast.PropertyNode
import org.codehaus.groovy.ast.expr.ClassExpression
import org.codehaus.groovy.ast.expr.ConstructorCallExpression
import org.codehaus.groovy.ast.expr.MethodCallExpression
import org.codehaus.groovy.ast.expr.PropertyExpression
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression
import org.codehaus.groovy.ast.expr.VariableExpression
import org.junit.jupiter.api.Test

final class IdentifiersTest {

    @Test
    void identifiersCountAndNameOf() {
        assert Identifiers.count(null, 'x') == 0
        assert Identifiers.count('foo', null) == 0
        assert Identifiers.count('foo', '') == 0
        assert Identifiers.containsWord('foo bar foo', 'foo')
        assert !Identifiers.containsWord('foobar', 'foo')
        assert Identifiers.count('foo foo', 'foo') == 2
        assert Identifiers.nameOf(null) == null
        assert Identifiers.nameOf(new ASTNode()) == null
        assert Identifiers.nameOf(new VariableExpression('x')) == 'x'
        assert Identifiers.nameOf(new Parameter(ClassHelper.STRING_TYPE, 'p')) == 'p'
        def method = new MethodNode('m', 0, ClassHelper.VOID_TYPE, new Parameter[0], ClassNode.EMPTY_ARRAY, null)
        assert Identifiers.nameOf(method) == 'm'
        def field = new FieldNode('f', 0, ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE, null)
        assert Identifiers.nameOf(field) == 'f'
        def property = new PropertyNode('q', 0, ClassHelper.STRING_TYPE, ClassHelper.OBJECT_TYPE, null, null, null)
        assert Identifiers.nameOf(property) == 'q'
        assert Identifiers.nameOf(new ClassNode('demo.Hello', 0, ClassHelper.OBJECT_TYPE)) == 'Hello'
        def call = new MethodCallExpression(new VariableExpression('this'), 'foo', MethodCallExpression.NO_ARGUMENTS)
        assert Identifiers.nameOf(call) == 'foo'
        def stat = new StaticMethodCallExpression(ClassHelper.OBJECT_TYPE, 'bar', MethodCallExpression.NO_ARGUMENTS)
        assert Identifiers.nameOf(stat) == 'bar'
        def prop = new PropertyExpression(new VariableExpression('this'), 'name')
        assert Identifiers.nameOf(prop) == 'name'
        assert Identifiers.nameOf(new ClassExpression(ClassHelper.STRING_TYPE)) == 'String'
        assert Identifiers.nameOf(new ConstructorCallExpression(ClassHelper.OBJECT_TYPE, MethodCallExpression.NO_ARGUMENTS)) == 'Object'
    }

    @Test
    void identifiersWordAroundEdges() {
        assert Identifiers.wordAround(null, 0) == ''
        assert Identifiers.wordAround('', 0) == ''
        assert Identifiers.wordAround('   ', 1) == ''
        assert Identifiers.wordAround('foo', 99) == 'foo'
        assert Identifiers.wordAround('foo', -1) == 'foo'
    }


    @Test
    void privateConstructorCanBeInvoked() {
        [Identifiers].each { Class type ->
            def ctor = type.getDeclaredConstructor()
            ctor.accessible = true
            ctor.newInstance()
        }
    }
}
