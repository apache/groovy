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
package groovy.transform

import groovy.transform.options.ImmutablePropertyHandler
import groovy.transform.stc.POJO
import org.apache.groovy.lang.annotation.Incubating

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Meta annotation used when defining record-like classes.
 * <p>
 * It allows you to write classes in this shortened form:
 *
 * <pre class="groovyTestCase">
 * {@code @groovy.transform.RecordType}
 * class Cyclist {
 *     String firstName
 *     String lastName
 * }
 *
 * def richie = new Cyclist('Richie', 'Porte')
 * assert richie.toString() =~ /Cyclist.*firstName.*Richie/
 * </pre>
 *
 * The {@code @RecordType} meta-annotation corresponds to adding the following annotations:
 * {@link RecordBase},
 * {@link ToString},
 * {@link EqualsAndHashCode},
 * {@link ImmutableOptions},
 * {@link PropertyOptions},
 * {@link TupleConstructor},
 * {@link MapConstructor} and
 * {@link KnownImmutable}.
 *
 * Together these annotations instruct the compiler to execute the necessary transformations to add
 * the necessary getters, constructors, equals, hashCode and other helper methods that are typically
 * written when creating record-like classes with the defined properties.
 * <p>
 * A class created in this way has the following characteristics:
 * <ul>
 * <li>The class is automatically made final
 * <li>The serialVersionUID is by default 0
 * <li>Properties automatically have private, final backing fields with getters which are the same name as the fields.
 * <li>A map-based constructor is provided which allows you to set properties by name.
 * <li>A tuple-style constructor is provided which allows you to set properties in the same order as they are defined.
 * <li>Default {@code equals}, {@code hashCode} and {@code toString} methods are provided based on the property values.
 * </ul>
 * Record-like classes are particularly useful for data structures.
 *
 * @see ToString
 * @see EqualsAndHashCode
 * @see RecordBase
 * @see ImmutableOptions
 * @see PropertyOptions
 * @see TupleConstructor
 * @see MapConstructor
 * @see KnownImmutable*
 * @since 4.0.0
 */
@RecordBase
@ToString(cache = true, includeNames = true)
@EqualsAndHashCode(cache = true, useCanEqual = false)
@ImmutableOptions
@PropertyOptions(propertyHandler = ImmutablePropertyHandler)
@TupleConstructor(defaults = false)
@MapConstructor
@KnownImmutable
@POJO
@AnnotationCollector(mode = AnnotationCollectorMode.PREFER_EXPLICIT_MERGED)
@Retention(RetentionPolicy.RUNTIME)
@Target([ElementType.TYPE])
@Incubating
@interface RecordType {
}
