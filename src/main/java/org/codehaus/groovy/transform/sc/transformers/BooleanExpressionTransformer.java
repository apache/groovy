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
import org.codehaus.groovy.ast.Parameter;
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

    private final StaticCompilationTransformer transformer;

    BooleanExpressionTransformer(final StaticCompilationTransformer transformer) {
        this.transformer = transformer;
    }

    Expression transformBooleanExpression(final BooleanExpression boolX) {
        Expression expr = boolX;
        boolean reverse = false;
        do { // undo arbitrary nesting of (Boolean|Not)Expressions
            if (expr instanceof NotExpression) reverse = !reverse;
            expr = ((BooleanExpression) expr).getExpression();
        } while (expr instanceof BooleanExpression);

        if (!(expr instanceof BinaryExpression)) {
            expr = transformer.transform(expr);
            ClassNode type = transformer.getTypeChooser().resolveType(expr, transformer.getClassNode());
            Expression opt = new OptimizingBooleanExpression(expr, type);
            if (reverse) opt = new NotExpression(opt);
            opt.setSourcePosition(boolX);
            return opt;
        }

        return transformer.superTransform(boolX);
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
            Expression opt = new OptimizingBooleanExpression(transformer.transform(getExpression()), type);
            opt.setSourcePosition(this);
            opt.copyNodeMetaData(this);
            return opt;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                WriterController controller = ((AsmClassGenerator) visitor).getController();
                MethodVisitor mv = controller.getMethodVisitor();
                OperandStack os = controller.getOperandStack();

                if (ClassHelper.Boolean_TYPE.equals(type)) {
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

                if (ClassHelper.isPrimitiveType(type) && !ClassHelper.VOID_TYPE.equals(type)) { // GROOVY-10920
                    getExpression().visit(visitor);
                    if (ClassHelper.boolean_TYPE.equals(type)) {
                        os.doGroovyCast(ClassHelper.boolean_TYPE);
                        return;
                    } else {
                        // in case of null safe invocation, it is possible that what was supposed to be a primitive type
                        // becomes the "null" constant, so we need to recheck
                        ClassNode top = controller.getOperandStack().getTopOperand();
                        if (ClassHelper.isPrimitiveType(top)) {
                            BytecodeHelper.convertPrimitiveToBoolean(mv, top);
                            os.replace(ClassHelper.boolean_TYPE);
                            return;
                        }
                    }
                }

                if (replaceAsBooleanWithCompareToNull(type, controller.getSourceUnit().getClassLoader())) {
                    new CompareToNullExpression(getExpression(), false).visit(visitor);
                    return;
                }
            }

            super.visit(visitor);
        }

        /**
         * Inline an "expr != null" check instead of boolean conversion iff:
         * (1) the class doesn't define an {@code asBoolean()} method
         * (2) no subclass defines an {@code asBoolean()} method
         * For (2), check that we are in one of these cases:
         *   (a) a final class
         *   (b) an effectively-final inner class
         */
        private static boolean replaceAsBooleanWithCompareToNull(final ClassNode type, final ClassLoader dgmProvider) {
            if (type.getMethod("asBoolean", Parameter.EMPTY_ARRAY) != null) {
                // GROOVY-10711
            } else if (Modifier.isFinal(type.getModifiers()) || isEffectivelyFinal(type)) {
                List<MethodNode> asBoolean = findDGMMethodsByNameAndArguments(dgmProvider, type, "asBoolean", ClassNode.EMPTY_ARRAY);
                if (asBoolean.size() == 1) {
                    MethodNode theAsBoolean = asBoolean.get(0);
                    if (theAsBoolean instanceof ExtensionMethodNode) {
                        ClassNode selfType = (((ExtensionMethodNode) theAsBoolean).getExtensionMethodNode()).getParameters()[0].getType();
                        if (ClassHelper.OBJECT_TYPE.equals(selfType)) {
                            return true;
                        }
                    }
                }
            }
            return false;
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
