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
 * Class annotation used to assist in the creation of tuple constructors in classes.
 * <p>
 * It allows you to write classes in this shortened form:
 * <pre>
 * {@code @TupleConstructor} class Customer {
 *     String first, last
 *     int age
 *     Date since
 *     Collection favItems
 * }
 * def c1 = new Customer(first:'Tom', last:'Jones', age:21, since:new Date(), favItems:['Books', 'Games'])
 * def c2 = new Customer('Tom', 'Jones', 21, new Date(), ['Books', 'Games'])
 * def c3 = new Customer('Tom', 'Jones')
 * </pre>
 * The {@code @TupleConstructor} annotation instructs the compiler to execute an
 * AST transformation which adds the necessary constructor method to your class.
 * <p>
 * A tuple constructor is created with a parameter for each property (and optionally field and
 * super properties).
 * A default value is provided (using Java's default values) for all parameters in the constructor.
 * Groovy's normal conventions then allows any number of parameters to be left off the end of the parameter list
 * including all of the parameters - giving a no-arg constructor which can be used with the map-style naming conventions.
 * <p>
 * The order of parameters is given by the properties of any super classes with most super first
 * (if {@code includeSuperProperties} is set) followed by the properties of the class followed
 * by the fields of the class (if {@code includeFields} is set). Within each grouping the order
 * is as attributes appear within the respective class.
 * <p>
 * Limitations:
 * <ul>
 * <li>Groovy's normal map-style naming conventions will not be available if the first property (or field)
 * has type {@code LinkedHashMap} or if there is a single Map, AbstractMap or HashMap property (or field)</li>
 * </ul>
 *
 * @author Paul King
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.TupleConstructorASTTransformation")
public @interface TupleConstructor {
    /**
     * List of field and/or property names to exclude from the constructor.
     * Must not be used if 'includes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] excludes() default {};

    /**
     * List of field and/or property names to include within the constructor.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] includes() default {};

    /**
     * Include fields in the constructor.
     */
    boolean includeFields() default false;

    /**
     * Include properties in the constructor.
     */
    boolean includeProperties() default true;

    /**
     * Include fields from super classes in the constructor.
     */
    boolean includeSuperFields() default false;

    /**
     * Include properties from super classes in the constructor.
     */
    boolean includeSuperProperties() default false;

    /**
     * Should super properties be called within a call to the parent constructor.
     * rather than set as properties
     */
    boolean callSuper() default false;

    /**
     * By default, this annotation becomes a no-op if you provide your own constructor.
     * By setting {@code force=true} then the tuple constructor(s) will be added regardless of
     * whether existing constructors exist. It is up to you to avoid creating duplicate constructors.
     */
    boolean force() default false;
}
