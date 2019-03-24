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
package org.codehaus.groovy.tools.javac;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.ConstructorNode;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.stmt.BlockStatement;
import org.codehaus.groovy.ast.stmt.ExpressionStatement;
import org.codehaus.groovy.ast.stmt.Statement;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.ResolveVisitor;

public class JavaAwareResolveVisitor extends ResolveVisitor {

    public JavaAwareResolveVisitor(CompilationUnit cu) {
        super(cu);
    }

    private static Expression getConstructorCall(Statement code) {
        if (code==null) return null;
        if (code instanceof BlockStatement) {
            BlockStatement bs = (BlockStatement) code;
            if (bs.isEmpty()) return null;
            return getConstructorCall(bs.getStatements().get(0));
        }
        if (!(code instanceof ExpressionStatement)) return null;
        ExpressionStatement es = (ExpressionStatement) code;
        Expression exp = es.getExpression();
        if (!(exp instanceof ConstructorCallExpression)) return null;
        ConstructorCallExpression cce = (ConstructorCallExpression) exp;
        if (!cce.isSpecialCall()) return null;
        return cce;
    }

    @Override
    public void visitConstructor(ConstructorNode node) {
        super.visitConstructor(node);
        Statement code = node.getCode();
        Expression cce = getConstructorCall(code);
        if (cce==null) return;
        cce.accept(this);
    }

    protected void visitClassCodeContainer(Statement code) {
        // do nothing here, leave it to the normal resolving
    }

    public void addError(String msg, ASTNode expr) {
        // do nothing here, leave it to the normal resolving
    }
}
