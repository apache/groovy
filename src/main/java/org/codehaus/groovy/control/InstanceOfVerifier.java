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

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.syntax.Types;

public abstract class InstanceOfVerifier extends ClassCodeVisitorSupport {

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        if (expression.getOperation().isA(Types.INSTANCEOF_OPERATOR) &&
                expression.getRightExpression() instanceof ClassExpression) {
            ClassNode referenceType = expression.getRightExpression().getType();

            if (ClassHelper.isPrimitiveType(referenceType)) {
                addTypeError(expression.getRightExpression(), "primitive type " + referenceType.getName());
            } else {
                while (referenceType.isArray()) {
                    referenceType = referenceType.getComponentType();
                }

                if (referenceType.isGenericsPlaceHolder()) {
                    addTypeError(expression.getRightExpression(), "type parameter " + referenceType.getUnresolvedName() +
                        ". Use its erasure " + referenceType.getNameWithoutPackage() + " instead since further generic type information will be erased at runtime");
                } else if (referenceType.getGenericsTypes() != null) {
                    // TODO: Cannot perform instanceof check against parameterized type Class<Type>. Use the form Class<?> instead since further eneric type information will be erased at runtime
                }
            }
        }
        super.visitBinaryExpression(expression);
    }

    private void addTypeError(Expression referenceExpr, String referenceType) {
        addError("Cannot perform instanceof check against " + referenceType, referenceExpr);
    }
}
