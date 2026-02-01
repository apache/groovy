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

import groovy.transform.OperatorRename;
import org.codehaus.groovy.GroovyBugError;
import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.AnnotatedNode;
import org.codehaus.groovy.ast.AnnotationNode;
import org.codehaus.groovy.ast.ClassCodeExpressionTransformer;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.ClosureExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.control.CompilePhase;
import org.codehaus.groovy.control.SourceUnit;
import org.objectweb.asm.Opcodes;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static org.codehaus.groovy.ast.ClassHelper.make;
import static org.codehaus.groovy.ast.tools.GeneralUtils.assignX;
import static org.codehaus.groovy.ast.tools.GeneralUtils.callX;
import static org.codehaus.groovy.syntax.TokenUtil.removeAssignment;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND;
import static org.codehaus.groovy.syntax.Types.BITWISE_AND_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR;
import static org.codehaus.groovy.syntax.Types.BITWISE_OR_EQUAL;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR;
import static org.codehaus.groovy.syntax.Types.BITWISE_XOR_EQUAL;
import static org.codehaus.groovy.syntax.Types.COMPARE_TO;
import static org.codehaus.groovy.syntax.Types.DIVIDE;
import static org.codehaus.groovy.syntax.Types.DIVIDE_EQUAL;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT;
import static org.codehaus.groovy.syntax.Types.LEFT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.MINUS;
import static org.codehaus.groovy.syntax.Types.MINUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.MULTIPLY;
import static org.codehaus.groovy.syntax.Types.MULTIPLY_EQUAL;
import static org.codehaus.groovy.syntax.Types.PLUS;
import static org.codehaus.groovy.syntax.Types.PLUS_EQUAL;
import static org.codehaus.groovy.syntax.Types.POWER;
import static org.codehaus.groovy.syntax.Types.POWER_EQUAL;
import static org.codehaus.groovy.syntax.Types.REMAINDER;
import static org.codehaus.groovy.syntax.Types.REMAINDER_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_EQUAL;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED;
import static org.codehaus.groovy.syntax.Types.RIGHT_SHIFT_UNSIGNED_EQUAL;
import static org.codehaus.groovy.transform.AbstractASTTransformation.getMemberStringValue;

/**
 * Handles transformation for the @OperatorRename annotation.
 */
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class OperatorRenameASTTransformation extends ClassCodeExpressionTransformer implements ASTTransformation, Opcodes {

    private static final Class<OperatorRename> MY_CLASS = OperatorRename.class;
    private static final ClassNode MY_TYPE = make(MY_CLASS);
    private static final String MY_TYPE_NAME = "@" + MY_TYPE.getNameWithoutPackage();
    private SourceUnit sourceUnit;
    private Map<String, String> nameTable = new HashMap<>();

    @Override
    public void visit(ASTNode[] nodes, SourceUnit source) {
        sourceUnit = source;
        if (nodes.length != 2 || !(nodes[0] instanceof AnnotationNode anno) || !(nodes[1] instanceof AnnotatedNode parent)) {
            throw new GroovyBugError("Internal error: expecting [AnnotationNode, AnnotatedNode] but got: " + Arrays.asList(nodes));
        }

        if (!MY_TYPE.equals(anno.getClassNode())) return;

        addIfFound(anno, nameTable, "plus");
        addIfFound(anno, nameTable, "minus");
        addIfFound(anno, nameTable, "multiply");
        addIfFound(anno, nameTable, "div");
        addIfFound(anno, nameTable, "remainder");
        addIfFound(anno, nameTable, "power");
        addIfFound(anno, nameTable, "leftShift");
        addIfFound(anno, nameTable, "rightShift");
        addIfFound(anno, nameTable, "rightShiftUnsigned");
        addIfFound(anno, nameTable, "and");
        addIfFound(anno, nameTable, "or");
        addIfFound(anno, nameTable, "xor");
        addIfFound(anno, nameTable, "compareTo");
        if (parent instanceof ClassNode) {
            super.visitClass((ClassNode) parent);
        } else if (parent instanceof ConstructorNode) {
            super.visitConstructorOrMethod((MethodNode) parent, true);
        } else if (parent instanceof MethodNode) {
            super.visitConstructorOrMethod((MethodNode) parent, false);
        }
    }

    private void addIfFound(AnnotationNode anno, Map<String, String> nameTable, String origName) {
        String newName = getMemberStringValue(anno, origName);
        if (newName != null) nameTable.put(origName, newName);
    }

    @Override
    public Expression transform(Expression expr) {
        if (expr == null) return null;
        if (expr instanceof BinaryExpression be) {
            int type = be.getOperation().getType();
            String oldName = getOperationName(type);
            if (nameTable.containsKey(oldName)) {
                boolean isEqualOperator = removeAssignment(type) != type;
                Expression left = transform(be.getLeftExpression());
                Expression right = transform(be.getRightExpression());
                Expression result = callX(left, nameTable.get(oldName), right);
                if (isEqualOperator) {
                    result = assignX(left, result);
                }
                result.setSourcePosition(be);
                return result;
            }
        } else if (expr instanceof ClosureExpression ce) {
            ce.getCode().visit(this);
        }
        return expr.transformExpression(this);
    }

    @Override
    protected SourceUnit getSourceUnit() {
        return sourceUnit;
    }

    static String getOperationName(final int op) {
        return switch (op) {
            case COMPARE_TO -> "compareTo";
            case BITWISE_AND, BITWISE_AND_EQUAL -> "and";
            case BITWISE_OR, BITWISE_OR_EQUAL -> "or";
            case BITWISE_XOR, BITWISE_XOR_EQUAL -> "xor";
            case PLUS, PLUS_EQUAL -> "plus";
            case MINUS, MINUS_EQUAL -> "minus";
            case MULTIPLY, MULTIPLY_EQUAL -> "multiply";
            case DIVIDE, DIVIDE_EQUAL -> "div";
            case REMAINDER, REMAINDER_EQUAL -> "remainder";
            case POWER, POWER_EQUAL -> "power";
            case LEFT_SHIFT, LEFT_SHIFT_EQUAL -> "leftShift";
            case RIGHT_SHIFT, RIGHT_SHIFT_EQUAL -> "rightShift";
            case RIGHT_SHIFT_UNSIGNED, RIGHT_SHIFT_UNSIGNED_EQUAL -> "rightShiftUnsigned";
            default -> null;
        };
    }

}
