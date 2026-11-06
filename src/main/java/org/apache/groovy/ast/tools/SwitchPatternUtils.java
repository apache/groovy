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
package org.apache.groovy.ast.tools;

import org.codehaus.groovy.ast.ASTNode;
import org.codehaus.groovy.ast.Parameter;
import org.codehaus.groovy.ast.expr.PatternExpression;
import org.codehaus.groovy.ast.expr.SwitchExpression;
import org.codehaus.groovy.ast.stmt.CaseStatement;
import org.codehaus.groovy.ast.stmt.SwitchStatement;

import java.util.List;

/**
 * Queries on the pattern arms of a {@code switch} (GEP-19). A pattern arm is a
 * {@link CaseStatement} whose label is a {@link PatternExpression}; once
 * lowered, its label tests the switch's synthetic subject variable, which the
 * bytecode writers bind to the evaluated selector.
 *
 * @see org.apache.groovy.ast.pattern.PatternLowering
 * @since 7.0.0
 */
public final class SwitchPatternUtils {

    private SwitchPatternUtils() {
    }

    /**
     * Returns the subject variable of a lowered pattern switch, or {@code null}
     * for a switch without pattern arms.
     *
     * @param switchNode a {@code SwitchExpression} or {@code SwitchStatement}
     */
    public static Parameter getSubjectVariable(final ASTNode switchNode) {
        List<CaseStatement> caseStatements = switchNode instanceof SwitchStatement statement ? statement.getCaseStatements()
                : switchNode instanceof SwitchExpression expression ? expression.getCaseStatements() : List.of();
        for (CaseStatement caseStatement : caseStatements) {
            if (caseStatement.getExpression() instanceof PatternExpression label) {
                return label.getSubject();
            }
        }
        return null;
    }

    /**
     * Returns the variable holding the selector of a lowered pattern switch as
     * a {@code List}, or {@code null} for a switch without list pattern arms.
     *
     * @param switchNode a {@code SwitchExpression} or {@code SwitchStatement}
     * @see PatternExpression#getElements()
     */
    public static Parameter getElementsVariable(final ASTNode switchNode) {
        List<CaseStatement> caseStatements = switchNode instanceof SwitchStatement statement ? statement.getCaseStatements()
                : switchNode instanceof SwitchExpression expression ? expression.getCaseStatements() : List.of();
        for (CaseStatement caseStatement : caseStatements) {
            if (caseStatement.getExpression() instanceof PatternExpression label) {
                return label.getElements();
            }
        }
        return null;
    }

    /**
     * Whether the case statement is a pattern arm.
     */
    public static boolean isPatternArm(final CaseStatement caseStatement) {
        return caseStatement.getExpression() instanceof PatternExpression;
    }
}
