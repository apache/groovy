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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;

public class ExpressionUtils {
    private ExpressionUtils() {

    }

    // resolve constant-looking expressions statically (do here as gets transformed away later)
    public static Expression transformInlineConstants(final Expression exp) {
        if (exp instanceof PropertyExpression) {
            PropertyExpression pe = (PropertyExpression) exp;
            if (pe.getObjectExpression() instanceof ClassExpression) {
                ClassExpression ce = (ClassExpression) pe.getObjectExpression();
                ClassNode type = ce.getType();
                if (type.isEnum()) return exp;
                Expression constant = findConstant(ClassNodeUtils.getField(type, pe.getPropertyAsString()));
                if (constant != null) return constant;
            }
        } else if (exp instanceof ListExpression) {
            ListExpression origList = (ListExpression) exp;
            ListExpression newList = new ListExpression();
            boolean changed = false;
            for (Expression e : origList.getExpressions()) {
                Expression transformed = transformInlineConstants(e);
                newList.addExpression(transformed);
                if (transformed != e) changed = true;
            }
            if (changed) {
                newList.setSourcePosition(origList);
                return newList;
            }
            return origList;
        }

        return exp;
    }

    private static Expression findConstant(FieldNode fn) {
        if (fn != null && !fn.isEnum() && fn.isStatic() && fn.isFinal()) {
            if (fn.getInitialValueExpression() instanceof ConstantExpression) {
                return fn.getInitialValueExpression();
            }
        }
        return null;
    }

}
