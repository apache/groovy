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

class MatchingConstraintsBuilder {
    Set<String> placeholders = new LinkedHashSet<>()
    Set<String> varargPlaceholders = new LinkedHashSet<>()
    ConstraintPredicate<Token> tokenPredicate
    ConstraintPredicate<TreeContext> eventually


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

    def propertyMissing(String name) {
        name
    }

    MatchingConstraintsBuilder placeholder(String... names) {
        names.each { String it -> placeholders.add(it) }
        this
    }

    MatchingConstraintsBuilder varargPlaceholder(String... names) {
        names.each { String it -> varargPlaceholders.add(it) }
        this
    }

    MatchingConstraintsBuilder anyToken() {
        tokenPredicate = MatchingConstraints.ANY_TOKEN
        this
    }

    MatchingConstraintsBuilder token(@DelegatesTo(value=Token, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> predicate) {
        def clone = (Closure<Boolean>) predicate.clone()
        clone.resolveStrategy = Closure.DELEGATE_FIRST
        tokenPredicate = new ConstraintPredicate<Token>() {
            @Override
            boolean apply(final Token a) {
                clone.delegate = a
                clone.call(a)
            }
        }
        this
    }

    MatchingConstraintsBuilder eventually(@DelegatesTo(value=TreeContext, strategy = Closure.DELEGATE_FIRST) Closure<Boolean> predicate) {
        def clone = (Closure<Boolean>) predicate.clone()
        clone.resolveStrategy = Closure.DELEGATE_FIRST
        eventually = new ConstraintPredicate<TreeContext>() {
            @Override
            boolean apply(final TreeContext a) {
                clone.delegate = a
                clone.call(a)
            }
        }
        this
    }
}
