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
package org.apache.groovy.ast.pattern;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.ast.ClassHelper;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.RecordComponentNode;
import org.codehaus.groovy.ast.expr.Expression;
import org.codehaus.groovy.ast.expr.VariableExpression;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

import static org.codehaus.groovy.ast.ClassHelper.getWrapper;
import static org.codehaus.groovy.ast.ClassHelper.isPrimitiveType;
import static org.codehaus.groovy.transform.stc.StaticTypeCheckingSupport.implementsInterfaceOrIsSubclassOf;

/**
 * A pattern of structural pattern matching (GEP-19), as written in a
 * {@code case} label or on the right of {@code instanceof}. The parser builds
 * it, {@link PatternLowering} derives the matching code from it, and the type
 * checker reads it; what a pattern means is decided here, once, rather than by
 * each of those phases.
 * <p>
 * A pattern is an immutable tree of records. The two kinds of node it shares
 * with the rest of the AST are mutable in the way the AST is: the types a
 * pattern names are the {@link ClassNode}s of the source, which resolution
 * completes in place, so the questions that need a resolved type
 * ({@link #isTotal()}, {@link #coverageType()}, {@link #isUnconditionalFor})
 * are only meaningful after semantic analysis; and the variables a pattern
 * {@linkplain #bindings() binds} are the {@link VariableExpression}s that
 * declare them, which scoping and type checking annotate and which references
 * in a guard or an arm resolve to. A pattern with different leaves is a new
 * pattern, made with {@link #map}.
 *
 * @since 7.0.0
 */
@Incubating
public sealed interface MatchPattern {

    /**
     * The type a value is tested against before any component is examined: the
     * type of a type or record pattern, or {@code null} for a pattern without
     * one, such as a list or map pattern.
     */
    default ClassNode headType() {
        return null;
    }

    /**
     * Whether the pattern matches every value of its head type (every value at
     * all for a pattern without one), so that only the head type test can fail.
     */
    boolean isTotal();

    /**
     * The type every non-null value of which the pattern matches, or
     * {@code null} when the pattern can fail for a value of its head type.
     * This is what a pattern contributes to the coverage of a switch.
     */
    default ClassNode coverageType() {
        return null;
    }

    /**
     * Whether the pattern matches every non-null value of the given type.
     */
    default boolean isUnconditionalFor(final ClassNode selectorType) {
        ClassNode coverage = coverageType();
        return coverage != null && implementsInterfaceOrIsSubclassOf(getWrapper(selectorType), coverage);
    }

    /**
     * The pattern in the form it is written in source, such as
     * {@code Point(int x, var y)}, with type names as they are at the time.
     */
    String getText();

    /**
     * Visits this pattern and then, in source order, every pattern nested in it.
     */
    default void forEach(final Consumer<? super MatchPattern> action) {
        action.accept(this);
    }

    /**
     * Returns the pattern with every leaf replaced by what {@code leaf} returns
     * for it; a composite pattern is rebuilt around its mapped components, and
     * then passed to {@code leaf} itself. The pattern is unchanged if
     * {@code leaf} returns every pattern it is given.
     */
    default MatchPattern map(final UnaryOperator<MatchPattern> leaf) {
        return leaf.apply(this);
    }

    /**
     * The variables the pattern declares, in source order: those of its type
     * patterns, {@code var}/{@code def} bindings and named rest patterns, at
     * any depth.
     */
    default List<VariableExpression> bindings() {
        List<VariableExpression> bindings = new ArrayList<>();
        forEach(pattern -> {
            VariableExpression variable = pattern instanceof TypePattern typed ? typed.variable()
                    : pattern instanceof BindingPattern binding ? binding.variable()
                    : pattern instanceof RestPattern rest ? rest.variable() : null;
            if (variable != null) bindings.add(variable);
        });
        return bindings;
    }

    /**
     * The expressions the pattern contains, in source order: the values of its
     * {@linkplain ConstantPattern constant patterns}, at any depth. These are
     * ordinary expressions that resolution, scoping and transforms process.
     */
    default List<Expression> expressions() {
        List<Expression> expressions = new ArrayList<>();
        forEach(pattern -> {
            if (pattern instanceof ConstantPattern constant) expressions.add(constant.value());
        });
        return expressions;
    }

    /**
     * Declares the variable a pattern binds: {@code name} with the given type.
     * A variable's {@linkplain VariableExpression#getAccessedVariable accessed
     * variable} is itself, as for any declaring occurrence.
     */
    static VariableExpression variable(final String name, final ClassNode type) {
        VariableExpression variable = new VariableExpression(name, type);
        variable.setAccessedVariable(variable);
        return variable;
    }

    /**
     * {@code T name}: matches a value of type {@code T} (the wrapper type for a
     * primitive {@code T}) and binds it, or only tests it when {@code variable}
     * is {@code null} ({@code T _}). The variable is declared with the pattern
     * type, so that a primitive type pattern binds a primitive.
     */
    record TypePattern(ClassNode type, VariableExpression variable) implements MatchPattern {
        /** {@code T name}, declaring {@code name} with type {@code T}; a {@code null} name binds nothing. */
        public TypePattern(final ClassNode type, final String name) {
            this(type, name == null ? null : MatchPattern.variable(name, type));
        }

