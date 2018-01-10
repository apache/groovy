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

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation used to assist in the creation of immutable classes.
 * Checks on the validity of an immutable class and makes some preliminary changes to the class.
 * Usually used via the {@code @Immutable} meta annotation.
 *
 * @see Immutable
 * @see MapConstructor
 * @see TupleConstructor
 * @since 2.5
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ImmutableASTTransformation")
public @interface ImmutableBase {
    /**
     * Allows you to provide {@code @Immutable} with a list of classes which
     * are deemed immutable. By supplying a class in this list, you are vouching
     * for its immutability and {@code @Immutable} will do no further checks.
     * Example:
     * <pre>
     * import groovy.transform.*
     * {@code @Immutable}(knownImmutableClasses = [Address])
     * class Person {
     *     String first, last
     *     Address address
     * }
     *
     * {@code @TupleConstructor}
     * class Address {
     *     final String street
     * }
     * </pre>
     *
     * @since 1.8.7
     */
    Class[] knownImmutableClasses() default {};

    /**
     * Allows you to provide {@code @Immutable} with a list of property names which
     * are deemed immutable. By supplying a property's name in this list, you are vouching
     * for its immutability and {@code @Immutable} will do no further checks.
     * Example:
     * <pre>
     * {@code @groovy.transform.Immutable}(knownImmutables = ['address'])
     * class Person {
     *     String first, last
     *     Address address
     * }
     * ...
     * </pre>
     *
     * @since 2.1.0
     */
    String[] knownImmutables() default {};

    /**
     * If {@code true}, this adds a method {@code copyWith} which takes a Map of
     * new property values and returns a new instance of the Immutable class with
     * these values set.
     * Example:
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.Immutable}(copyWith = true)
     * class Person {
     *     String first, last
     * }
     *
     * def tim   = new Person( 'tim', 'yates' )
     * def alice = tim.copyWith( first:'alice' )
     *
     * assert tim.first   == 'tim'
     * assert alice.first == 'alice'
     * </pre>
     * Unknown keys in the map are ignored, and if the values would not change
     * the object, then the original object is returned.
     *
     * If a method called {@code copyWith} that takes a single parameter already
     * exists in the class, then this setting is ignored, and no method is generated.
     *
     * @since 2.2.0
     */
    boolean copyWith() default false;
}
