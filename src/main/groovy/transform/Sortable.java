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
package groovy.transform;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.annotation.ElementType;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

/**
 * A class annotation used to make a class Comparable by multiple Comparators.
 *
 * As an example, given this class:
 * <pre>
 * {@code @Sortable} class Person {
 *     String first
 *     String last
 *     Integer born
 * }
 * </pre>
 * The generated Groovy class will:
 * <ul>
 *     <li>implement the {@code Comparable} interface</li>
 *     <li>have a {@code compareTo} method based on the {@code first},
 *     {@code last} and {@code born} properties (priority ordering will be according
 *     to the ordering of property definition, highest first, unless 'includes' is used; in which case,
 *     priority will be according to the order given in the includes list)</li>
 *     <li>have three {@code Comparator} methods named {@code comparatorByFirst},
 *     {@code comparatorByLast} and {@code comparatorByBorn}</li>
 * </ul>
 * The properties within the class must themselves be {@code Comparable} or {@code @Sortable}.
 *
 * @author Andres Almiray
 * @author Paul King
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.SortableASTTransformation")
public @interface Sortable {
    /**
     * Property names to include in the comparison algorithm.
     * Must not be used if 'excludes' is used.
     */
    String[] includes() default {};

    /**
     * Property names to exclude in the comparison algorithm.
     * Must not be used if 'includes' is used.
     */
    String[] excludes() default {};
}
