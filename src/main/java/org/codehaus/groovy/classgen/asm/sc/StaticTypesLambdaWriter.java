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

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.LambdaExpression;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.BytecodeHelper;
import org.codehaus.groovy.classgen.asm.LambdaWriter;
import org.codehaus.groovy.classgen.asm.WriterController;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.List;
import java.util.stream.Collectors;

import static org.apache.groovy.parser.antlr4.AstBuilder.LAMBDA_ENCLOSING_CLASSNODE;
import static org.apache.groovy.parser.antlr4.AstBuilder.SYNTHETIC_LAMBDA_METHOD_NODE;
import static org.codehaus.groovy.classgen.asm.sc.StaticInvocationWriter.PARAMETER_TYPE;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;

/**
 * Writer responsible for generating lambda classes in statically compiled mode.
 *
 */
public class StaticTypesLambdaWriter extends LambdaWriter {
    private StaticTypesClosureWriter staticTypesClosureWriter;
    private WriterController controller;

    public StaticTypesLambdaWriter(WriterController wc) {
        super(wc);
        this.staticTypesClosureWriter = new StaticTypesClosureWriter(wc);
        this.controller = wc;
    }

    @Override
    public void writeLambda(LambdaExpression expression) {
        ClassNode parameterType = expression.getNodeMetaData(PARAMETER_TYPE);

        List<MethodNode> abstractMethodNodeList =
                parameterType.redirect().getMethods().stream()
                        .filter(MethodNode::isAbstract)
                        .collect(Collectors.toList());

        if (abstractMethodNodeList.size() != 1) {
            super.writeClosure(expression);
            return;
        }

        MethodNode abstractMethodNode = abstractMethodNodeList.get(0);
        String abstractMethodName = abstractMethodNode.getName();
        String abstractMethodDesc = "()L" + parameterType.redirect().getPackageName().replace('.', '/') + "/" + parameterType.redirect().getNameWithoutPackage() + ";";


        AsmClassGenerator acg = controller.getAcg();
        ClassVisitor cw = acg.getClassVisitor();
        cw.visitInnerClass(
                "java/lang/invoke/MethodHandles$Lookup",
                "java/lang/invoke/MethodHandles",
                "Lookup",
                ACC_PUBLIC + ACC_FINAL + ACC_STATIC);

        MethodVisitor mv = controller.getMethodVisitor();
        MethodNode syntheticLambdaMethodNode = expression.getNodeMetaData(SYNTHETIC_LAMBDA_METHOD_NODE);
        ClassNode lambdaEnclosingClassNode = expression.getNodeMetaData(LAMBDA_ENCLOSING_CLASSNODE);

        String syntheticLambdaMethodDesc = BytecodeHelper.getMethodDescriptor(syntheticLambdaMethodNode);

        controller.getOperandStack().push(ClassHelper.OBJECT_TYPE);

        mv.visitInvokeDynamicInsn(
                abstractMethodName,
                abstractMethodDesc,
                new Handle(
                        Opcodes.H_INVOKESTATIC,
                        "java/lang/invoke/LambdaMetafactory",
                        "metafactory",
                        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodType;Ljava/lang/invoke/MethodHandle;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
                        false),
                new Object[] {
                        Type.getType(syntheticLambdaMethodDesc),
                        new Handle(
                                Opcodes.H_INVOKESTATIC,
                                lambdaEnclosingClassNode.getName(),
                                syntheticLambdaMethodNode.getName(),
                                syntheticLambdaMethodDesc,
                                false),
                        Type.getType(syntheticLambdaMethodDesc)
                });

    }

    @Override
    protected ClassNode createClosureClass(final ClosureExpression expression, final int mods) {
        return staticTypesClosureWriter.createClosureClass(expression, mods);
    }
}
