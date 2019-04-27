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

package org.codehaus.groovy.ast.expr;

import org.codehaus.groovy.ast.AstToTextHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.stmt.Statement;

/**
 * Represents a lambda expression such as one of these:
 * <pre>
 * {@code
 * e -> e * 2
 * (x, y) -> x + y
 * (x, y) -> { x + y }
 * (int x, int y) -> { x + y }
 * }
 * </pre>
 */
public class LambdaExpression extends ClosureExpression {
    public LambdaExpression(Parameter[] parameters, Statement code) {
        super(parameters, code);
    }

    public void visit(GroovyCodeVisitor visitor) {
        visitor.visitLambdaExpression(this);
    }

    @Override
    public String getText() {
        String paramText = AstToTextHelper.getParametersText(this.getParameters());
        if (paramText.length() > 0) {
            return "(" + paramText + ") -> { ... }";
        } else {
            return "() -> { ... }";
        }
    }
}
