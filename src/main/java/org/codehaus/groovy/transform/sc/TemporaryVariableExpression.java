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

import static org.codehaus.groovy.transform.stc.StaticTypesMarker.INFERRED_TYPE;

/**
 * A front-end class for {@link org.codehaus.groovy.classgen.asm.ExpressionAsVariableSlot} which
 * allows defining temporary variables loaded from variable slots directly at the AST level,
 * without any knowledge of {@link org.codehaus.groovy.classgen.AsmClassGenerator}.
 *
 * @since 2.4.0
 */
public class TemporaryVariableExpression extends Expression {

    private final Expression expression;

    private ExpressionAsVariableSlot[] variable = {null};

    /**
     * Wraps an expression so it can be cached in a temporary variable slot during bytecode generation.
     *
     * @param expression the expression to cache
     */
    public TemporaryVariableExpression(final Expression expression) {
        this.expression = expression;
        putNodeMetaData(INFERRED_TYPE, expression.getNodeMetaData(INFERRED_TYPE));
    }

    /**
     * Returns the wrapped expression type.
     *
     * @return the inferred type of the wrapped expression
     */
    @Override
    public ClassNode getType() {
        return expression.getType();
    }

    /**
     * Releases the temporary variable slot associated with this expression.
     *
     * @param controller the active writer controller
     */
    public void remove(final WriterController controller) {
        controller.getCompileStack().removeVar(variable[0].getIndex());
        variable[0] = null;
    }

    /**
     * Transforms the wrapped expression while preserving the shared variable slot.
     *
     * @param transformer the expression transformer to apply
     * @return a transformed temporary-variable expression
     */
    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        TemporaryVariableExpression result = new TemporaryVariableExpression(transformer.transform(expression));
        result.copyNodeMetaData(this);
        result.variable = variable;
        return result;
    }

    /**
     * Emits the cached variable-slot access when visiting bytecode, or delegates to the wrapped expression otherwise.
     *
     * @param visitor the visitor to accept
     */
    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        if (visitor instanceof AsmClassGenerator) {
            if (variable[0] == null) {
                WriterController controller = ((AsmClassGenerator) visitor).getController();
                variable[0] = new ExpressionAsVariableSlot(controller, expression);
            }
            variable[0].visit(visitor);
        } else {
            expression.visit(visitor);
        }
    }
}
