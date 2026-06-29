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

import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.query.AstQuery;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Derives an SQL {@code ORDER BY} fragment from a simple sort closure, used by {@link DataSet}.
 */
public class SqlOrderByVisitor {

    private final StringBuffer buffer = new StringBuffer();

    /**
     * Appends the outermost referenced property name of each property access in the supplied
     * closure body, in source order.
     *
     * @param code the sort closure body to inspect
     */
    public void visit(Statement code) {
        AstQuery.from(code)
                .descendants(PropertyExpression.class)
                .notInto(PropertyExpression.class) // keep only the outermost name of a property chain
                .forEach(expression -> buffer.append(expression.getPropertyAsString()));
    }

    /**
     * Returns the SQL {@code ORDER BY} fragment built so far.
     *
     * @return the derived order-by fragment
     */
    public String getOrderBy() {
        return buffer.toString();
    }

}
