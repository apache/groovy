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
package groovy.contracts;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Declares the <b>frame condition</b> for a method: the set of fields and parameters
 * that the method is allowed to modify. All other state is implicitly declared unchanged.
 * <p>
 * The closure value lists the modifiable targets as field references ({@code this.fieldName})
 * or parameter references ({@code paramName}). Multiple targets can be specified using a list:
 * <pre>
 *   &#064;Modifies({ this.items })
 *   void addItem(Item item) { ... }
 *
 *   &#064;Modifies({ [this.items, this.count] })
 *   void addAndCount(Item item) { ... }
 * </pre>
 * <p>
 * Multiple {@code @Modifies} annotations can also be used (via {@link Repeatable}):
 * <pre>
 *   &#064;Modifies({ this.items })
 *   &#064;Modifies({ this.count })
 *   void addAndCount(Item item) { ... }
 * </pre>
 * <p>
 * When both {@code @Modifies} and {@link Ensures} are present on a method,
 * the {@code old} variable in the postcondition may only reference fields
 * declared in {@code @Modifies}. A compile error is reported otherwise.
 * <p>
 * {@code @Modifies} does not generate runtime assertion code. It serves as
 * a specification for humans and tools (including AI) to reason about method behavior.
 *
 * @since 6.0.0
 * @see Ensures
 * @see Requires
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD})
@Incubating
@Repeatable(ModifiesConditions.class)
@GroovyASTTransformationClass({
        "org.apache.groovy.contracts.ast.ModifiesASTTransformation",
        "org.apache.groovy.contracts.ast.ModifiesEnsuresValidationTransformation"
})
public @interface Modifies {
    /**
     * Returns the closure class that lists the locations the annotated member may mutate.
     *
     * @return the generated closure class backing the frame-condition expression
     */
    Class value();
}
