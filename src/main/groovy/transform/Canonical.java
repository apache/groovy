/*
 * Copyright 2008-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation used to assist in the creation of mutable classes.
 * <p>
 * It allows you to write classes in this shortened form:
 * <pre>
 * {@code @Canonical} class Customer {
 *     String first, last
 *     int age
 *     Date since
 *     Collection favItems = ['Food']
 *     def object 
 * }
 * def d = new Date()
 * def anyObject = new Object()
 * def c1 = new Customer(first:'Tom', last:'Jones', age:21, since:d, favItems:['Books', 'Games'], object: anyObject)
 * def c2 = new Customer('Tom', 'Jones', 21, d, ['Books', 'Games'], anyObject)
 * assert c1 == c2
 * </pre>
 *
 * If you set the autoDefaults flag to true, you don't need to provide all arguments in constructors calls,
 * in this case all properties not present are initialized to the default value, e.g.:
 * <pre>
 * def c3 = new Customer(last: 'Jones', age: 21)
 * def c4 = new Customer('Tom', 'Jones')
 * 
 * assert null == c3.since
 * assert 0 == c4.age
 * assert c3.favItems == ['Food'] && c4.favItems == ['Food']
 * </pre>
 *
 * The {@code @Canonical} annotation instructs the compiler to execute an
 * AST transformation which adds positional constructors,
 * equals, hashCode and a pretty print toString to your class. There are additional
 * annotations if you only need some of the functionality: {@code @EqualsAndHashCode},
 * {@code @ToString} and {@code @TupleConstructor}. In addition, you can add one of
 * the other annotations if you need to further customize the behavior of the
 * AST transformation.
 * <p>
 * A class created in this way has the following characteristics:
 * <ul>
 * <li>A no-arg constructor is provided which allows you to set properties by name using Groovy's normal bean conventions.
 * <li>Tuple-style constructors are provided which allow you to set properties in the same order as they are defined.
 * <li>Default {@code equals}, {@code hashCode} and {@code toString} methods are provided based on the property values.
 * Though not normally required, you may write your own implementations of these methods. For {@code equals} and {@code hashCode},
 * if you do write your own method, it is up to you to obey the general contract for {@code equals} methods and supply
 * a corresponding matching {@code hashCode} method.
 * If you do provide one of these methods explicitly, the default implementation will be made available in a private
 * "underscore" variant which you can call. E.g., you could provide a (not very elegant) multi-line formatted
 * {@code toString} method for {@code Customer} above as follows:
 * <pre>
 *     String toString() {
 *        _toString().replaceAll(/\(/, '(\n\t').replaceAll(/\)/, '\n)').replaceAll(/, /, '\n\t')
 *    }
 * </pre>
 * If an "underscore" version of the respective method already exists, then no default implementation is provided.
 * </ul>
 * <p>
 * If you want similar functionality to what this annotation provides but also require immutability, see the
 * {@code @}{@link Immutable} annotation.
 * <p>
 * Limitations:
 * <ul>
 * <li>If you explicitly add your own constructors, then the transformation will not add any other constructor to the class</li>
 * <li>Groovy's normal map-style naming conventions will not be available if the first property
 * has type {@code LinkedHashMap} or if there is a single Map, AbstractMap or HashMap property</li>
 * </ul>
 *
 * @author Paulo Poiati
 * @author Paul King
 * @see groovy.transform.EqualsAndHashCode
 * @see groovy.transform.ToString
 * @see groovy.transform.TupleConstructor
 * @see groovy.transform.Immutable
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.CanonicalASTTransformation")
public @interface Canonical {
    /**
     * List of field and/or property names to exclude.
     * Must not be used if 'includes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     *
     * If the {@code @Canonical} behavior is customised by using it in conjunction with one of the more specific
     * related annotations (i.e. {@code @ToString}, {@code @EqualsAndHashCode} or {@code @TupleConstructor}), then
     * the value of this attribute can be overriden within the more specific annotation.
     */
    String[] excludes() default {};

    /**
     * List of field and/or property names to include.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     *
     * If the {@code @Canonical} behavior is customised by using it in conjunction with one of the more specific
     * related annotations (i.e. {@code @ToString}, {@code @EqualsAndHashCode} or {@code @TupleConstructor}), then
     * the value of this attribute can be overriden within the more specific annotation.
     */
    String[] includes() default {};
}
