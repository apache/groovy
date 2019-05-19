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

import groovy.transform.options.ImmutablePropertyHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation used to assist in the creation of immutable classes.
 * Defines any known immutable properties (or fields) or known immutable classes.
 *
 * @see Immutable
 * @see ImmutablePropertyHandler
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface ImmutableOptions {
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
     */
    String[] knownImmutables() default {};
}
