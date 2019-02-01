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
package org.codehaus.groovy.classgen.asm.sc;

import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ClassCodeVisitorSupport;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.classgen.asm.ClosureWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.control.SourceUnit;
import org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;
import org.objectweb.asm.Opcodes;

import java.util.List;

/**
 * Writer responsible for generating closure classes in statically compiled mode.
 */
public class StaticTypesClosureWriter extends ClosureWriter {
    public StaticTypesClosureWriter(WriterController wc) {
        super(wc);
    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int mods) {
        ClassNode closureClass = super.createClosureClass(expression, mods);
        List<MethodNode> methods = closureClass.getDeclaredMethods("call");
        List<MethodNode> doCall = closureClass.getMethods("doCall");
        if (doCall.size() != 1) {
            throw new GroovyBugError("Expected to find one (1) doCall method on generated closure, but found " + doCall.size());
        }
        MethodNode doCallMethod = doCall.get(0);
        if (methods.isEmpty() && doCallMethod.getParameters().length == 1) {
            createDirectCallMethod(closureClass, doCallMethod);
        }
        MethodTargetCompletionVisitor visitor = new MethodTargetCompletionVisitor(doCallMethod);
        Object dynamic = expression.getNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION);
        if (dynamic != null) {
            doCallMethod.putNodeMetaData(StaticTypesMarker.DYNAMIC_RESOLUTION, dynamic);
        }
        for (MethodNode method : methods) {
            visitor.visitMethod(method);
        }
        closureClass.putNodeMetaData(StaticCompilationMetadataKeys.STATIC_COMPILE_NODE, Boolean.TRUE);
        return closureClass;
    }

    private static void createDirectCallMethod(final ClassNode closureClass, final MethodNode doCallMethod) {
        // in case there is no "call" method on the closure, we can create a "fast invocation" paths
        // to avoid going through ClosureMetaClass by call(Object...) method

        // we can't have a specialized version of call(Object...) because the dispatch logic in ClosureMetaClass
        // is too complex!

        // call(Object)
        Parameter args = new Parameter(ClassHelper.OBJECT_TYPE, "args");
        MethodCallExpression doCall1arg = new MethodCallExpression(
                new VariableExpression("this", closureClass),
                "doCall",
                new ArgumentListExpression(new VariableExpression(args))
        );
        doCall1arg.setImplicitThis(true);
        doCall1arg.setMethodTarget(doCallMethod);
        closureClass.addMethod(
                new MethodNode("call",
                        Opcodes.ACC_PUBLIC,
                        ClassHelper.OBJECT_TYPE,
                        new Parameter[]{args},
                        ClassNode.EMPTY_ARRAY,
                        new ReturnStatement(doCall1arg)));

        // call()
        MethodCallExpression doCallNoArgs = new MethodCallExpression(new VariableExpression("this", closureClass), "doCall", new ArgumentListExpression(new ConstantExpression(null)));
        doCallNoArgs.setImplicitThis(true);
        doCallNoArgs.setMethodTarget(doCallMethod);
        closureClass.addMethod(
                new MethodNode("call",
                        Opcodes.ACC_PUBLIC,
                        ClassHelper.OBJECT_TYPE,
                        Parameter.EMPTY_ARRAY,
                        ClassNode.EMPTY_ARRAY,
                        new ReturnStatement(doCallNoArgs)));
    }

    private static final class MethodTargetCompletionVisitor extends ClassCodeVisitorSupport {

        private final MethodNode doCallMethod;

        private MethodTargetCompletionVisitor(final MethodNode doCallMethod) {
            this.doCallMethod = doCallMethod;
        }

        @Override
        protected SourceUnit getSourceUnit() {
            return null;
        }

        @Override
        public void visitMethodCallExpression(final MethodCallExpression call) {
            super.visitMethodCallExpression(call);
            MethodNode mn = call.getMethodTarget();
            if (mn == null) {
                call.setMethodTarget(doCallMethod);
            }
        }
    }
}
