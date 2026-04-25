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

/**
 * AST visitor used by {@link DataSet} to derive an SQL {@code WHERE} clause
 * and positional parameter list from a supported filter closure.
 */
public class SqlWhereVisitor extends CodeVisitorSupport {

    private final StringBuffer buffer = new StringBuffer();
    private final List<Object> parameters = new ArrayList<Object>();
    private Closure<?> closure;

    /**
     * Returns the SQL {@code WHERE} fragment built so far.
     *
     * @return the derived where fragment
     */
    public String getWhere() {
        return buffer.toString();
    }

    /**
     * Visits the closure return expression that defines the filter.
     *
     * @param statement the return statement to visit
     */
    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    /**
     * Visits a binary expression and renders it as SQL, collecting any literal values as parameters.
     *
     * @param expression the binary expression to visit
     */
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

    /**
     * Visits the wrapped boolean expression.
     *
     * @param expression the boolean expression to visit
     */
    @Override
    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    /**
     * Adds a constant value as a positional parameter.
     *
     * @param expression the constant expression to visit
     */
    @Override
    public void visitConstantExpression(ConstantExpression expression) {
        getParameters().add(expression.getValue());
        buffer.append("?");
    }

    /**
     * Appends a property reference as a column name.
     *
     * @param expression the property expression to visit
     */
    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getPropertyAsString());
    }

    /**
     * Supplies the closure whose captured variables may be referenced while the
     * SQL fragment is being derived.
     *
     * @param closure the closure currently being translated
     * @since 6.0.0
     */
    public void setClosure(Closure<?> closure) {
        this.closure = closure;
    }

    /**
     * Resolves a variable captured from the enclosing closure scope and adds it as a positional parameter.
     *
     * @param expression the variable expression to visit
     */
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

    /**
     * Returns the positional parameters collected while building the SQL fragment.
     *
     * @return the collected parameter values
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Maps a Groovy AST token to its SQL operator representation.
     *
     * @param token the AST token to translate
     * @return the SQL operator text
     */
    protected String tokenAsSql(Token token) {
        return switch (token.getType()) {
            case Types.COMPARE_EQUAL -> "=";
            case Types.LOGICAL_AND -> "and";
            case Types.LOGICAL_OR -> "or";
            default -> token.getText();
        };
    }
}
