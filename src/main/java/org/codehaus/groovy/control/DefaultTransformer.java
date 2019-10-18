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
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.SWITCH_TYPE;

/**
 * The default transformer a.k.a. the last transformer to transform expressions
 *
 * @since 3.0.0
 */
public class DefaultTransformer extends ClassCodeExpressionTransformer {
    private ClassNode currentClass;
    private SourceUnit source;

    public void visitClass(ClassNode node, SourceUnit source) {
        this.currentClass = node;
        this.source = source;
        super.visitClass(node);
    }

    public Expression transform(Expression exp) {
        if (null == exp) return null;

        if (exp.getClass() == VariableExpression.class) {
            return transformVariableExpression((VariableExpression) exp);
        }
        return exp;
    }

    private Expression transformVariableExpression(VariableExpression ve) {
        ClassNode enumClassNode = ve.getNodeMetaData(SWITCH_TYPE);
        if (null != enumClassNode) {
            Expression result = new PropertyExpression(new ClassExpression(enumClassNode), ve.getName());
            setSourcePosition(result, ve);

            return result;
        }
        return ve;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }
}
