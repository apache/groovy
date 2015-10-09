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
package org.codehaus.groovy.macro.runtime;

import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.expr.ArgumentListExpression;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.ConstructorCallExpression;
import org.codehaus.groovy.ast.expr.Expression;

/**
 *
 * @author Sergei Egorov <bsideup@gmail.com>
 */
public class MacroSubstitutionKey {

    private int startLine;
    private int startColumn;
    private int endLine;
    private int endColumn;

    public MacroSubstitutionKey(int startLine, int startColumn, int endLine, int endColumn) {
        this.startLine = startLine;
        this.startColumn = startColumn;
        this.endLine = endLine;
        this.endColumn = endColumn;
    }

    public MacroSubstitutionKey(Expression expression, int linesOffset, int columnsOffset) {
        this(
                expression.getLineNumber() - linesOffset,
                expression.getColumnNumber() - (expression.getLineNumber() == linesOffset ? columnsOffset : 0),
                expression.getLastLineNumber() - linesOffset,
                expression.getLastColumnNumber() - (expression.getLastLineNumber() == linesOffset ? columnsOffset : 0)
        );
    }

    public ConstructorCallExpression toConstructorCallExpression() {
        return new ConstructorCallExpression(
                ClassHelper.make(this.getClass()),
                new ArgumentListExpression(new Expression[] {
                        new ConstantExpression(startLine),
                        new ConstantExpression(startColumn),
                        new ConstantExpression(endLine),
                        new ConstantExpression(endColumn)
                })
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MacroSubstitutionKey that = (MacroSubstitutionKey) o;

        if (endColumn != that.endColumn) return false;
        if (endLine != that.endLine) return false;
        if (startColumn != that.startColumn) return false;
        if (startLine != that.startLine) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = startLine;
        result = 31 * result + startColumn;
        result = 31 * result + endLine;
        result = 31 * result + endColumn;
        return result;
    }

    @Override
    public String toString() {
        return "SubstitutionKey{" +
                "startLine=" + startLine +
                ", startColumn=" + startColumn +
                ", endLine=" + endLine +
                ", endColumn=" + endColumn +
                '}';
    }
}
