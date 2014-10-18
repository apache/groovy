/*
 * Copyright 2003-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.codehaus.groovy.macro.matcher

import groovy.transform.CompileStatic
import groovy.transform.PackageScope

/**
 * Represents constraints in AST pattern matching.
 *
 * @author Cedric Champeau
 * @since 2.4.0
 */
@CompileStatic
class MatchingConstraints {
    final Set<String> placeholders = new LinkedHashSet<>()

    @PackageScope static class Builder {
        private final MatchingConstraints constraints = new MatchingConstraints()

        MatchingConstraints build(@DelegatesTo(value=Builder, strategy=Closure.DELEGATE_ONLY) Closure spec) {
            def clone = (Closure) spec.clone()
            clone.delegate = this
            clone.resolveStrategy = Closure.DELEGATE_ONLY
            clone()

            constraints
        }

        def getProperty(String name) {
            if ('constraints'==name) {
                return constraints
            }
            name
        }

        Builder placeholder(String... names) {
            names.each { String it -> constraints.placeholders.add(it) }
            this
        }
    }
}
