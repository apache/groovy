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
    private final List<Object> parameters = new ArrayList<>();

    public String getWhere() {
        return buffer.toString();
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

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

    public void visitBooleanExpression(BooleanExpression expression) {
        expression.getExpression().visit(this);
    }

    public void visitConstantExpression(ConstantExpression expression) {
        getParameters().add(expression.getValue());
        buffer.append("?");
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getPropertyAsString());
    }

    @Override
    public void visitVariableExpression(VariableExpression expression) {
        throw new GroovyRuntimeException("DataSet currently doesn't support arbitrary variables, only literals: found attempted reference to variable '" + expression.getName() + "'");
    }

    public List<Object> getParameters() {
        return parameters;
    }

    protected String tokenAsSql(Token token) {
        switch (token.getType()) {
            case Types.COMPARE_EQUAL:
                return "=";
            case Types.LOGICAL_AND:
                return "and";
            case Types.LOGICAL_OR:
                return "or";
            default:
                return token.getText();
        }
    }
}
