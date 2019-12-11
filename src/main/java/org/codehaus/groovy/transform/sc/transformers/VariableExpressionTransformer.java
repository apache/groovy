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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;


/**
 * Transformer for VariableExpression the bytecode backend wouldn't be able to
 * handle otherwise.
 */
public class VariableExpressionTransformer {

    public Expression transformVariableExpression(VariableExpression expr) {
        Expression trn = tryTransformDelegateToProperty(expr);
        if (trn != null) {
            return trn;
        }
        trn = tryTransformPrivateFieldAccess(expr);
        if (trn != null) {
            return trn;
        }
        return expr;
    }

    private static Expression tryTransformDelegateToProperty(VariableExpression expr) {
        // we need to transform variable expressions that go to a delegate
        // to a property expression, as ACG would lose the information in
        // processClassVariable before it reaches any makeCall, that could handle it
        Object val = expr.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        if (val == null) return null;

        // TODO handle the owner and delegate cases better for nested scenarios and potentially remove the need for the implicit this case
        VariableExpression receiver = new VariableExpression("owner".equals(val) ? (String) val : "delegate".equals(val) ? (String) val : "this");
        // GROOVY-9136 -- object expression should not overlap source range of property; property stands in for original varibale expression
        receiver.setLineNumber(expr.getLineNumber());
        receiver.setColumnNumber(expr.getColumnNumber());

        PropertyExpression pexp = new PropertyExpression(receiver, expr.getName());
        pexp.getProperty().setSourcePosition(expr);
        pexp.copyNodeMetaData(expr);
        pexp.setImplicitThis(true);

        ClassNode owner = expr.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
        if (owner != null) {
            receiver.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, owner);
            receiver.putNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER, val);
        }

        return pexp;
    }

    private static Expression tryTransformPrivateFieldAccess(VariableExpression expr) {
        FieldNode field = expr.getNodeMetaData(StaticTypesMarker.PV_FIELDS_ACCESS);
        FieldNode mutationField = expr.getNodeMetaData(StaticTypesMarker.PV_FIELDS_MUTATION);
        if (field == null) {
            field = mutationField;
        }
        if (field != null) {
            ClassNode declaringClass = field.getDeclaringClass();
            ClassNode originType = field.getOriginType();

            // GROOVY-9332: Error occurred when accessing static field in lambda within static initialization block
            if (field.isStatic()) {
                ClassExpression receiver = new ClassExpression(declaringClass);
                PropertyExpression pexp = new PropertyExpression(receiver, expr.getName());
                pexp.setSourcePosition(expr);
                pexp.getProperty().setSourcePosition(expr);

                receiver.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, declaringClass);
                receiver.putNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER, declaringClass);
                pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, originType);
                pexp.putNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE, originType);
                pexp.putNodeMetaData(
                        null == mutationField
                                ? StaticTypesMarker.PV_FIELDS_ACCESS
                                : StaticTypesMarker.PV_FIELDS_MUTATION,
                        field);

                return pexp;
            }

            // access to a private field from a section of code that normally doesn't have access to it, like a
            // closure or an inner class
            VariableExpression receiver = new VariableExpression("this");
            PropertyExpression pexp = new PropertyExpression(
                    receiver,
                    expr.getName()
            );
            pexp.setImplicitThis(true);
            pexp.getProperty().setSourcePosition(expr);
            // put the receiver inferred type so that the class writer knows that it will have to call a bridge method
            receiver.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, declaringClass);
            // add inferred type information
            pexp.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, originType);
            return pexp;
        }
        return null;
    }
}
