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

import org.codehaus.groovy.ast.ClassNode;

import java.util.LinkedList;
import java.util.List;

/**
 * Represents class expression with pattern variable.
 * <p>
 * <pre><code>
 *     // for `someVar instanceof Person(String name, int age) p`,
 *     // the structure of `PatternClassExpression` is shown as follows:
 *     PatternClassExpression("p", Person).destructedPatternClassExpressionList =
 *             [PatternClassExpression("name", String), PatternClassExpression("age", int)]
 *
 * </code></pre>
 *
 * @since 5.0.0
 */
public class PatternClassExpression extends ClassExpression {
    private static final VariableExpression DUMMY = new VariableExpression("<dummy>");
    private final List<PatternClassExpression> destructedPatternClassExpressionList = new LinkedList<>();

    private final VariableExpression patternVariable;

    public PatternClassExpression(VariableExpression patternVariable, ClassNode type) {
        super(type);
        this.patternVariable = patternVariable;
    }

    public PatternClassExpression(ClassNode type) {
        this(DUMMY, type);
    }

    public boolean isDummy() {
        return DUMMY == patternVariable;
    }

    public void addDestructedPatternClassExpression(PatternClassExpression destructedPatternClassExpression) {
        destructedPatternClassExpressionList.add(destructedPatternClassExpression);
    }

    public List<PatternClassExpression> getDestructedPatternClassExpressionList() {
        return destructedPatternClassExpressionList;
    }

    @Override
    public String toString() {
        return super.toString() + "[patternType: " + getType().getName() + (isDummy() ? "" : " " + patternVariable.getName()) + "]";
    }
}
