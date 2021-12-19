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
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.ast.expr.NotExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.OperandStack;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.objectweb.asm.Opcodes.DUP;
import static org.objectweb.asm.Opcodes.GOTO;
import static org.objectweb.asm.Opcodes.ICONST_0;
import static org.objectweb.asm.Opcodes.IFNONNULL;
import static org.objectweb.asm.Opcodes.INVOKEVIRTUAL;
import static org.objectweb.asm.Opcodes.POP;

class BooleanExpressionTransformer {

    private final StaticCompilationTransformer scTransformer;

    BooleanExpressionTransformer(final StaticCompilationTransformer scTransformer) {
        this.scTransformer = scTransformer;
    }

    Expression transformBooleanExpression(final BooleanExpression be) {
        if (!(be instanceof NotExpression || be.getExpression() instanceof BinaryExpression)) {
            ClassNode type = scTransformer.getTypeChooser().resolveType(be.getExpression(), scTransformer.getClassNode());
            return optimizeBooleanExpression(be, type, scTransformer);
        }
        return scTransformer.superTransform(be);
    }

    private static Expression optimizeBooleanExpression(final BooleanExpression be, final ClassNode targetType, final ExpressionTransformer transformer) {
        Expression opt = new OptimizingBooleanExpression(transformer.transform(be.getExpression()), targetType);
        opt.setSourcePosition(be);
        opt.copyNodeMetaData(be);
        return opt;
    }

    //--------------------------------------------------------------------------

    private static class OptimizingBooleanExpression extends BooleanExpression {

        private final ClassNode type;

        OptimizingBooleanExpression(final Expression expression, final ClassNode type) {
            super(expression);
            // we must use the redirect node, otherwise InnerClassNode would not have the "correct" type
            this.type = type.redirect();
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            return optimizeBooleanExpression(this, type, transformer);
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                WriterController controller = ((AsmClassGenerator) visitor).getController();
                MethodVisitor mv = controller.getMethodVisitor();
                OperandStack os = controller.getOperandStack();

                if (ClassHelper.isPrimitiveBoolean(type)) {
                    getExpression().visit(visitor);
                    os.doGroovyCast(ClassHelper.boolean_TYPE);
                    return;
                }

                if (ClassHelper.isWrapperBoolean(type)) {
                    getExpression().visit(visitor);
                    Label unbox = new Label();
                    Label exit = new Label();
                    // check for null
                    mv.visitInsn(DUP);
                    mv.visitJumpInsn(IFNONNULL, unbox);
                    mv.visitInsn(POP);
                    mv.visitInsn(ICONST_0);
                    mv.visitJumpInsn(GOTO, exit);
                    mv.visitLabel(unbox);
                    if (!os.getTopOperand().equals(type)) BytecodeHelper.doCast(mv, type); // GROOVY-6270
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z", false);
                    mv.visitLabel(exit);
                    os.replace(ClassHelper.boolean_TYPE);
                    return;
                }

                ClassNode top = type;
                if (ClassHelper.isPrimitiveType(top)) {
                    getExpression().visit(visitor);
                    // in case of null-safe invocation, it is possible that what
                    // was supposed to be a primitive type becomes "null" value,
                    // so we need to recheck
                    top = os.getTopOperand();
                    if (ClassHelper.isPrimitiveType(top)) {
                        BytecodeHelper.convertPrimitiveToBoolean(mv, top);
                        os.replace(ClassHelper.boolean_TYPE);
                        return;
                    }
                }

                List<MethodNode> asBoolean = findDGMMethodsByNameAndArguments(controller.getSourceUnit().getClassLoader(), top, "asBoolean", ClassNode.EMPTY_ARRAY);
                if (asBoolean.size() == 1) {
                    MethodNode method = asBoolean.get(0);
                    if (method instanceof ExtensionMethodNode) {
                        ClassNode selfType = (((ExtensionMethodNode) method).getExtensionMethodNode()).getParameters()[0].getType();
                        // we may inline a var!=null check instead of calling a helper method iff
                        // (1) the class doesn't define an asBoolean method (already tested)
                        // (2) no subclass defines an asBoolean method
                        // For (2), check that we are in one of these cases:
                        // (a) a final class
                        // (b) an effectively-final inner class
                        if (ClassHelper.isObjectType(selfType) && (Modifier.isFinal(top.getModifiers()) || isEffectivelyFinal(top))) {
                            Expression opt = new CompareToNullExpression(getExpression(), false);
                            opt.visit(visitor);
                            return;
                        }
                    }
                }
            }

            super.visit(visitor);
        }

        private static boolean isEffectivelyFinal(final ClassNode type) {
            if (!Modifier.isPrivate(type.getModifiers())) return false;

            List<ClassNode> outers = type.getOuterClasses();
            ClassNode outer = outers.get(outers.size() - 1);
            return !isExtended(type, outer.getInnerClasses());
        }

        private static boolean isExtended(final ClassNode type, final Iterator<? extends ClassNode> inners) {
            while (inners.hasNext()) { ClassNode next = inners.next();
                if (next != type && next.isDerivedFrom(type))
                    return true;
                if (isExtended(type, next.getInnerClasses()))
                    return true;
            }
            return false;
        }
    }
}
