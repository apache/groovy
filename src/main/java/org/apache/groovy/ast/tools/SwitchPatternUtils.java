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
import org.codehaus.groovy.ast.stmt.CaseStatement;

/**
 * Node metadata keys and accessors for structural pattern matching in
 * {@code switch} (GEP-19). The parser lowers a pattern switch to ordinary
 * AST plus this metadata:
 * <ul>
 * <li>the switch node carries {@link #SUBJECT_VARIABLE}, a synthetic
 *     {@link Parameter} that the bytecode writers bind to the evaluated
 *     selector, so that pattern tests and bindings can refer to it;</li>
 * <li>a pattern arm is a {@link CaseStatement} carrying {@link #PATTERN_ARM};
 *     its label is a boolean test on the subject variable (not an
 *     {@code isCase} operand) and its code starts with the matching steps,
 *     each of the form {@code if (!(check)) break <arm>}, where the arm
 *     label is the case statement's statement label and the break target
 *     is the next case test.</li>
 * </ul>
 *
 * @since 7.0.0
 */
public final class SwitchPatternUtils {

    /** On a switch node: the synthetic {@link Parameter} holding the selector value. */
    public static final String SUBJECT_VARIABLE = "_SWITCH_PATTERN_SUBJECT";
    /** On a {@link CaseStatement}: {@code Boolean.TRUE} marks a pattern arm. */
    public static final String PATTERN_ARM = "_SWITCH_PATTERN_ARM";
    /** On a pattern-generated {@code instanceof} expression: {@code Boolean.TRUE}. */
    public static final String PATTERN_TEST = "_SWITCH_PATTERN_TEST";
    /** On a pattern arm: the {@code ClassNode} tested by a type or record pattern. */
    public static final String PATTERN_HEAD_TYPE = "_SWITCH_PATTERN_TYPE";
    /** On a pattern arm: {@code Boolean.TRUE} when the arm is conditional (guard, record, list or map pattern). */
    public static final String PATTERN_GUARDED = "_SWITCH_PATTERN_GUARDED";
    /** On a pattern arm: the {@code Integer} arity of a record pattern. */
    public static final String PATTERN_RECORD = "_SWITCH_PATTERN_RECORD";
    /** On a pattern arm: {@code Boolean.TRUE} for a list pattern. */
    public static final String PATTERN_LIST = "_SWITCH_PATTERN_LIST";
    /** On a pattern arm: {@code Boolean.TRUE} for a map pattern. */
    public static final String PATTERN_MAP = "_SWITCH_PATTERN_MAP";

    private SwitchPatternUtils() {
    }

    /**
     * Returns the subject variable of a pattern switch, or {@code null} for a
     * switch without pattern arms.
     *
     * @param switchNode a {@code SwitchExpression} or {@code SwitchStatement}
     */
    public static Parameter getSubjectVariable(final ASTNode switchNode) {
        return switchNode.getNodeMetaData(SUBJECT_VARIABLE);
    }

    /**
     * Whether the case statement is a pattern arm.
     */
    public static boolean isPatternArm(final CaseStatement caseStatement) {
        return Boolean.TRUE.equals(caseStatement.getNodeMetaData(PATTERN_ARM));
    }
}
