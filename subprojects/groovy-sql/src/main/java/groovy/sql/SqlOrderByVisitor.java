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

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;

/**
 * AST visitor used by {@link DataSet} to derive an SQL {@code ORDER BY} clause
 * from a simple sort closure.
 */
public class SqlOrderByVisitor extends CodeVisitorSupport {

    private final StringBuffer buffer = new StringBuffer();

    /**
     * Returns the SQL {@code ORDER BY} fragment built so far.
     *
     * @return the derived order-by fragment
     */
    public String getOrderBy() {
        return buffer.toString();
    }

    /**
     * Visits the closure return expression that defines the sort key.
     *
     * @param statement the return statement to visit
     */
    @Override
    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    /**
     * Appends the referenced property name as the next order-by term.
     *
     * @param expression the property expression to visit
     */
    @Override
    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getPropertyAsString());
    }

}
