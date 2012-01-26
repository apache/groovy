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
import org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport;
import static org.codehaus.groovy.transform.sc.StaticCompilationMetadataKeys.*;

public class BinaryExpressionTransformer {
    private final StaticCompilationTransformer staticCompilationTransformer;

    public BinaryExpressionTransformer(StaticCompilationTransformer staticCompilationTransformer) {
        this.staticCompilationTransformer = staticCompilationTransformer;
    }

    Expression transformBinaryExpression(final Expression expr) {
        Object[] list = (Object[]) expr.getNodeMetaData(BINARY_EXP_TARGET);
        if (list != null) {
            BinaryExpression bin = (BinaryExpression) expr;
            Token operation = bin.getOperation();
            boolean isAssignment = StaticTypeCheckingSupport.isAssignment(operation.getType());
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
            ClassExpression sba = new ClassExpression(StaticCompilationTransformer.BYTECODE_ADAPTER_CLASS);
            MethodNode adapter = StaticCompilationTransformer.BYTECODE_BINARY_ADAPTERS.get(operation.getType());
            if (adapter != null) {
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
        return staticCompilationTransformer.superTransform(expr);
    }

}