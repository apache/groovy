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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import static org.codehaus.groovy.ast.tools.GeneralUtils.callThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.classX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.propX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.thisPropX;

/**
 * Transformer for VariableExpression the bytecode backend wouldn't be able to
 * handle otherwise.
 */
public class VariableExpressionTransformer {

    public Expression transformVariableExpression(final VariableExpression ve) {
        Expression xe = tryTransformImplicitReceiver(ve);
        if (xe == null) {
            xe = tryTransformPrivateFieldAccess(ve);
        }
        if (xe == null) {
            xe = tryTransformDirectMethodTarget(ve);
        }
        if (xe != null) {
            return xe;
        }
        return ve;
    }

    private static Expression tryTransformImplicitReceiver(final VariableExpression ve) {
        // we need to transform variable expressions that go to a delegate
        // to a property expression, as ACG would lose the information in
        // processClassVariable before it reaches any makeCall, that could handle it
        Object val = ve.getNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);
        if (val == null || val.equals(ve.getName())) return null;

        // TODO: handle the owner and delegate cases better for nested scenarios and potentially remove the need for the implicit this case
        Expression receiver = new VariableExpression("owner".equals(val) ? (String) val : "delegate".equals(val) ? (String) val : "this");
        // GROOVY-9136: object expression should not overlap source range of property; property stands in for original variable expression
        receiver.setColumnNumber(ve.getColumnNumber());
        receiver.setLineNumber(ve.getLineNumber());

        PropertyExpression pe = propX(receiver, ve.getName());
        pe.getProperty().setSourcePosition(ve);
        pe.setImplicitThis(true);
        pe.copyNodeMetaData(ve);

        ClassNode owner = ve.getNodeMetaData(StaticCompilationMetadataKeys.PROPERTY_OWNER);
        if (owner != null) {
            receiver.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, owner);
            receiver.putNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER, val);
        }
        pe.removeNodeMetaData(StaticTypesMarker.IMPLICIT_RECEIVER);

        return pe;
    }

    private static Expression tryTransformPrivateFieldAccess(final VariableExpression ve) {
        FieldNode field = ve.getNodeMetaData(StaticTypesMarker.PV_FIELDS_ACCESS);
        if (field == null) {
            field = ve.getNodeMetaData(StaticTypesMarker.PV_FIELDS_MUTATION);
        }
        if (field == null) {
            return null;
        }

        // access to a private field from a section of code that normally doesn't have access to it, like a closure block or an inner class
        PropertyExpression pe = !field.isStatic() ? thisPropX(true, ve.getName()) : propX(classX(field.getDeclaringClass()), ve.getName());
        // store the declaring class so that the class writer knows that it will have to call a bridge method
        pe.getObjectExpression().putNodeMetaData(StaticTypesMarker.INFERRED_TYPE, field.getDeclaringClass());
        pe.putNodeMetaData(StaticTypesMarker.DECLARATION_INFERRED_TYPE, field.getOriginType());
        pe.getProperty().setSourcePosition(ve);
        return pe;
    }

    private static Expression tryTransformDirectMethodTarget(final VariableExpression ve) {
        MethodNode dmct = ve.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        // NOTE: BinaryExpressionTransformer handles the setter
        if (dmct == null || dmct.getParameters().length != 0) {
            return null;
        }

        MethodCallExpression mce = callThisX(dmct.getName());
        mce.getMethod().setSourcePosition(ve);
        mce.setMethodTarget(dmct);
        // GROOVY-10637: return type might be parameterized
        mce.putNodeMetaData(StaticTypesMarker.INFERRED_TYPE,
         ve.getNodeMetaData(StaticTypesMarker.INFERRED_TYPE));
        return mce;
    }
}
