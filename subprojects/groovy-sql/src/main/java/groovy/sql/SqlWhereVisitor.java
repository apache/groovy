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
package groovy.sql;

import groovy.lang.Closure;
import groovy.lang.GroovyRuntimeException;
import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.BinaryExpression;
import org.codehaus.groovy.ast.expr.BooleanExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.expr.VariableExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;
import org.codehaus.groovy.syntax.Token;
import org.codehaus.groovy.syntax.Types;

import java.util.ArrayList;
import java.util.List;

public class SqlWhereVisitor extends CodeVisitorSupport {

    private final StringBuffer buffer = new StringBuffer();
    private final List<Object> parameters = new ArrayList<Object>();
    private Closure<?> closure;

    public String getWhere() {
        return buffer.toString();
    }

    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    @Override
    public void visitBinaryExpression(BinaryExpression expression) {
        Expression left = expression.getLeftExpression();
        Expression right = expression.getRightExpression();
        boolean leaf = (right instanceof ConstantExpression || left instanceof ConstantExpression);

        if (!leaf) buffer.append("(");
        left.visit(this);
        buffer.append(" ");

        Token token = expression.getOperation();
        buffer.append(tokenAsSql(token));

        buffer.append(" ");
        right.visit(this);
        if (!leaf) buffer.append(")");
    }

    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        getParameters().add(expression.getValue());
        buffer.append("?");
    }

    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getPropertyAsString());
    }

    /**
     * @since 6.0.0
     */
    public void setClosure(Closure<?> closure) {
        this.closure = closure;
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        // Try to resolve captured variables from the closure's context
        if (closure != null) {
            String name = expression.getName();
            try {
                java.lang.reflect.Field field = closure.getClass().getDeclaredField(name);
                if (!field.trySetAccessible()) {
                    throw new GroovyRuntimeException("DataSet unable to access captured variable '" + name + "'");
                }
                Object value = field.get(closure);
                // Groovy wraps shared (mutable) variables in a Reference
                if (value instanceof groovy.lang.Reference) {
                    value = ((groovy.lang.Reference<?>) value).get();
                }
                getParameters().add(value);
                buffer.append("?");
                return;
            } catch (ReflectiveOperationException ignored) {
                // fall through to error
            }
        }
        throw new GroovyRuntimeException("DataSet unable to resolve variable '" + expression.getName()
                + "'. Supported: literals and variables captured from the enclosing scope.");
    }

    public List<Object> getParameters() {
        return parameters;
    }

    protected String tokenAsSql(Token token) {
        return switch (token.getType()) {
            case Types.COMPARE_EQUAL -> "=";
            case Types.LOGICAL_AND -> "and";
            case Types.LOGICAL_OR -> "or";
            default -> token.getText();
        };
    }
}
