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
package org.codehaus.groovy.transform;

import groovy.transform.NullCheck;
import org.apache.groovy.ast.tools.ConstructorNodeUtils;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ThrowStatement;
import org.codehaus.groovy.classgen.BytecodeSequence;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;

import java.util.List;

import static org.apache.groovy.ast.tools.AnnotatedNodeUtils.isGenerated;
import static org.apache.groovy.ast.tools.MethodNodeUtils.getCodeAsBlock;
import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.constX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ctorX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.ifS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.isNullX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.throwS;
import static org.codehaus.groovy.ast.tools.GeneralUtils.varX;

/**
 * Handles generation of code for the @NullCheck annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.INSTRUCTION_SELECTION)
public class NullCheckASTTransformation extends AbstractASTTransformation {
    public static final ClassNode NULL_CHECK_TYPE = make(NullCheck.class);
    private static final String NULL_CHECK_NAME = "@" + NULL_CHECK_TYPE.getNameWithoutPackage();
    private static final ClassNode EXCEPTION = ClassHelper.make(IllegalArgumentException.class);
    private static final String NULL_CHECK_IS_PROCESSED = "NullCheck.isProcessed";

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        init(nodes, source);
        AnnotatedNode parent = (AnnotatedNode) nodes[1];
        AnnotationNode anno = (AnnotationNode) nodes[0];
        if (!NULL_CHECK_TYPE.equals(anno.getClassNode())) return;
        boolean includeGenerated = isIncludeGenerated(anno);

        if (parent instanceof ClassNode) {
            ClassNode cNode = (ClassNode) parent;
            if (!checkNotInterface(cNode, NULL_CHECK_NAME)) return;
            for (ConstructorNode cn : cNode.getDeclaredConstructors()) {
                adjustMethod(cn, includeGenerated);
            }
            for (MethodNode mn : cNode.getAllDeclaredMethods()) {
                adjustMethod(mn, includeGenerated);
            }
        } else if (parent instanceof MethodNode) {
            // handles constructor case too
            adjustMethod((MethodNode) parent, false);
        }
    }

    private boolean isIncludeGenerated(AnnotationNode anno) {
        return memberHasValue(anno, "includeGenerated", true);
    }

    public static boolean hasIncludeGenerated(ClassNode cNode) {
        List<AnnotationNode> annotations = cNode.getAnnotations(NULL_CHECK_TYPE);
        if (annotations.isEmpty()) return false;
        return hasIncludeGenerated(annotations.get(0));
    }

    private static boolean hasIncludeGenerated(AnnotationNode node) {
        final Expression member = node.getMember("includeGenerated");
        return member instanceof ConstantExpression && ((ConstantExpression) member).getValue().equals(true);
    }

    private void adjustMethod(MethodNode mn, boolean includeGenerated) {
        BlockStatement newCode = getCodeAsBlock(mn);
        if (mn.getParameters().length == 0) return;
        boolean generated = isGenerated(mn);
        int startingIndex = 0;
        if (!includeGenerated && generated) return;
        if (isMarkedAsProcessed(mn)) return;
        if (mn instanceof ConstructorNode) {
            // some transform has been here already and we assume it knows what it is doing
            if (mn.getFirstStatement() instanceof BytecodeSequence) return;
            // ignore any constructors calling this(...) or super(...)
            ConstructorCallExpression cce = ConstructorNodeUtils.getFirstIfSpecialConstructorCall(mn.getCode());
            if (cce != null) {
                if (generated) {
                    return;
                } else {
                    startingIndex = 1; // skip over this/super() call
                }
            }
        }
        for (Parameter p : mn.getParameters()) {
            if (ClassHelper.isPrimitiveType(p.getType())) continue;
            newCode.getStatements().add(startingIndex, ifS(isNullX(varX(p)), makeThrowStmt(p.getName())));
        }
        mn.setCode(newCode);
    }

    public static ThrowStatement makeThrowStmt(String name) {
        return throwS(ctorX(EXCEPTION, constX(name + " cannot be null")));
    }

    /**
     * Mark a method as already processed.
     *
     * @param mn the method node to be considered already processed
     */
    public static void markAsProcessed(MethodNode mn) {
        mn.setNodeMetaData(NULL_CHECK_IS_PROCESSED, Boolean.TRUE);
    }

    private static boolean isMarkedAsProcessed(MethodNode mn) {
        Boolean r = mn.getNodeMetaData(NULL_CHECK_IS_PROCESSED);
        return null != r && r;
    }
}