        /** The name of the bound variable, or {@code null} for {@code T _}. */
        public String name() {
            return variable == null ? null : variable.getName();
        }

        @Override
        public ClassNode headType() {
            return type;
        }

        @Override
        public boolean isTotal() {
            return true;
        }

        @Override
        public ClassNode coverageType() {
            return getWrapper(type);
        }

        @Override
        public String getText() {
            return type.toString(false) + " " + (variable == null ? "_" : variable.getName());
        }
    }

    /**
     * {@code R(p1, ..., pn)}: matches a value of type {@code R} whose components
     * match the component patterns, by position.
     */
    record RecordPattern(ClassNode type, List<MatchPattern> components) implements MatchPattern {
        public RecordPattern {
            components = List.copyOf(components);
        }

        @Override
        public ClassNode headType() {
            return type;
        }

        /**
         * Whether {@code type} declares record components, so that the arity
         * is known at compile time (a {@code toList()} deconstruction is not).
         */
        public boolean isRecord() {
            return type.isRecord() || !type.getRecordComponents().isEmpty();
        }

        /**
         * A record pattern matches every instance of its record when the arity
         * agrees and each component pattern matches every value the component
         * can hold: a {@code var}/{@code def} binding or {@code _} always does,
         * and a type pattern does when the record component is a primitive of
         * that type, which cannot be {@code null}. A type pattern on a
         * reference-typed component does not match a {@code null} component,
         * and a nested pattern can fail, so neither is total.
         */
        @Override
        public boolean isTotal() {
            if (!isRecord()) return false;
            List<RecordComponentNode> declared = type.getRecordComponents();
            if (declared.size() != components.size()) return false;
            for (int i = 0, n = components.size(); i < n; i += 1) {
                MatchPattern component = components.get(i);
                if (component instanceof BindingPattern || component instanceof WildcardPattern) continue;
                ClassNode declaredType = declared.get(i).getType();
                if (component instanceof TypePattern typePattern && isPrimitiveType(declaredType)
                        && getWrapper(declaredType).equals(getWrapper(typePattern.type()))) continue;
                return false;
            }
            return true;
        }

        @Override
        public ClassNode coverageType() {
            return isTotal() ? type : null;
        }

        @Override
        public String getText() {
            return type.toString(false) + MatchPattern.join(components, "(", ")");
        }

        @Override
        public void forEach(final Consumer<? super MatchPattern> action) {
            action.accept(this);
            for (MatchPattern component : components) component.forEach(action);
        }

        @Override
        public MatchPattern map(final UnaryOperator<MatchPattern> leaf) {
            List<MatchPattern> mapped = MatchPattern.mapAll(components, leaf);
            return leaf.apply(mapped == components ? this : new RecordPattern(type, mapped));
        }
    }

    /**
     * {@code [p1, ..., pn]}: matches a {@code List}, array or re-iterable
     * {@code Iterable} whose elements match the element patterns, with at most
     * one {@link RestPattern} standing for any number of elements.
     */
    record ListPattern(List<MatchPattern> elements) implements MatchPattern {
        public ListPattern {
            elements = List.copyOf(elements);
        }

        /** Whether the pattern is {@code [...]} or {@code [var... xs]}, matching every list. */
        @Override
        public boolean isTotal() {
            return elements.size() == 1 && elements.get(0) instanceof RestPattern rest && rest.type() == null;
        }

        @Override
        public ClassNode coverageType() {
            return isTotal() ? ClassHelper.ITERABLE_TYPE : null;
        }

        @Override
        public boolean isUnconditionalFor(final ClassNode selectorType) {
            return isTotal() && (selectorType.isArray() || MatchPattern.super.isUnconditionalFor(selectorType));
        }

        /** The index of the rest element, or {@code -1}. */
        public int restIndex() {
            for (int i = 0, n = elements.size(); i < n; i += 1) {
                if (elements.get(i) instanceof RestPattern) return i;
            }
            return -1;
        }

        @Override
        public String getText() {
            return MatchPattern.join(elements, "[", "]");
        }

        @Override
        public void forEach(final Consumer<? super MatchPattern> action) {
            action.accept(this);
            for (MatchPattern element : elements) element.forEach(action);
        }

        @Override
        public MatchPattern map(final UnaryOperator<MatchPattern> leaf) {
            List<MatchPattern> mapped = MatchPattern.mapAll(elements, leaf);
            return leaf.apply(mapped == elements ? this : new ListPattern(mapped));
        }
    }

    /**
     * {@code [k1: p1, ..., kn: pn]}: matches a {@code Map} that has each key
     * with a value matching its pattern; other entries are allowed, and are
     * bound as a new map by {@code rest} when it names a variable.
     */
    record MapPattern(List<Entry> entries, RestPattern rest) implements MatchPattern {
        public MapPattern {
            entries = List.copyOf(entries);
        }

