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

import org.apache.groovy.ast.tools.ClassNodeUtils;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.transform.trait.Traits;

import java.util.List;

/**
 * Checks for dynamic variables in static contexts.
 */
public class StaticVerifier extends ClassCodeVisitorSupport {
    private boolean inClosure, inSpecialConstructorCall;
    private MethodNode methodNode;
    private SourceUnit sourceUnit;

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    public void visitClass(ClassNode node, SourceUnit unit) {
        sourceUnit = unit;
        visitClass(node);
    }

    @Override
    public void visitClosureExpression(ClosureExpression ce) {
        boolean oldInClosure = inClosure;
        inClosure = true;
        super.visitClosureExpression(ce);
        inClosure = oldInClosure;
    }

    @Override
    public void visitConstructorCallExpression(ConstructorCallExpression cce) {
        boolean oldIsSpecialConstructorCall = inSpecialConstructorCall;
        inSpecialConstructorCall |= cce.isSpecialCall();
        super.visitConstructorCallExpression(cce);
        inSpecialConstructorCall = oldIsSpecialConstructorCall;
    }

    @Override
    public void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        MethodNode oldMethodNode = methodNode;
        methodNode = node;
        super.visitConstructorOrMethod(node, isConstructor);
        if (isConstructor) {
            for (Parameter param : node.getParameters()) {
                if (param.hasInitialExpression()) {
                    // initial expression will be argument to special constructor call
                    boolean oldIsSpecialConstructorCall = inSpecialConstructorCall;
                    inSpecialConstructorCall = true;
                    param.getInitialExpression().visit(this);
                    inSpecialConstructorCall = oldIsSpecialConstructorCall;
                }
            }
        }
        methodNode = oldMethodNode;
    }

    @Override
    public void visitVariableExpression(VariableExpression ve) {
        if (ve.getAccessedVariable() instanceof DynamicVariable && (ve.isInStaticContext() || inSpecialConstructorCall) && !inClosure) {
            String variableName = ve.getName();
            // GROOVY-5687: interface constants not visible to implementing subclass in static context
            if (methodNode != null && methodNode.isStatic()) {
                ClassNode classNode = methodNode.getDeclaringClass();
                FieldNode fieldNode = getDeclaredOrInheritedField(classNode, variableName);
                if (fieldNode != null && fieldNode.isStatic()) {
                    return;
                }
            }
            addError("Apparent variable '" + variableName + "' was found in a static scope but doesn't refer to a local variable, static field or class. Possible causes:\n" +
                    "You attempted to reference a variable in the binding or an instance variable from a static context.\n" +
                    "You misspelled a classname or statically imported field. Please check the spelling.\n" +
                    "You attempted to use a method '" + variableName + "' but left out brackets in a place not allowed by the grammar.",
                    ve);
        }
    }

    private static FieldNode getDeclaredOrInheritedField(ClassNode classNode, String fieldName) {
        FieldNode fieldNode = ClassNodeUtils.getField(classNode, fieldName);
        if (fieldNode == null && fieldName.contains("__")) { // GROOVY-11663
            List<ClassNode> traits = Traits.findTraits(classNode);
            traits.remove(classNode); // included if it is a trait
            for (ClassNode cn : traits) {
                cn = Traits.findFieldHelper(cn);
                if (cn != null) {
                    for (FieldNode fn : cn.getFields()) {
                        if (fn.getName().endsWith(fieldName)) { // prefix for modifiers
                            fieldNode = fn;
                            break;
                        }
                    }
                }
            }
        }
        return fieldNode;
    }
}
