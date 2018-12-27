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
package org.codehaus.groovy.macro.transform;

import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GenericsType;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClassExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.macro.runtime.MacroContext;
import org.codehaus.groovy.macro.runtime.MacroStub;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.Collections;
import java.util.List;

/**
 * Visitor to find and transform macro method calls. For the performance reasons it's not a transformer,
 * but transforming visitor - it mutates {@link MethodCallExpression} if it's a macro method call by replacing
 * original call (i.e. {@code myMacroMethod("foo", "bar")} with something like:
 * {@code MacroStub.INSTANCE.macroMethod(123)}
 * (where {@code myMacroMethod} returns constant expression {@code 123})
 *
 * @since 2.5.0
 */
class MacroCallTransformingVisitor extends ClassCodeVisitorSupport {

    private static final ClassNode MACRO_CONTEXT_CLASS_NODE = ClassHelper.make(MacroContext.class);

    private static final ClassNode MACRO_STUB_CLASS_NODE = ClassHelper.make(MacroStub.class);

    private static final PropertyExpression MACRO_STUB_INSTANCE = new PropertyExpression(new ClassExpression(MACRO_STUB_CLASS_NODE), "INSTANCE");

    private static final String MACRO_STUB_METHOD_NAME = "macroMethod";

    private final SourceUnit sourceUnit;
    private final CompilationUnit unit;
    private final ClassLoader classLoader;

    public MacroCallTransformingVisitor(SourceUnit sourceUnit, CompilationUnit unit) {
        this.sourceUnit = sourceUnit;
        this.unit = unit;
        this.classLoader = unit.getTransformLoader();
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitMethodCallExpression(MethodCallExpression call) {
        super.visitMethodCallExpression(call);

        final List<Expression> callArguments;
        if (call.getArguments() instanceof TupleExpression) {
            callArguments = ((TupleExpression) call.getArguments()).getExpressions();
        } else {
            callArguments = Collections.singletonList(call.getArguments());
        }

        List<MethodNode> macroMethods = findMacroMethods(call.getMethodAsString(), callArguments);

        if (macroMethods.isEmpty()) {
            // Early return to avoid macro context and arguments creation
            return;
        }

        MacroContext macroContext = new MacroContext(unit, sourceUnit, call);

        Object[] macroArguments = new Object[callArguments.size() + 1];
        macroArguments[0] = macroContext;
        System.arraycopy(callArguments.toArray(), 0, macroArguments, 1, callArguments.size());

        for (MethodNode macroMethodNode : macroMethods) {
            if (!(macroMethodNode instanceof ExtensionMethodNode)) {
                throw new IllegalStateException(macroMethodNode + " is not an instance of ExtensionMethodNode");
            }

            if (tryMacroMethod(call, (ExtensionMethodNode) macroMethodNode, macroArguments)) {
                break;
            }
        }
    }

    /**
     * Finds all extension methods of {@link MacroContext} for given methodName
     * with @{@link org.codehaus.groovy.macro.runtime.Macro} annotation.
     */
    private List<MethodNode> findMacroMethods(String methodName, List<Expression> callArguments) {
        List<MethodNode> methods = MacroMethodsCache.get(classLoader).get(methodName);

        if (methods == null) {
            // Not a macro call
            return Collections.emptyList();
        }

        ClassNode[] argumentsList = new ClassNode[callArguments.size()];

        for (int i = 0; i < callArguments.size(); i++) {
            argumentsList[i] = ClassHelper.make(callArguments.get(i).getClass());
        }

        return StaticTypeCheckingSupport.chooseBestMethod(MACRO_CONTEXT_CLASS_NODE, methods, argumentsList);
    }

    /**
     * Attempts to call given macroMethod
     * @param call MethodCallExpression before the transformation
     * @param macroMethod a macro method candidate
     * @param macroArguments arguments to pass to
     * @return true if call succeeded and current call was transformed
     */
    private boolean tryMacroMethod(MethodCallExpression call, ExtensionMethodNode macroMethod, Object[] macroArguments) {
        Expression result = (Expression) InvokerHelper.invokeStaticMethod(
            macroMethod.getExtensionMethodNode().getDeclaringClass().getTypeClass(),
            macroMethod.getName(),
            macroArguments
        );

        if (result == null) {
            // Allow macro methods to return null as an indicator that they didn't match a method call
            return false;
        }

        call.setObjectExpression(MACRO_STUB_INSTANCE);
        call.setMethod(new ConstantExpression(MACRO_STUB_METHOD_NAME));

        // TODO check that we reset everything here
        call.setSpreadSafe(false);
        call.setSafe(false);
        call.setImplicitThis(false);
        call.setArguments(result);
        call.setGenericsTypes(GenericsType.EMPTY_ARRAY);

        return true;
    }
}