        /** One {@code key: pattern} entry; a {@link WildcardPattern} only requires the key. */
        public record Entry(Object key, MatchPattern value) {
        }

        /** {@code [:]} matches only an empty map; a map pattern is never total. */
        @Override
        public boolean isTotal() {
            return false;
        }

        @Override
        public String getText() {
            if (entries.isEmpty() && rest == null) return "[:]";
            StringBuilder text = new StringBuilder("[");
            for (Entry entry : entries) {
                if (text.length() > 1) text.append(", ");
                text.append(entry.key() instanceof String key && key.matches("[A-Za-z_$][A-Za-z0-9_$]*") ? key : entry.key());
                text.append(": ").append(entry.value().getText());
            }
            if (rest != null) {
                if (text.length() > 1) text.append(", ");
                text.append(rest.getText());
            }
            return text.append("]").toString();
        }

        @Override
        public void forEach(final Consumer<? super MatchPattern> action) {
            action.accept(this);
            for (Entry entry : entries) entry.value().forEach(action);
            if (rest != null) rest.forEach(action);
        }

        @Override
        public MatchPattern map(final UnaryOperator<MatchPattern> leaf) {
            List<Entry> mappedEntries = null;
            for (int i = 0, n = entries.size(); i < n; i += 1) {
                Entry entry = entries.get(i);
                MatchPattern value = entry.value().map(leaf);
                if (mappedEntries == null && value != entry.value()) {
                    mappedEntries = new ArrayList<>(entries.subList(0, i));
                }
                if (mappedEntries != null) mappedEntries.add(value == entry.value() ? entry : new Entry(entry.key(), value));
            }
            MatchPattern mappedRest = rest == null ? null : rest.map(leaf);
            boolean unchanged = mappedEntries == null && mappedRest == rest;
            return leaf.apply(unchanged ? this : new MapPattern(mappedEntries == null ? entries : mappedEntries, (RestPattern) mappedRest));
        }
    }

    /** {@code var name} or {@code def name}: matches any value, including {@code null}, and binds it. */
    record BindingPattern(VariableExpression variable) implements MatchPattern {
        /** {@code var name}, declaring {@code name} without a type. */
        public BindingPattern(final String name) {
            this(MatchPattern.variable(name, ClassHelper.dynamicType()));
        }

        /** The name of the bound variable. */
        public String name() {
            return variable.getName();
        }

        @Override
        public boolean isTotal() {
            return true;
        }

        @Override
        public String getText() {
            return "var " + variable.getName();
        }
    }

    /** {@code _}: matches any value, including {@code null}, without binding it. */
    record WildcardPattern() implements MatchPattern {
        @Override
        public boolean isTotal() {
            return true;
        }

        @Override
        public String getText() {
            return "_";
        }
    }

    /** A literal element or entry value, matched by equality. */
    record ConstantPattern(Expression value) implements MatchPattern {
        @Override
        public boolean isTotal() {
            return false;
        }

        @Override
        public String getText() {
            return value.getText();
        }
    }

    /**
     * {@code ...}, {@code var... name} or {@code T... name} in a list pattern
     * (the remaining elements, each tested against {@code T} when given), and
     * {@code ...} or {@code var... name} in a map pattern (the remaining entries).
     * The variable, when there is one, is declared without a type and holds a
     * new {@code List} or {@code Map} of the rest.
     */
    record RestPattern(ClassNode type, VariableExpression variable) implements MatchPattern {
        /** A rest pattern binding {@code name}, or binding nothing for a {@code null} name. */
        public RestPattern(final ClassNode type, final String name) {
            this(type, name == null ? null : MatchPattern.variable(name, ClassHelper.dynamicType()));
        }

        /** The name of the bound variable, or {@code null} for a bare {@code ...}. */
        public String name() {
            return variable == null ? null : variable.getName();
        }

        @Override
        public boolean isTotal() {
            return type == null;
        }

        @Override
        public String getText() {
            String prefix = type == null ? (variable == null ? "" : "var") : type.toString(false);
            return prefix + "..." + (variable == null ? "" : " " + variable.getName());
        }
    }

    //--------------------------------------------------------------------------

    private static String join(final List<MatchPattern> patterns, final String open, final String close) {
        StringBuilder text = new StringBuilder(open);
        for (MatchPattern pattern : patterns) {
            if (text.length() > open.length()) text.append(", ");
            text.append(pattern.getText());
        }
        return text.append(close).toString();
    }

    /** Maps each pattern, returning {@code patterns} itself when none changed. */
    private static List<MatchPattern> mapAll(final List<MatchPattern> patterns, final UnaryOperator<MatchPattern> leaf) {
        List<MatchPattern> mapped = null;
        for (int i = 0, n = patterns.size(); i < n; i += 1) {
            MatchPattern pattern = patterns.get(i), result = pattern.map(leaf);
            if (mapped == null && result != pattern) {
                mapped = new ArrayList<>(patterns.subList(0, i));
            }
            if (mapped != null) mapped.add(result);
        }
        return mapped == null ? patterns : mapped;
    }
}
