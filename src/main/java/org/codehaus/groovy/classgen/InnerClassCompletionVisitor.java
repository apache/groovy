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
package org.codehaus.groovy.classgen;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.InnerClassNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.TupleExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;
import java.util.StringJoiner;

import static org.apache.groovy.ast.tools.ClassNodeUtils.addGeneratedConstructor;
import static org.apache.groovy.ast.tools.ConstructorNodeUtils.getFirstIfSpecialConstructorCall;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getCodeAsBlock;
import static org.codehaus.groovy.ast.tools.GeneralUtils.block;
import static org.codehaus.groovy.ast.tools.GeneralUtils.castX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorSuperX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorThisX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.nullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.stmt;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;
import static org.codehaus.groovy.transform.trait.Traits.isTrait;
import static org.objectweb.asm.Opcodes.ACC_FINAL;
import static org.objectweb.asm.Opcodes.ACC_MANDATED;
import static org.objectweb.asm.Opcodes.ACC_PUBLIC;
import static org.objectweb.asm.Opcodes.ACC_STATIC;
import static org.objectweb.asm.Opcodes.ACC_SYNTHETIC;

public class InnerClassCompletionVisitor extends InnerClassVisitorHelper {

    private ClassNode classNode;
    private FieldNode thisField;
    private final SourceUnit sourceUnit;

    public InnerClassCompletionVisitor(CompilationUnit cu, SourceUnit su) {
        sourceUnit = su;
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    @Override
    public void visitClass(final ClassNode node) {
        classNode = node;
        thisField = null;

        if (node.isEnum() || node.isInterface() || isTrait(node.getOuterClass())) return;

        if (node instanceof InnerClassNode innerClass) {
            thisField = node.getDeclaredField("this$0");
            if (innerClass.getVariableScope() == null && node.getDeclaredConstructors().isEmpty()) {
                // add empty default constructor
                addGeneratedConstructor(innerClass, ACC_PUBLIC, Parameter.EMPTY_ARRAY, null, null);
            }
            super.visitClass(node);
        }
    }

    @Override
    public void visitConstructor(final ConstructorNode node) {
        addThisReference(node);
        super.visitConstructor(node);
        // an anonymous inner class may use a private constructor (via a bridge) if its super class is also an outer class
        if (((InnerClassNode) classNode).isAnonymous() && classNode.getOuterClasses().contains(classNode.getSuperClass())) {
            ConstructorNode superCtor = classNode.getSuperClass().getDeclaredConstructor(node.getParameters());
            if (superCtor != null && superCtor.isPrivate()) {
                ClassNode superClass = classNode.getUnresolvedSuperClass();
                makeBridgeConstructor(superClass, node.getParameters()); // GROOVY-5728
                ConstructorCallExpression superCtorCall = getFirstIfSpecialConstructorCall(node.getCode());
                ((TupleExpression) superCtorCall.getArguments()).addExpression(castX(superClass, nullX()));
            }
        }
    }

    private static void makeBridgeConstructor(final ClassNode c, final Parameter[] p) {
        Parameter[] newP = new Parameter[p.length + 1];
        for (int i = 0; i < p.length; i += 1) {
            newP[i] = new Parameter(p[i].getType(), "p" + i);
        }
        newP[p.length] = new Parameter(c, "$anonymous");

        if (c.getDeclaredConstructor(newP) == null) {
            TupleExpression args = new TupleExpression();
            for (int i = 0; i < p.length; i += 1) args.addExpression(varX(newP[i]));
            addGeneratedConstructor(c, ACC_SYNTHETIC, newP, ClassNode.EMPTY_ARRAY, stmt(ctorThisX(args)));
        }
    }

    private void addThisReference(final ConstructorNode node) {
        if (!shouldHandleImplicitThisForInnerClass(classNode)) return;

        // add "this$0" field init

        // add this parameter to node
        Parameter[] params = node.getParameters();
        Parameter[] newParams = new Parameter[params.length + 1];
        System.arraycopy(params, 0, newParams, 1, params.length);

        Parameter thisZero = new Parameter(classNode.getOuterClass().getPlainNodeReference(), getUniqueName(params, node));
        thisZero.setModifiers(ACC_FINAL | ACC_MANDATED);
        if (params.length > 0 && params[0].isReceiver()) {
            newParams[0] = newParams[1];
            newParams[1] = thisZero;
        } else {
            newParams[0] = thisZero;
        }
        node.setParameters(newParams);

        BlockStatement block = getCodeAsBlock(node);
        BlockStatement newCode = block();
        addFieldInit(thisZero, thisField, newCode);
        ConstructorCallExpression cce = getFirstIfSpecialConstructorCall(block);
        if (cce == null) {
            ClassNode superClass = classNode.getSuperClass();
            Parameter[] implicit = Parameter.EMPTY_ARRAY; // signature of the super class constuctor
            if (superClass.getOuterClass() != null && (superClass.getModifiers() & ACC_STATIC) == 0)
                implicit = new Parameter[]{new Parameter(superClass.getOuterClass(), "outerClass")};
            if (superClass.getDeclaredConstructor(implicit) == null && !superClass.getDeclaredConstructors().isEmpty()) { // GROOVY-11485
                var joiner = new StringJoiner(", ", superClass.getNameWithoutPackage() + "(", ")");
                for (var parameter : implicit) joiner.add(parameter.getType().getNameWithoutPackage());
                addError("An explicit constructor is required because the implicit super constructor " + joiner.toString() + " is undefined", classNode);
            }
            cce = ctorSuperX(new TupleExpression());
            block.getStatements().add(0, stmt(cce));
        }
        if (shouldImplicitlyPassThisZero(cce)) {
            TupleExpression args = (TupleExpression) cce.getArguments();
            List<Expression> expressions = args.getExpressions();
            VariableExpression ve = varX(thisZero.getName());
            ve.setAccessedVariable(thisZero);
            expressions.add(0, ve);
        }
        if (cce.isSuperCall()) {
            // we have a call to super here, so we need to add
            // our code after that
            block.getStatements().add(1, newCode);
        }
        node.setCode(block);
    }

    private boolean shouldImplicitlyPassThisZero(final ConstructorCallExpression cce) {
        boolean pass = false;
        if (cce.isThisCall()) {
            pass = true;
        } else if (cce.isSuperCall()) {
            // if the super class is another non-static inner class in the same
            // outer class hierarchy, implicit this needs to be passed
            ClassNode superClass = classNode.getSuperClass();
            if (!superClass.isEnum()
                    && !superClass.isInterface()
                    && superClass instanceof InnerClassNode
                    && !isStatic((InnerClassNode) superClass)
                    && classNode.getOuterClass().isDerivedFrom(superClass.getOuterClass())) {
                pass = true;
            }
        }
        return pass;
    }

    private String getUniqueName(final Parameter[] params, final ConstructorNode node) {
        String namePrefix = "$p";
        outer:
        for (int i = 0; i < 100; i++) {
            namePrefix = namePrefix + "$";
            for (Parameter p : params) {
                if (p.getName().equals(namePrefix)) continue outer;
            }
            return namePrefix;
        }
        addError("unable to find a unique prefix name for synthetic this reference in inner class constructor", node);
        return namePrefix;
    }
}
