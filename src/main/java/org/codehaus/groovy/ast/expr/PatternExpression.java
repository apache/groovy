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
import org.apache.groovy.ast.pattern.MatchPattern.ConstantPattern;
import org.apache.groovy.ast.pattern.MatchPattern.TypePattern;
import org.apache.groovy.ast.pattern.PatternLowering;
import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.GroovyCodeVisitor;
import org.codehaus.groovy.ast.Parameter;

import java.util.List;

/**
 * A pattern match (GEP-19): either the label of a pattern arm of a
 * {@code switch}, which tests the switch subject, or {@code source instanceof
 * pattern}, including the type pattern {@code source instanceof T name}.
 * <p>
 * The node holds what was written: the {@link MatchPattern}, the {@code when}
 * guard of an arm, and the matched value of {@code instanceof}. None of these
 * can be changed; a different match is a new node, which is what
 * {@link #transformExpression} builds. The variables the pattern
 * {@linkplain #getBindings() binds} are declared by the pattern itself, so
 * references to them in the guard or the arm resolve to the pattern's
 * {@link VariableExpression}s from semantic analysis on.
 * <p>
 * The code that implements the match is derived from the pattern by
 * {@link PatternLowering}, the first operation of instruction selection: until
 * then, resolution, scoping, AST transforms, compilation customizers and tools
 * see only the pattern; from then on, the type checker and code generation see
 * the {@linkplain #getLowered() lowered} code, which a visitor without an
 * interest in patterns is given by the default
 * {@link GroovyCodeVisitor#visitPatternExpression}.
 *
 * @see PatternLowering
 * @since 7.0.0
 */
@Incubating
public class PatternExpression extends Expression {

    private final MatchPattern pattern;
    private final Expression guard;
    private final Expression source;
    private Parameter subject;
    private Parameter elements;
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
     * Creates {@code source instanceof pattern}. An {@code instanceof} test
     * cannot bind a primitive, so a type pattern with a primitive type binds
     * its wrapper type here, where a switch arm binds the primitive.
     *
     * @param source  the value being matched
     * @param pattern the pattern
     */
    public static PatternExpression instanceOf(final Expression source, final MatchPattern pattern) {
        return new PatternExpression(pattern.map(PatternExpression::wrapPrimitiveBinding), null, source);
    }

    private static MatchPattern wrapPrimitiveBinding(final MatchPattern pattern) {
        if (pattern instanceof TypePattern typed && typed.variable() != null && ClassHelper.isPrimitiveType(typed.variable().getOriginType())) {
            VariableExpression wrapped = MatchPattern.variable(typed.name(), ClassHelper.getWrapper(typed.type()));
            wrapped.setSourcePosition(typed.variable());
            return new TypePattern(typed.type(), wrapped);
        }
        return pattern;
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

    /**
     * The {@code when} guard of a pattern arm as written, or {@code null}.
     * Once lowered, the guard is evaluated by the matching steps at the start
     * of the arm.
     */
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

    /**
     * The variables the pattern declares, in source order; see
     * {@link MatchPattern#bindings()}. A reference to one of them in the
     * guard or in the arm has it as its accessed variable.
     */
    public List<VariableExpression> getBindings() {
        return pattern.bindings();
    }

    /**
     * The variable holding the selector value of the enclosing switch, for the
     * label of a switch arm; set by lowering, and shared by every pattern arm
     * of the switch.
     */
    public Parameter getSubject() {
        return subject;
    }

    public void setSubject(final Parameter subject) {
        this.subject = subject;
    }

    /**
     * The variable holding the selector of the enclosing switch as a
     * {@code List}, for a switch with a list pattern arm, so that an array or
     * {@code Iterable} selector is materialised once for all of its list
     * patterns; set by lowering, and shared by every pattern arm of the switch.
     * {@code null} for a switch without list pattern arms.
     */
    public Parameter getElements() {
        return elements;
    }

    public void setElements(final Parameter elements) {
        this.elements = elements;
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
            MatchPattern transformed = pattern.map(leaf -> {
                if (leaf instanceof ConstantPattern constant) {
                    Expression value = transformer.transform(constant.value());
                    if (value != constant.value()) return new ConstantPattern(value);
                }
                return leaf;
            });
            ret = new PatternExpression(transformed,
                    guard != null ? transformer.transform(guard) : null,
                    source != null ? transformer.transform(source) : null);
        }
        ret.subject = subject;
        ret.elements = elements;
        ret.setSourcePosition(this);
        ret.copyNodeMetaData(this);
        return ret;
    }

    @Override
    public String getText() {
        String text = pattern.getText();
        if (source != null) return source.getText() + " instanceof " + text;
        return guard != null ? text + " when " + guard.getText() : text;
    }
}
