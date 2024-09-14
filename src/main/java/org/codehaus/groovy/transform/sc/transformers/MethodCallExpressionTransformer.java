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
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.EmptyStatement;
import org.codehaus.groovy.classgen.asm.MopWriter;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import static org.codehaus.groovy.classgen.AsmClassGenerator.argumentSize;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

class MethodCallExpressionTransformer {

    private final StaticCompilationTransformer scTransformer;

    MethodCallExpressionTransformer(final StaticCompilationTransformer scTransformer) {
        this.scTransformer = scTransformer;
    }

    Expression transformMethodCallExpression(final MethodCallExpression mce) {
        Expression arguments = mce.getArguments();

        // replace call to DefaultGroovyMethods#is(Object,Object) with a CompareIdentityExpression
        if (!mce.isSafe() && !mce.isSpreadSafe() && isIsExtension(mce.getMethodTarget()) && argumentSize(arguments) == 1) {
            Expression lhs = scTransformer.transform(mce.getObjectExpression());
            Expression rhs = scTransformer.transform(arguments instanceof TupleExpression ? ((TupleExpression) arguments).getExpression(0) : arguments);
            Expression cmp = new CompareIdentityExpression(lhs, rhs);
            cmp.setSourcePosition(mce);
            return cmp;
        }

        var superCallReceiver = mce.getNodeMetaData(StaticTypesMarker.SUPER_MOP_METHOD_REQUIRED);
        if (superCallReceiver instanceof ClassNode) {
            return transformMethodCallExpression(transformToMopSuperCall((ClassNode) superCallReceiver, mce));
        }

        var callable = (Expression) mce.putNodeMetaData("callable property", null);
        if (callable != null) {
            var callableCall = new MethodCallExpression(scTransformer.transform(callable), "call", scTransformer.transform(arguments));
            // "callable(args)" expression has no place for safe, spread-safe or type arguments
            callableCall.setImplicitThis(false);
            callableCall.setMethodTarget(mce.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET));
            callableCall.setSourcePosition(mce);
            callableCall.copyNodeMetaData(mce);
            return callableCall;
        }

        return scTransformer.superTransform(mce);
    }

    //--------------------------------------------------------------------------

    private static boolean isIsExtension(final MethodNode node) {
        return node instanceof ExtensionMethodNode // guards null
                && "is".equals(node.getName())
                && node.getParameters().length == 1
                && DefaultGroovyMethods.class.getName().equals(
                    ((ExtensionMethodNode) node).getExtensionMethodNode().getDeclaringClass().getName());
    }

    private static MethodCallExpression transformToMopSuperCall(final ClassNode superType, final MethodCallExpression expr) {
        MethodNode mn = expr.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        String mopName = MopWriter.getMopMethodName(mn, false);
        MethodNode direct = new MethodNode(
                mopName,
                ACC_PUBLIC | ACC_SYNTHETIC,
                mn.getReturnType(),
                mn.getParameters(),
                mn.getExceptions(),
                EmptyStatement.INSTANCE
        );
        direct.setDeclaringClass(superType);

        MethodCallExpression result = new MethodCallExpression(
                new VariableExpression("this"),
                mopName,
                expr.getArguments()
        );
        result.setImplicitThis(true);
        result.setSpreadSafe(false);
        result.setSafe(false);
        result.setSourcePosition(expr);
        result.setMethodTarget(direct);
        return result;
    }
}
