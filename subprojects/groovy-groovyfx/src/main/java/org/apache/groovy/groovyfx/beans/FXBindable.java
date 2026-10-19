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
package org.apache.groovy.groovyfx.beans;

import org.apache.groovy.lang.annotation.Incubating;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Turns a Groovy property into a JavaFX property.
 * <p>
 * When annotating a property, the property is backed by a lazily created
 * JavaFX {@code Property} whose type follows the declared type of the
 * Groovy property: {@code boolean}, {@code int}, {@code long},
 * {@code float}, {@code double} and {@code String} (and their wrapper
 * types) map to the corresponding primitive JavaFX property types,
 * {@code ObservableList}, {@code ObservableMap} and {@code ObservableSet}
 * map to the collection property types, and everything else maps to an
 * {@code ObjectProperty}.
 * <p>
 * When annotating a class, every non-static, non-final Groovy property in
 * that class is treated as though it carried the annotation.
 * <p>
 * For a property {@code name} of type {@code T} the generated members are:
 * <pre>
 * T getName()
 * void setName(T value)
 * TProperty nameProperty()
 * </pre>
 * The following class:
 * <pre>
 * class Person {
 *     &#064;FXBindable String name
 *     &#064;FXBindable int age = 21
 * }
 * </pre>
 * behaves like this hand-written JavaFX bean:
 * <pre>
 * class Person {
 *     private StringProperty name$property
 *     private IntegerProperty age$property
 *     private String name
 *     private int age = 21
 *
 *     String getName() { name$property == null ? name : name$property.get() }
 *     void setName(String value) { if (name$property == null) name = value else name$property.set(value) }
 *     StringProperty nameProperty() {
 *         if (name$property == null) name$property = new SimpleStringProperty(this, 'name', name)
 *         name$property
 *     }
 *     // and likewise for age
 * }
 * </pre>
 * The property object is only created when {@code nameProperty()} is first
 * called, so a bean that is never bound or listened to costs no more than a
 * plain Groovy bean.
 * <p>
 * It is a compilation error to annotate a static or final property, a field
 * that is not a property, a property whose type is already a JavaFX
 * {@code Property}, or a property for which a setter or a
 * {@code nameProperty()} method is declared explicitly.
 */
@Documented
@Incubating
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD, ElementType.TYPE})
@GroovyASTTransformationClass({
        "org.apache.groovy.groovyfx.beans.FXBindableASTStubber",
        "org.apache.groovy.groovyfx.beans.FXBindableASTTransformation"
})
public @interface FXBindable {
}
