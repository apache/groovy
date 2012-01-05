/*
 * Copyright 2003-2009 the original author or authors.
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
package org.codehaus.groovy.transform.sc;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingVisitor;
import org.codehaus.groovy.transform.stc.TypeCheckerPluginFactory;

import java.util.Arrays;
import java.util.Iterator;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.BINARY_EXP_TARGET;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.STATIC_COMPILE_NODE;
import static org.codehaus.groovy.transform.sc.StaticCompileTransformation.COMPILE_STATIC_ANNOTATION;
import static org.codehaus.groovy.transform.stc.StaticTypesMarker.DIRECT_METHOD_CALL_TARGET;

/**
 * This visitor is responsible for amending the AST with static compilation metadata or transform the AST so that
 * a class or a method can be statically compiled. It may also throw errors specific to static compilation which
 * are not considered as an error at the type check pass. For example, usage of spread operator is not allowed
 * in statically compiled portions of code, while it may be statically checked.
 *
 * Static compilation relies on static type checking, which explains why this visitor extends the type checker
 * visitor.
 *
 * @author Cedric Champeau
 */
public class StaticCompilationVisitor extends StaticTypeCheckingVisitor {
    public StaticCompilationVisitor(final SourceUnit unit, final ClassNode node, final TypeCheckerPluginFactory pluginFactory) {
        super(unit, node, pluginFactory);
    }

    public static boolean isStaticallyCompiled(AnnotatedNode node) {
        if (node.getNodeMetaData(STATIC_COMPILE_NODE)!=null) return true;
        if (node instanceof MethodNode) {
            return isStaticallyCompiled(node.getDeclaringClass());
        }
        if (node instanceof InnerClassNode) {
            return isStaticallyCompiled(((InnerClassNode)node).getOuterClass());
        }
        return false;
    }

    @Override
    public void visitMethodCallExpression(final MethodCallExpression call) {
        super.visitMethodCallExpression(call);

        MethodNode target = (MethodNode) call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target!=null) call.setMethodTarget(target);

        if (call.getMethodTarget()==null && call.getLineNumber()>0) {
            addError("Target method for method call expression hasn't been set", call);
        }
    }

    @Override
    public void visitConstructorCallExpression(final ConstructorCallExpression call) {
        super.visitConstructorCallExpression(call);

        MethodNode target = (MethodNode) call.getNodeMetaData(DIRECT_METHOD_CALL_TARGET);
        if (target==null && call.getLineNumber()>0) {
            addError("Target constructor for constructor call expression hasn't been set", call);
        }
    }

    @Override
    protected MethodNode findMethodOrFail(final Expression expr, final ClassNode receiver, final String name, final ClassNode... args) {
        MethodNode methodNode = super.findMethodOrFail(expr, receiver, name, args);
        if (expr instanceof BinaryExpression && methodNode!=null) {
            expr.putNodeMetaData(BINARY_EXP_TARGET, new Object[] {methodNode, name});
        }
        return methodNode;
    }
}
