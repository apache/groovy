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
package org.codehaus.groovy.transform.sc;

import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.ExpressionTransformer;
import org.codehaus.groovy.classgen.AsmClassGenerator;
import org.codehaus.groovy.classgen.asm.ExpressionAsVariableSlot;
import org.codehaus.groovy.classgen.asm.WriterController;

/**
 * A front-end class for {@link org.codehaus.groovy.classgen.asm.ExpressionAsVariableSlot} which
 * allows defining temporary variables loaded from variable slots directly at the AST level,
 * without any knowledge of {@link org.codehaus.groovy.classgen.AsmClassGenerator}.
 *
 * @since 2.4.0
 */
public class TemporaryVariableExpression extends Expression {

    private final Expression expression;

    private ExpressionAsVariableSlot variable;

    public TemporaryVariableExpression(final Expression expression) {
        this.expression = expression;
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        TemporaryVariableExpression result = new TemporaryVariableExpression(expression.transformExpression(transformer));
        result.copyNodeMetaData(this);
        return result;
    }

    @Override
    public void accept(final GroovyCodeVisitor visitor) {
        if (visitor instanceof AsmClassGenerator) {
            if (variable==null) {
                AsmClassGenerator acg = (AsmClassGenerator) visitor;
                WriterController controller = acg.getController();
                variable = new ExpressionAsVariableSlot(controller, expression);
            }
            variable.accept(visitor);
        } else {
            expression.accept(visitor);
        }
    }

    public void remove(WriterController controller) {
        controller.getCompileStack().removeVar(variable.getIndex());
        variable = null;
    }

    @Override
    public ClassNode getType() {
        return expression.getType();
    }
}
