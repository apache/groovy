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

import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.expr.*;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;
import org.codehaus.groovy.transform.sc.ListOfExpressionsExpression;
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;

import java.util.Iterator;
import java.util.List;

import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.*;

public class BinaryExpressionTransformer {
    private final StaticCompilationTransformer staticCompilationTransformer;

    public BinaryExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    Expression transformBinaryExpression(final BinaryExpression bin) {
        Object[] list = (Object[]) bin.getNodeMetaData(BINARY_EXP_TARGET);
        if (list != null) {
            Token operation = bin.getOperation();
            int operationType = operation.getType();
            if (operationType==Types.COMPARE_EQUAL || operationType == Types.COMPARE_NOT_EQUAL) {
                // let's check if one of the operands is the null constant
                if (isNullConstant(bin.getLeftExpression())) {
                    return new CompareToNullExpression(staticCompilationTransformer.transform(bin.getRightExpression()), operationType==Types.COMPARE_EQUAL);
                } else if (isNullConstant(bin.getRightExpression())) {
                    return new CompareToNullExpression(staticCompilationTransformer.transform(bin.getLeftExpression()), operationType==Types.COMPARE_EQUAL);
                }
            }
            boolean isAssignment = StaticTypeCheckingSupport.isAssignment(operationType);
            MethodCallExpression call;
            MethodNode node = (MethodNode) list[0];
            String name = (String) list[1];
            Expression left = staticCompilationTransformer.transform(bin.getLeftExpression());
            Expression right = staticCompilationTransformer.transform(bin.getRightExpression());
            call = new MethodCallExpression(
                    left,
                    name,
                    new ArgumentListExpression(right)
            );
            call.setMethodTarget(node);
            MethodNode adapter = StaticCompilationTransformer.BYTECODE_BINARY_ADAPTERS.get(operationType);
            if (adapter != null) {
                ClassExpression sba = new ClassExpression(StaticCompilationTransformer.BYTECODE_ADAPTER_CLASS);
                // replace with compareEquals
                call = new MethodCallExpression(sba,
                        "compareEquals",
                        new ArgumentListExpression(left, right));
                call.setMethodTarget(adapter);
            }
            if (!isAssignment) return call;
            // case of +=, -=, /=, ...
            // the method represents the operation type only, and we must add an assignment
            return new BinaryExpression(left, Token.newSymbol("=", operation.getStartLine(), operation.getStartColumn()), call);
        }
        if (bin.getOperation().getType() == Types.EQUAL && bin.getLeftExpression() instanceof TupleExpression && bin.getRightExpression() instanceof ListExpression) {
            // multiple assignment
            ListOfExpressionsExpression cle = new ListOfExpressionsExpression();
            boolean isDeclaration = bin instanceof DeclarationExpression;
            List<Expression> leftExpressions = ((TupleExpression) bin.getLeftExpression()).getExpressions();
            List<Expression> rightExpressions = ((ListExpression) bin.getRightExpression()).getExpressions();
            Iterator<Expression> leftIt = leftExpressions.iterator();
            Iterator<Expression> rightIt = rightExpressions.iterator();
            while (leftIt.hasNext()) {
                Expression left = leftIt.next();
                if (rightIt.hasNext()) {
                    Expression right = rightIt.next();
                    BinaryExpression bexp = isDeclaration?
                            new DeclarationExpression(left, bin.getOperation(), right):
                            new BinaryExpression(left, bin.getOperation(), right);
                    bexp.setSourcePosition(right);
                    cle.addExpression(bexp);
                }
            }
            return staticCompilationTransformer.transform(cle);
        }
        return staticCompilationTransformer.superTransform(bin);
    }

    private static boolean isNullConstant(final Expression expression) {
        return expression instanceof ConstantExpression && ((ConstantExpression) expression).getValue()==null;
    }

}