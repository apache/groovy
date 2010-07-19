/*
 * Copyright 2008-2010 the original author or authors.
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
 * Class annotation used to assist in creating appropriate {@code equals()} and {@code hashCode()} methods.
 * <p/>
 * It allows you to write classes in this shortened form:
 * <pre>
 * import groovy.transform.EqualsAndHashCode
 * {@code @EqualsAndHashCode}
 * class Person {
 *     String first, last
 *     int age
 * }
 * def p1 = new Person(first:'John', last:'Smith', age:21)
 * def p2 = new Person(first:'John', last:'Smith', age:21)
 * assert p1 == p2
 * def map = [:]
 * map[p1] = 45
 * assert map[p2] == 45
 * </pre>
 * The {@code @EqualsAndHashCode} annotation instructs the compiler to execute an
 * AST transformation which adds the necessary equals and hashCode methods to the class.
 * <p/>
 * The {@code hashCode()} method is calculated using Groovy's {@code HashCodeHelper} class
 * which implements an algorithm similar to the outlined in the book <em>Effective Java</em>.
 * <p/>
 * The {@code equals()} method compares the values of the individual properties of the class.
 *
 * @see org.codehaus.groovy.util.HashCodeHelper
 * @author Paul King
 * @since 1.8.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.EqualsAndHashCodeASTTransformation")
public @interface EqualsAndHashCode {
    /**
     * Comma separated list of field and property names to exclude from equals and hashCode calculations
     */
    String excludes() default "";

    /**
     * Whether to include super in equals and hashCode calculations
     */
    boolean callSuper() default false;

    /**
     * Include fields as well as properties in equals and hashCode calculations
     */
    boolean includeFields() default false;
}