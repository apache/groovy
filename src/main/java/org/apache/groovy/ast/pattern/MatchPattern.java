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

import java.util.List;

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
 * The types a pattern names are the {@link ClassNode}s of the source, which
 * resolution completes in place, so the questions that need a resolved type
 * ({@link #isTotal()}, {@link #coverageType()}, {@link #isUnconditionalFor})
 * are only meaningful after semantic analysis.
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
     * {@code T name}: matches a value of type {@code T} (the wrapper type for a
     * primitive {@code T}) and binds it, or only tests it when {@code name} is
     * {@code null} ({@code T _}).
     */
    record TypePattern(ClassNode type, String name) implements MatchPattern {
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
    }

    /**
     * {@code R(p1, ..., pn)}: matches a value of type {@code R} whose components
     * match the component patterns, by position.
     */
    record RecordPattern(ClassNode type, List<MatchPattern> components) implements MatchPattern {
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
    }

    /**
     * {@code [p1, ..., pn]}: matches a {@code List}, array or re-iterable
     * {@code Iterable} whose elements match the element patterns, with at most
     * one {@link RestPattern} standing for any number of elements.
     */
    record ListPattern(List<MatchPattern> elements) implements MatchPattern {
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
    }

    /**
     * {@code [k1: p1, ..., kn: pn]}: matches a {@code Map} that has each key
     * with a value matching its pattern; other entries are allowed, and are
     * bound as a new map by {@code rest} when it names a variable.
     */
    record MapPattern(List<Entry> entries, RestPattern rest) implements MatchPattern {
        /** One {@code key: pattern} entry; a {@link WildcardPattern} only requires the key. */
        public record Entry(Object key, MatchPattern value) {
        }

        /** {@code [:]} matches only an empty map; a map pattern is never total. */
        @Override
        public boolean isTotal() {
            return false;
        }
    }

    /** {@code var name} or {@code def name}: matches any value, including {@code null}, and binds it. */
    record BindingPattern(String name) implements MatchPattern {
        @Override
        public boolean isTotal() {
            return true;
        }
    }

    /** {@code _}: matches any value, including {@code null}, without binding it. */
    record WildcardPattern() implements MatchPattern {
        @Override
        public boolean isTotal() {
            return true;
        }
    }

    /** A literal element or entry value, matched by equality. */
    record ConstantPattern(Expression value) implements MatchPattern {
        @Override
        public boolean isTotal() {
            return false;
        }
    }

    /**
     * {@code ...}, {@code var... name} or {@code T... name} in a list pattern
     * (the remaining elements, each tested against {@code T} when given), and
     * {@code ...} or {@code var... name} in a map pattern (the remaining entries).
     */
    record RestPattern(ClassNode type, String name) implements MatchPattern {
        @Override
        public boolean isTotal() {
            return type == null;
        }
    }
}
