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
 * @see RecordBase
 * @since 4.0.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
public @interface RecordOptions {
    /**
     * Mode to use when creating record type classes.
     */
    RecordTypeMode mode() default RecordTypeMode.AUTO;

    /**
     * If {@code true}, this adds a method {@code getAt(int)} which given
     * an integer n, returns the n'th component in the record.
     * Example:
     * <pre class="groovyTestCase">
     * import static groovy.test.GroovyAssert.shouldFail
     *
     * record Point(int x, int y, String color) {}
     *
     * def p = new Point(100, 200, 'green')
     * assert p[0] == 100
     * assert p[1] == 200
     * assert p[2] == 'green'
     * shouldFail(IllegalArgumentException) {
     *     p[-1]
     * }
     *
     * // getAt also enables destructuring
     * def (x, y, c) = p
     * assert x == 100
     * assert y == 200
     * assert c == 'green'
     * </pre>
     *
     * If a method {@code getAt(int)} already exists in the class,
     * then this setting is ignored, and no additional method is generated.
     */
    boolean getAt() default true;

    /**
     * If {@code true}, this adds a method {@code toList()} to the record
     * which returns the record's components as a list.
     *
     * Example:
     * <pre class="groovyTestCase">
     * record Point(int x, int y, String color) {}
     * def p = new Point(100, 200, 'green')
     * assert p.toList() == [100, 200, 'green']
     * </pre>
     *
     * If a method {@code toList()} already exists in the class,
     * then this setting is ignored, and no additional method is generated.
     */
    boolean toList() default true;

    /**
     * If {@code true}, this adds a method {@code toMap()} to the record.
     *
     * Example:
     * <pre class="groovyTestCase">
     * record Point(int x, int y, String color) {}
     * def p = new Point(100, 200, 'green')
     * assert p.toMap() == [x:100, y:200, color:'green']
     * </pre>
     *
     * If a method {@code toMap()} already exists in the class,
     * then this setting is ignored, and no additional method is generated.
     */
    boolean toMap() default true;

    /**
     * If {@code true}, this adds a method {@code size()} to the record which returns the number of components.
     *
     * Example:
     * <pre class="groovyTestCase">
     * record Point(int x, int y, String color) {}
     * def p = new Point(100, 200, 'green')
     * assert p.size() == 3
     * </pre>
     *
     * If a method {@code size()} already exists in the class,
     * then this setting is ignored, and no additional method is generated.
     */
    boolean size() default true;

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
    boolean copyWith() default false;

    /**
     * If {@code true}, this adds a method {@code components()} to the record
     * which returns its components as a typed tuple {@code Tuple0}, {@code Tuple1}...
     *
     * Example:
     * <pre class="groovyTestCase">
     * import groovy.transform.*
     *
     * {@code @RecordOptions(components=true)}
     * record Point(int x, int y, String color) {}
     *
     * def (x, y, c) = new Point(100, 200, 'green').components()
     * assert x == 100
     * assert y.intdiv(2) == 100
     * assert c.toUpperCase() == 'GREEN'
     * </pre>
     *
     * The signature of the components method for this example is:
     * <pre>
     * Tuple3&lt;Integer, Integer, String> components()
     * </pre>
     * This is suitable for destructuring in {@code TypeChecked} scenarios.
     *
     * If a method {@code components()} already exists in the class,
     * then this setting is ignored, and no additional method is generated.
     * It is a compile-time error if there are more components in the record
     * than the largest TupleN class in the Groovy codebase.
     */
    boolean components() default false;
}
