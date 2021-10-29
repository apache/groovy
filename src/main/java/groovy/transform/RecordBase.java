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
 * Class annotation used to assist in the creation of record-like classes.
 *
 * @see ImmutableOptions
 * @see MapConstructor
 * @see TupleConstructor
 * @see PropertyOptions
 * @since 4.0.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.RecordTypeASTTransformation")
public @interface RecordBase {
    /**
     * Mode to use when creating record type classes.
     */
    RecordTypeMode mode() default RecordTypeMode.AUTO;

    /**
     * If {@code true}, this adds a method {@code copyWith} which takes a Map of
     * new property values and returns a new instance of the record class with
     * these values set.
     * Example:
     * <pre class="groovyTestCase">
     * {@code @groovy.transform.RecordType}(copyWith = true)
     * class Person {
     *     String first, last
     * }
     *
     * def tim   = new Person('tim', 'yates')
     * def alice = tim.copyWith(first:'alice')
     *
     * assert tim.toString() == 'Person[first=tim, last=yates]'
     * assert alice.toString() == 'Person[first=alice, last=yates]'
     * </pre>
     * Unknown keys in the map are ignored, and if the values would not change
     * the object, then the original object is returned.
     *
     * If a method called {@code copyWith} that takes a single parameter already
     * exists in the class, then this setting is ignored, and no method is generated.
     */
    boolean copyWith() default false;}
