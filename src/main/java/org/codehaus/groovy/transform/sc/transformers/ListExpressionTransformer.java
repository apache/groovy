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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ArrayExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.ListExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.transform.stc.StaticTypesMarker;

import java.util.ArrayList;
import java.util.List;

import static org.codehaus.groovy.classgen.AsmClassGenerator.containsSpreadExpression;
import static org.objectweb.asm.Opcodes.CHECKCAST;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;

class ListExpressionTransformer {

    private final StaticCompilationTransformer scTransformer;

    ListExpressionTransformer(final StaticCompilationTransformer scTransformer) {
        this.scTransformer = scTransformer;
    }

    Expression transformListExpression(final ListExpression le) {
        MethodNode mn = le.getNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET);
        if (mn instanceof ConstructorNode) {
            List<Expression> elements = le.getExpressions().stream().map(scTransformer::transform).toList();

            if (mn.getDeclaringClass().isArray()) {
                var ae = new ArrayExpression(mn.getDeclaringClass().getComponentType(), elements);
                ae.setSourcePosition(le);
                return ae;
            }

            var cce = new ConstructorCallExpression(mn.getDeclaringClass(), new ArgumentListExpression(elements));
            cce.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, mn);
            cce.setSourcePosition(le);
            return cce;
        }

        // GROOVY-8699: emit direct bytecode for simple list
        if (mn == null && le.getExpressions().size() < 25 && !containsSpreadExpression(le)) {
            ClassNode type = scTransformer.getTypeChooser().resolveType(le, scTransformer.getClassNode());
            if (ArrayList_TYPE.equals(type)) { // annotation attributes cannot be transformed
                var list = new NewListExpression(le.getExpressions().stream().map(scTransformer::transform).toList());
                list.setSourcePosition(le);
                list.copyNodeMetaData(le);
                return list;
            }
        }

        return scTransformer.superTransform(le);
    }

    //--------------------------------------------------------------------------

    private static final ClassNode ArrayList_TYPE = ClassHelper.makeWithoutCaching(ArrayList.class);

    private static final MethodNode ArrayList_NEW = ArrayList_TYPE.getDeclaredConstructor(new Parameter[] {new Parameter(ClassHelper.int_TYPE, "capacity")});

    private static class NewListExpression extends ListExpression {

        NewListExpression(final List<Expression> values) {
            super(values);
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            var list = new NewListExpression(transformExpressions(getExpressions(), transformer));
            list.setSourcePosition(this);
            list.copyNodeMetaData(this);
            return list;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (!(visitor instanceof AsmClassGenerator g)) {
                super.visit(visitor);
            } else {
                var mv = g.getController().getMethodVisitor();
                var os = g.getController().getOperandStack ();

                var list = new ConstructorCallExpression(ArrayList_TYPE, new ConstantExpression(getExpressions().size(), true));
                list.putNodeMetaData(StaticTypesMarker.DIRECT_METHOD_CALL_TARGET, ArrayList_NEW);
                list.visit(visitor);
                // GROOVY-11967: when the constructor goes through a dynamic call
                // site (indy or non-indy), the call leaves Object on the JVM stack
                // and the following INVOKEVIRTUAL ArrayList.add fails verification
                // unless preceded by CHECKCAST. The direct INVOKESPECIAL path of
                // StaticInvocationWriter already leaves ArrayList on the stack, so
                // there the cast is unnecessary.
                if (!ArrayList_TYPE.equals(os.getTopOperand())) {
                    mv.visitTypeInsn(CHECKCAST, "java/util/ArrayList");
                    os.replace(ArrayList_TYPE);
                }

                for (Expression li : getExpressions()) {
                    mv.visitInsn(DUP);
                    li.visit(visitor);
                    os.box();
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/util/ArrayList", "add", "(Ljava/lang/Object;)Z", false);
                    os.pop(); // boolean return value
                }
            }
        }
    }
}
