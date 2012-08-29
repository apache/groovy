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
package org.codehaus.groovy.transform.sc.transformers;

import org.codehaus.groovy.ast.*;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.codehaus.groovy.classgen.asm.sc.StaticTypesTypeChooser;
import org.codehaus.groovy.transform.stc.ExtensionMethodNode;
import org.objectweb.asm.MethodVisitor;

import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.findDGMMethodsByNameAndArguments;
import static org.objectweb.asm.Opcodes.*;

public class BooleanExpressionTransformer {
    private final StaticCompilationTransformer transformer;

    public BooleanExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        transformer = staticCompilationTransformer;
    }

    Expression transformBooleanExpression(final BooleanExpression booleanExpression) {
        if (booleanExpression instanceof NotExpression) {
            return transformer.superTransform(booleanExpression);
        }
        final Expression expression = booleanExpression.getExpression();
        if (!(expression instanceof BinaryExpression)) {
            StaticTypesTypeChooser typeChooser = transformer.getTypeChooser();
            final ClassNode type = typeChooser.resolveType(expression, transformer.getClassNode());
            BooleanExpression transformed = new OptimizingBooleanExpression(transformer.transform(expression),type);
            transformed.setSourcePosition(booleanExpression);
            transformed.copyNodeMetaData(booleanExpression);
            return transformed;

        }
        return transformer.superTransform(booleanExpression);
    }
    
    private static boolean isExtended(ClassNode owner, Iterator<InnerClassNode> classes) {
        while (classes.hasNext()) {
            InnerClassNode next =  classes.next();
            if (next!=owner && next.isDerivedFrom(owner)) return true;
        }
        if (owner.getInnerClasses().hasNext()) {
            return isExtended(owner, owner.getInnerClasses());
        }
        return false;
    }

    private static class OptimizingBooleanExpression extends BooleanExpression {

        private final Expression expression;
        private final ClassNode type;

        public OptimizingBooleanExpression(final Expression expression, final ClassNode type) {
            super(expression);
            this.expression = expression;
            // we must use the redirect node, otherwise InnerClassNode would not have the "correct" type
            this.type = type.redirect();
        }

        @Override
        public Expression transformExpression(final ExpressionTransformer transformer) {
            Expression ret = new OptimizingBooleanExpression(transformer.transform(expression), type);
            ret.setSourcePosition(this);
            ret.copyNodeMetaData(this);
            return ret;
        }

        @Override
        public void visit(final GroovyCodeVisitor visitor) {
            if (visitor instanceof AsmClassGenerator) {
                AsmClassGenerator acg = (AsmClassGenerator) visitor;
                WriterController controller = acg.getController();
                if (type.equals(ClassHelper.boolean_TYPE)) {
                    expression.visit(visitor);
                    controller.getOperandStack().doGroovyCast(ClassHelper.boolean_TYPE);
                    return;
                }
                if (type.equals(ClassHelper.Boolean_TYPE)) {
                    expression.visit(visitor);
                    // unbox
                    MethodVisitor mv = controller.getMethodVisitor();
                    mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/Boolean", "booleanValue", "()Z");
                    controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
                    return;
                }
                if (type.equals(ClassHelper.int_TYPE) || type.equals(ClassHelper.byte_TYPE)
                        || type.equals(ClassHelper.short_TYPE) || type.equals(ClassHelper.char_TYPE)) {
                    // int on stack
                    expression.visit(visitor);
                    return;
                } else if (type.equals(ClassHelper.long_TYPE)) {
                    expression.visit(visitor);
                    MethodVisitor mv = controller.getMethodVisitor();
                    mv.visitInsn(L2I);
                    controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
                    return;
                } else if (type.equals(ClassHelper.float_TYPE)) {
                    expression.visit(visitor);
                    MethodVisitor mv = controller.getMethodVisitor();
                    mv.visitInsn(F2I);
                    controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
                    return;
                } else if (type.equals(ClassHelper.double_TYPE)) {
                    expression.visit(visitor);
                    MethodVisitor mv = controller.getMethodVisitor();
                    mv.visitInsn(D2I);
                    controller.getOperandStack().replace(ClassHelper.boolean_TYPE);
                    return;
                }
                List<MethodNode> asBoolean = findDGMMethodsByNameAndArguments(type, "asBoolean", ClassNode.EMPTY_ARRAY);
                if (asBoolean.size() == 1) {
                    MethodNode node = asBoolean.get(0);
                    if (node instanceof ExtensionMethodNode) {
                        MethodNode dgmNode = ((ExtensionMethodNode) node).getExtensionMethodNode();
                        ClassNode owner = dgmNode.getParameters()[0].getType();
                        if (ClassHelper.OBJECT_TYPE.equals(owner)) {
                            // we may inline a var!=null check instead of calling a helper method iff
                            // (1) the class doesn't define an asBoolean method (already tested)
                            // (2) no subclass defines an asBoolean method
                            // For (2), we check that we are in one of those cases
                            // (a) a final class
                            // (b) a private inner class without subclass
                            if (Modifier.isFinal(type.getModifiers())
                                    || (type instanceof InnerClassNode
                                    && Modifier.isPrivate(type.getModifiers())
                                    && !isExtended(type, type.getOuterClass().getInnerClasses()))
                                    ) {
                                CompareToNullExpression expr = new CompareToNullExpression(
                                        expression, false
                                );
                                expr.visit(acg);
                                return;
                            }
                        }
                    }
                }
                super.visit(visitor);
            } else {
                super.visit(visitor);
            }
        }
    }
}