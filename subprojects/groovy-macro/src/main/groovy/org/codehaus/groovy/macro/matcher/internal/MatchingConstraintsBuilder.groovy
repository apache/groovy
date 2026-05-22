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
package org.codehaus.groovy.macro.matcher.internal

import org.codehaus.groovy.macro.matcher.MatchingConstraints
import org.codehaus.groovy.macro.matcher.TreeContext
import org.codehaus.groovy.syntax.Token

/**
 * Builder for {@link MatchingConstraints} instances.
 *
 * @since 2.5.0
 */
class MatchingConstraintsBuilder {
    /**
     * Placeholder names accepted during matching.
     */
    Set<String> placeholders = new LinkedHashSet<>()
    /**
     * Placeholder names that can absorb multiple arguments.
     *
     * @since 5.0.0
     */
    Set<String> varargPlaceholders = new LinkedHashSet<>()
    /**
     * Predicate used to compare tokens.
     */
    ConstraintPredicate<Token> tokenPredicate
    /**
     * Predicate that must match somewhere in the current tree context path.
     */
    ConstraintPredicate<TreeContext> eventually


    /**
     * Builds constraints from the supplied builder closure.
     *
     * @param spec the builder closure
     * @return the built constraints
     */
    MatchingConstraints build(@DelegatesTo(value=MatchingConstraintsBuilder, strategy=Closure.DELEGATE_ONLY) Closure spec) {
        def clone = (Closure) spec.clone()
        clone.delegate = this
        clone.resolveStrategy = Closure.DELEGATE_ONLY
        clone()

        new MatchingConstraints(
                placeholders: Collections.unmodifiableSet(placeholders),
                varargPlaceholders: Collections.unmodifiableSet(varargPlaceholders),
                tokenPredicate: tokenPredicate,
                eventually: eventually
        )
    }

    /**
     * Treats unknown property names as placeholder identifiers.
     *
     * @param name the missing property name
     * @return {@code name}
     */
    def propertyMissing(String name) {
        name
    }

    /**
     * Registers one or more placeholder names.
     *
     * @param names the placeholder names
     * @return this builder
     */
    MatchingConstraintsBuilder placeholder(String... names) {
        names.each { String it -> placeholders.add(it) }
        this
    }

    /**
     * Registers one or more vararg placeholder names.
     *
     * @param names the vararg placeholder names
     * @return this builder
     * @since 5.0.0
     */
    MatchingConstraintsBuilder varargPlaceholder(String... names) {
        names.each { String it -> varargPlaceholders.add(it) }
        this
    }

    /**
     * Configures token matching to accept any token.
     *
     * @return this builder
     */
    MatchingConstraintsBuilder anyToken() {
        tokenPredicate = MatchingConstraints.ANY_TOKEN
        this
    }

    /**
     * Configures a token predicate.
     *
     * @param predicate the token predicate closure
     * @return this builder
     */
    MatchingConstraintsBuilder token(@DelegatesTo(value=Token, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> predicate) {
        def clone = (Closure<Boolean>) predicate.clone()
        clone.resolveStrategy = Closure.DELEGATE_FIRST
        tokenPredicate = new ConstraintPredicate<Token>() {
            /** Evaluates the configured token predicate closure. */
            @Override
            boolean apply(final Token a) {
                clone.delegate = a
                clone.call(a)
            }
        }
        this
    }

    /**
     * Configures a predicate that must match an enclosing tree context.
     *
     * @param predicate the tree-context predicate closure
     * @return this builder
     */
    MatchingConstraintsBuilder eventually(@DelegatesTo(value=TreeContext, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> predicate) {
        def clone = (Closure<Boolean>) predicate.clone()
        clone.resolveStrategy = Closure.DELEGATE_FIRST
        eventually = new ConstraintPredicate<TreeContext>() {
            /** Evaluates the configured tree-context predicate closure. */
            @Override
            boolean apply(final TreeContext a) {
                clone.delegate = a
                clone.call(a)
            }
        }
        this
    }
}
