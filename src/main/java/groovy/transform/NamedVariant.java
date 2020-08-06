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

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Allows construction of a named-arg equivalent method or constructor.
 * The method or constructor will have at least a first argument of type
 * {@code Map} and may have more arguments. As such, it can be called
 * using Groovy's named-arg syntax. The original method/constructor is retained
 * and is called by the generated method/constructor.
 *
 * One benefit of this approach is the potential for improved type checking.
 * The annotated "tuple" method/constructor can be type rich and will be checked
 * as such during normal compilation. The generated method/constructor using
 * the map argument will be named-argument friendly but the map also hides
 * type information. The generated method however contains no business logic
 * so the chance of errors is minimal.
 *
 * Any arguments identified as named arguments will be supplied as part of the map.
 * Any additional arguments are supplied in the normal tuple style.
 *
 * Named parameters are identified in one of three ways:
 * <ol>
 *     <li>Use one or more {@code @NamedParam} annotations to explicitly identify such parameters</li>
 *     <li>Use one or more {@code @NamedDelegate} annotations to explicitly identify such parameters as
 *     delegate parameters</li>
 *     <li>If no parameters with {@code @NamedParam} or {@code @NamedDelegate} annotations are found then:
 *     <ul>
 *         <li>If {@code autoDelegate} is false (the default), all parameters are treated as if they were named parameters</li>
 *         <li>If {@code autoDelegate} is true, the first parameters is treated as if it was a delegate parameter</li>
 *     </ul>
 *     </li>
 * </ol>
 * You can also mix and match the {@code @NamedParam} and {@code @NamedDelegate} annotations.
 *
 * Named arguments will be supplied via the map with their property name (configurable via
 * annotation attributes within {@code @NamedParam}) being the key and value being the argument value.
 * For named delegates, any properties of the delegate can become map keys.
 * Duplicate keys across delegate properties or named parameters are not allowed.
 * The type of delegate parameters must be compatible with Groovy's {@code as} cast operation from a {@code Map}.
 *
 * Here is an example using implicit named parameters.
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 *
 * {@code @NamedVariant}
 * int makeSense(int dollars, int cents) {
 *     100 * dollars + cents
 * }
 *
 * assert makeSense(dollars: 2, cents: 50) == 250
 * </pre>
 * Here is an example using a delegate parameter.
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 *
 * {@code @ToString(includeNames=true)}
 * class Color {
 *     Integer r, g, b
 * }
 *
 * {@code @NamedVariant}
 * String foo(@NamedDelegate Color shade) {
 *     shade
 * }
 *
 * def result = foo(g: 12, b: 42, r: 12)
 * assert result.toString() == 'Color(r:12, g:12, b:42)'
 * </pre>
 * You could also explicitly annotate the {@code shade} argument with the {@code @NamedDelegate} annotation if you wanted.
 *
 * The generated method will be something like this:
 * <pre>
 * String foo(Map args) {
 *     return foo(args as Color)
 * }
 * </pre>
 * The generated method/constructor retains the visibility and return type of the original method/constructor
 * but the {@link VisibilityOptions} annotation can be added to customize the visibility. You could have the
 * annotated method/constructor private for instance but have the generated one be public.
 *
 * @see VisibilityOptions
 * @see NamedParam
 * @see NamedDelegate
 * @since 2.5.0
 */
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD, ElementType.CONSTRUCTOR})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.NamedVariantASTTransformation")
public @interface NamedVariant {
    /**
     * If specified, must match the optional "id" attribute in an applicable {@code VisibilityOptions} annotation.
     */
    String visibilityId() default Undefined.STRING;

    /**
     * If true, add an implicit @NamedDelegate to the first parameter if no @NamedDelegate or @NamedParam annotations are found on any parameter.
     *
     * @since 2.5.3
     */
    boolean autoDelegate() default false;

    /**
     * If true, will use {@code as} to convert map parameter to required class
     */
    boolean coerce() default false;
}
