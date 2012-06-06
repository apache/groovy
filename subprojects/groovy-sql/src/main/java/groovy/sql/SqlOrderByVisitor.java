/*
 * Copyright 2003-2008 the original author or authors.
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
package groovy.sql;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.PropertyExpression;
import org.codehaus.groovy.ast.stmt.ReturnStatement;

/**
 * @author Paul King
 * @version $Revision: 6714 $
 */
public class SqlOrderByVisitor extends CodeVisitorSupport {

    private StringBuffer buffer = new StringBuffer();

    public String getOrderBy() {
        return buffer.toString();
    }

    public void visitReturnStatement(ReturnStatement statement) {
        statement.getExpression().visit(this);
    }

    public void visitPropertyExpression(PropertyExpression expression) {
        buffer.append(expression.getPropertyAsString());
    }


}
