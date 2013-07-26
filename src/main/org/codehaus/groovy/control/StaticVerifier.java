/*
 * Copyright 2003-2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.codehaus.groovy.control;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Verifier to check non-static access in static contexts
 *
 * @author Jochen Theodorou
 * @author Paul King
 * @author <a href="mailto:roshandawrani@codehaus.org">Roshan Dawrani</a>
 */
public class StaticVerifier extends ClassCodeVisitorSupport {
    private boolean inSpecialConstructorCall;
    private boolean inPropertyExpression;
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
        if (v != null && v instanceof DynamicVariable) {
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
        currentMethod = oldCurrentMethod;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression mce) {
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
        addError("Apparent variable '" + ve.getName() + "' was found in a static scope but doesn't refer" +
                " to a local variable, static field or class. Possible causes:\n" +
                "You attempted to reference a variable in the binding or an instance variable from a static context.\n" +
                "You misspelled a classname or statically imported field. Please check the spelling.\n" +
                "You attempted to use a method '" + ve.getName() +
                "' but left out brackets in a place not allowed by the grammar.", ve);
    }

    private FieldNode getDeclaredOrInheritedField(ClassNode cn, String fieldName) {
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
