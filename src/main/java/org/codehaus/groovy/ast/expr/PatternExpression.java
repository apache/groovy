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

import org.apache.groovy.ast.pattern.MatchPattern;
import org.apache.groovy.ast.pattern.PatternLowering;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;

/**
 * A pattern match (GEP-19): either the label of a pattern arm of a
 * {@code switch}, which tests the switch subject, or {@code source instanceof
 * pattern}. It keeps the {@link MatchPattern} through every compiler phase, so that
 * semantic analysis and the type checker work from what was written rather than
 * from the code that implements it.
 * <p>
 * Before semantic analysis, {@link PatternLowering} derives that code: the
 * {@linkplain #getLowered() lowered} expression, and for a switch arm the
 * matching steps at the start of the arm. Visitors that have no interest in
 * patterns see the lowered expression, through the default
 * {@link GroovyCodeVisitor#visitPatternExpression}. Code that builds an AST
 * programmatically creates the pattern and leaves the lowering to the compiler.
 *
 * @see PatternLowering
 * @since 7.0.0
 */
@Incubating
public class PatternExpression extends Expression {

    private final MatchPattern pattern;
    private final Expression guard;
    private Expression source;
    private Parameter subject;
    private Expression lowered;

    /**
     * Creates the label of a pattern arm, with an optional {@code when} guard.
     *
     * @param pattern the pattern
     * @param guard   the guard, or {@code null}
     */
    public PatternExpression(final MatchPattern pattern, final Expression guard) {
        this(pattern, guard, null);
    }

    /**
     * Creates {@code source instanceof pattern}.
     *
     * @param source  the value being matched
     * @param pattern the pattern
     */
    public static PatternExpression instanceOf(final Expression source, final MatchPattern pattern) {
        return new PatternExpression(pattern, null, source);
    }

    private PatternExpression(final MatchPattern pattern, final Expression guard, final Expression source) {
        this.pattern = pattern;
        this.guard = guard;
        this.source = source;
        setType(ClassHelper.boolean_TYPE);
    }

    public MatchPattern getPattern() {
        return pattern;
    }

    /** The {@code when} guard of a pattern arm, or {@code null}. */
    public Expression getGuard() {
        return guard;
    }

    public boolean hasGuard() {
        return guard != null;
    }

    /** Whether this is {@code source instanceof pattern} rather than the label of a switch arm. */
    public boolean isInstanceOf() {
        return source != null;
    }

    /**
     * The value matched by {@code instanceof}, or {@code null} for the label of
     * a switch arm, which matches the switch {@linkplain #getSubject() subject}.
     * Once lowered, it is the operand of the first test of the lowered expression.
     */
    public Expression getSource() {
        return lowered != null && source != null ? PatternLowering.sourceOf(lowered) : source;
    }

    /** The variable holding the selector value of the enclosing switch, for the label of a switch arm. */
    public Parameter getSubject() {
        return subject;
    }

    public void setSubject(final Parameter subject) {
        this.subject = subject;
    }

    /**
     * The boolean expression that implements the match: for {@code instanceof},
     * the whole match; for a switch arm, the test of the head type, with the
     * remaining steps at the start of the arm. {@code null} until lowered.
     */
    public Expression getLowered() {
        return lowered;
    }

    public void setLowered(final Expression lowered) {
        this.lowered = lowered;
    }

    @Override
    public void visit(final GroovyCodeVisitor visitor) {
        visitor.visitPatternExpression(this);
    }

    @Override
    public Expression transformExpression(final ExpressionTransformer transformer) {
        PatternExpression ret;
        if (lowered != null) { // the source and the guard are part of the lowered code
            ret = new PatternExpression(pattern, guard, source);
            ret.lowered = transformer.transform(lowered);
        } else {
            ret = new PatternExpression(pattern,
                    guard != null ? transformer.transform(guard) : null,
                    source != null ? transformer.transform(source) : null);
        }
        ret.subject = subject;
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        String text = pattern.toString();
        return source != null ? source.getText() + " instanceof " + text : text;
    }
}
