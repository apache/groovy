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
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.DynamicVariable;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.Variable;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.apache.groovy.ast.tools.ClassNodeUtils.isInnerClass;

/**
 * Verifier to check non-static access in static contexts
 */
public class StaticVerifier extends ClassCodeVisitorSupport {
    private boolean inSpecialConstructorCall;
    private boolean inPropertyExpression; // TODO use it or lose it
    private boolean inClosure;
    private MethodNode currentMethod;
    private SourceUnit source;

    public void visitClass(ClassNode node, SourceUnit source) {
        this.source = source;
        super.visitClass(node);
    }

    @Override
    public void visitVariableExpression(VariableExpression ve) {
        Variable v = ve.getAccessedVariable();
        if (v instanceof DynamicVariable) {
            if (!inPropertyExpression || inSpecialConstructorCall) addStaticVariableError(ve);
        }
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
        inSpecialConstructorCall = cce.isSpecialCall();
        super.visitConstructorCallExpression(cce);
        inSpecialConstructorCall = oldIsSpecialConstructorCall;
    }

    @Override
    public void visitConstructorOrMethod(MethodNode node, boolean isConstructor) {
        MethodNode oldCurrentMethod = currentMethod;
        currentMethod = node;
        super.visitConstructorOrMethod(node, isConstructor);
        if (isConstructor) {
            final Set<String> exceptions = new HashSet<String>();
            for (final Parameter param : node.getParameters()) {
                exceptions.add(param.getName());
                if (param.hasInitialExpression()) {
                    param.getInitialExpression().accept(new CodeVisitorSupport() {
                        @Override
                        public void visitVariableExpression(VariableExpression ve) {
                            if (exceptions.contains(ve.getName())) return;
                            if (ve.getAccessedVariable() instanceof DynamicVariable || !ve.isInStaticContext()) {
                                addVariableError(ve);
                            }
                        }

                        @Override
                        public void visitMethodCallExpression(MethodCallExpression call) {
                            Expression objectExpression = call.getObjectExpression();
                            if (objectExpression instanceof VariableExpression) {
                                VariableExpression ve = (VariableExpression) objectExpression;
                                if (ve.isThisExpression()) {
                                    addError("Can't access instance method '" + call.getMethodAsString() + "' for a constructor parameter default value", param);
                                    return;
                                }
                            }
                            super.visitMethodCallExpression(call);
                        }

                        @Override
                        public void visitClosureExpression(ClosureExpression expression) {
                            //skip contents, because of dynamic scope
                        }
                    });
                }
            }
        }
        currentMethod = oldCurrentMethod;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression mce) {
        if (inSpecialConstructorCall && !isInnerClass(currentMethod.getDeclaringClass())) {
            Expression objectExpression = mce.getObjectExpression();
            if (objectExpression instanceof VariableExpression) {
                VariableExpression ve = (VariableExpression) objectExpression;
                if (ve.isThisExpression()) {
                    addError("Can't access instance method '" + mce.getMethodAsString() + "' before the class is constructed", mce);
                    return;
                }
            }
        }
        super.visitMethodCallExpression(mce);
    }

    @Override
    public void visitPropertyExpression(PropertyExpression pe) {
        if (!inSpecialConstructorCall) checkStaticScope(pe);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return source;
    }


    private void checkStaticScope(PropertyExpression pe) {
        if (inClosure) return;
        for (Expression it = pe; it != null; it = ((PropertyExpression) it).getObjectExpression()) {
            if (it instanceof PropertyExpression) continue;
            if (it instanceof VariableExpression) {
                addStaticVariableError((VariableExpression) it);
            }
            return;
        }
    }

    private void addStaticVariableError(VariableExpression ve) {
        // closures are always dynamic
        // propertyExpressions will handle the error a bit differently
        if (!inSpecialConstructorCall && (inClosure || !ve.isInStaticContext())) return;
        if (ve.isThisExpression() || ve.isSuperExpression()) return;
        Variable v = ve.getAccessedVariable();
        if (currentMethod != null && currentMethod.isStatic()) {
            FieldNode fieldNode = getDeclaredOrInheritedField(currentMethod.getDeclaringClass(), ve.getName());
            if (fieldNode != null && fieldNode.isStatic()) return;
        }
        if (v != null && !(v instanceof DynamicVariable) && v.isInStaticContext()) return;
        addVariableError(ve);
    }

    private void addVariableError(VariableExpression ve) {
        addError("Apparent variable '" + ve.getName() + "' was found in a static scope but doesn't refer" +
                " to a local variable, static field or class. Possible causes:\n" +
                "You attempted to reference a variable in the binding or an instance variable from a static context.\n" +
                "You misspelled a classname or statically imported field. Please check the spelling.\n" +
                "You attempted to use a method '" + ve.getName() +
                "' but left out brackets in a place not allowed by the grammar.", ve);
    }

    private static FieldNode getDeclaredOrInheritedField(ClassNode cn, String fieldName) {
        ClassNode node = cn;
        while (node != null) {
            FieldNode fn = node.getDeclaredField(fieldName);
            if (fn != null) return fn;
            List<ClassNode> interfacesToCheck = new ArrayList<ClassNode>(Arrays.asList(node.getInterfaces()));
            while (!interfacesToCheck.isEmpty()) {
                ClassNode nextInterface = interfacesToCheck.remove(0);
                fn = nextInterface.getDeclaredField(fieldName);
                if (fn != null) return fn;
                interfacesToCheck.addAll(Arrays.asList(nextInterface.getInterfaces()));
            }
            node = node.getSuperClass();
        }
        return null;
    }

}
