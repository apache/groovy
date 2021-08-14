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
package org.codehaus.groovy.transform.tailrec;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.StaticMethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.codehaus.groovy.runtime.DefaultGroovyMethods.transpose;

/**
 * Test if a method call is recursive if called within a given method node.
 * Handles static calls as well.
 * <p>
 * Currently known simplifications:
 * <ul>
 * <li>Does not check for method overloading or overridden methods</li>
 * <li>Does not check for matching return types; even void and any object type are considered to be compatible</li>
 * <li>Argument type matching could be more specific in case of static compilation</li>
 * <li>Method names via a GString are never considered to be recursive</li>
 * </ul>
 */
class RecursivenessTester {
    public boolean isRecursive(Map<String, ASTNode> params) {
        ASTNode method = params.get("method");
        assert MethodNode.class.equals(method.getClass());
        ASTNode call = params.get("call");
        Class<? extends ASTNode> callClass = call.getClass();
        assert MethodCallExpression.class.equals(callClass) || StaticMethodCallExpression.class.equals(callClass);

        if (callClass == MethodCallExpression.class) {
            return isRecursive((MethodNode) method, (MethodCallExpression) call);
        }
        return isRecursive((MethodNode) method, (StaticMethodCallExpression) call);
    }

    @SuppressWarnings("Instanceof")
    public boolean isRecursive(MethodNode method, MethodCallExpression call) {
        if (!isCallToThis(call)) return false;
        // Could be a GStringExpression
        if (!(call.getMethod() instanceof ConstantExpression)) return false;
        if (!((ConstantExpression) call.getMethod()).getValue().equals(method.getName())) return false;
        return methodParamsMatchCallArgs(method, call);
    }

    public boolean isRecursive(MethodNode method, StaticMethodCallExpression call) {
        if (!method.isStatic()) return false;
        if (!method.getDeclaringClass().equals(call.getOwnerType())) return false;
        if (!call.getMethod().equals(method.getName())) return false;
        return methodParamsMatchCallArgs(method, call);
    }

    @SuppressWarnings("Instanceof")
    private boolean isCallToThis(MethodCallExpression call) {
        if (call.getObjectExpression() == null) return call.isImplicitThis();
        if (!(call.getObjectExpression() instanceof VariableExpression)) {
            return false;
        }

        return ((boolean) (DefaultGroovyMethods.invokeMethod(call.getObjectExpression(), "isThisExpression", new Object[0])));
    }

    private boolean methodParamsMatchCallArgs(MethodNode method, Expression call) {
        TupleExpression arguments;
        if (call instanceof MethodCallExpression) {
            arguments = ((TupleExpression) ((MethodCallExpression) call).getArguments());
        } else {
            arguments = ((TupleExpression) ((StaticMethodCallExpression) call).getArguments());
        }

        if (method.getParameters().length != arguments.getExpressions().size())
            return false;

        List<List<ClassNode>> classNodePairs =
                transpose(Arrays.asList(
                        Arrays.stream(method.getParameters()).map(Parameter::getType).collect(Collectors.toList()),
                        arguments.getExpressions().stream().map(Expression::getType).collect(Collectors.toList())));
        return classNodePairs.stream().allMatch(t -> areTypesCallCompatible(t.get(0), t.get(1)));
    }

    /**
     * Parameter type and calling argument type can both be derived from the other since typing information is
     * optional in Groovy.
     * Since int is not derived from Integer (nor the other way around) we compare the boxed types
     */
    private Boolean areTypesCallCompatible(ClassNode argType, ClassNode paramType) {
        ClassNode boxedArg = ClassHelper.getWrapper(argType);
        ClassNode boxedParam = ClassHelper.getWrapper(paramType);
        return boxedArg.isDerivedFrom(boxedParam) || boxedParam.isDerivedFrom(boxedArg);
    }
}
