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
 * <p>
 * Custom property handling:
 * <ul>
 * <li>The {@code @ImmutableBase} annotation supports customization using {@code @PropertyOptions}
 * which allows a custom property handler to be defined. This is most typically used behind the scenes
 * by the {@code @Immutable} meta-annotation but you can also define your own handler. If a custom
 * handler is present, it will determine the code generated for the getters and setters of any property.</li>
 * </ul>
 *
 * @see Immutable
 * @see ImmutableOptions
 * @see MapConstructor
 * @see TupleConstructor
 * @see PropertyOptions
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ImmutableASTTransformation")
public @interface ImmutableBase {
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
     */
    boolean copyWith() default false;
}
