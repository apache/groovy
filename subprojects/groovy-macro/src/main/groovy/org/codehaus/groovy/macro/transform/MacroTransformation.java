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

import groovy.transform.CompilationUnitAware;
import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.asm.InvocationWriter;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.macro.runtime.MacroContext;
import org.codehaus.groovy.macro.runtime.MacroStub;
import org.codehaus.groovy.runtime.InvokerHelper;
import org.codehaus.groovy.transform.GroovyASTTransformation;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Sergei Egorov <bsideup@gmail.com>
 */

@GroovyASTTransformation(phase = CompilePhase.CONVERSION)
public class MacroTransformation extends MethodCallTransformation implements CompilationUnitAware {

    private static final ClassNode MACRO_CONTEXT_CLASS_NODE = ClassHelper.make(MacroContext.class);

    private static final ClassNode MACRO_STUB_CLASS_NODE = ClassHelper.make(MacroStub.class);

    private static final PropertyExpression MACRO_STUB_INSTANCE = new PropertyExpression(new ClassExpression(MACRO_STUB_CLASS_NODE), "INSTANCE");

    private static final String MACRO_STUB_METHOD_NAME = "macroMethod";

    protected CompilationUnit unit;

    @Override
    public void setCompilationUnit(CompilationUnit unit) {
        this.unit = unit;
    }

    @Override
    protected GroovyCodeVisitor getTransformer(ASTNode[] nodes, final SourceUnit sourceUnit) {
        // Macro methods should on a classpath of the compiler because we invoke them during the compilation
        final ClassLoader classLoader = this.getClass().getClassLoader();
        return new ClassCodeVisitorSupport() {

            @Override
            protected SourceUnit getSourceUnit() {
                return sourceUnit;
            }

            @Override
            public void visitMethodCallExpression(MethodCallExpression call) {
                super.visitMethodCallExpression(call);

                List<MethodNode> methods = MacroMethodsCache.get(classLoader).get(call.getMethodAsString());

                if (methods == null) {
                    // Not a macro call
                    return;
                }

                List<Expression> callArguments = InvocationWriter.makeArgumentList(call.getArguments()).getExpressions();

                ClassNode[] argumentsList = new ClassNode[callArguments.size()];

                for (int i = 0; i < callArguments.size(); i++) {
                    argumentsList[i] = ClassHelper.make(callArguments.get(i).getClass());
                }

                methods = StaticTypeCheckingSupport.chooseBestMethod(MACRO_CONTEXT_CLASS_NODE, methods, argumentsList);

                for (MethodNode macroMethodNode : methods) {
                    if (!(macroMethodNode instanceof ExtensionMethodNode)) {
                        // TODO is it even possible?
                        continue;
                    }

                    MethodNode macroExtensionMethodNode = ((ExtensionMethodNode) macroMethodNode).getExtensionMethodNode();

                    final Class clazz;
                    try {
                        clazz = classLoader.loadClass(macroExtensionMethodNode.getDeclaringClass().getName());
                    } catch (ClassNotFoundException e) {
                        //TODO different reaction?
                        continue;
                    }

                    MacroContext macroContext = new MacroContext(unit, sourceUnit, call);

                    List<Object> macroArguments = new ArrayList<>();
                    macroArguments.add(macroContext);
                    macroArguments.addAll(callArguments);

                    Expression result = (Expression) InvokerHelper.invokeStaticMethod(clazz, macroMethodNode.getName(), macroArguments.toArray());

                    call.setObjectExpression(MACRO_STUB_INSTANCE);
                    call.setMethod(new ConstantExpression(MACRO_STUB_METHOD_NAME));

                    // TODO check that we reset everything here
                    call.setSpreadSafe(false);
                    call.setSafe(false);
                    call.setImplicitThis(false);
                    call.setArguments(result);

                    break;
                }
            }
        };
    }
}
