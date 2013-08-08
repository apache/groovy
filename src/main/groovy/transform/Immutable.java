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
 * Class annotation used to assist in the creation of immutable classes.
 * <p>
 * It allows you to write classes in this shortened form:
 * <pre>
 * {@code @Immutable} class Customer {
 *     String first, last
 *     int age
 *     Date since
 *     Collection favItems
 * }
 * def d = new Date()
 * def c1 = new Customer(first:'Tom', last:'Jones', age:21, since:d, favItems:['Books', 'Games'])
 * def c2 = new Customer('Tom', 'Jones', 21, d, ['Books', 'Games'])
 * assert c1 == c2
 * </pre>
 * The {@code @Immutable} annotation instructs the compiler to execute an
 * AST transformation which adds the necessary getters, constructors,
 * equals, hashCode and other helper methods that are typically written
 * when creating immutable classes with the defined properties.
 * <p>
 * A class created in this way has the following characteristics:
 * <ul>
 * <li>The class is automatically made final.
 * <li>Properties must be of an immutable type or a type with a strategy for handling non-immutable
 * characteristics. Specifically, the type must be one of the primitive or wrapper types, Strings, enums,
 * other {@code @Immutable} classes or known immutables (e.g. java.awt.Color, java.net.URI, java.util.UUID).
 * Also handled are Cloneable classes, collections, maps and arrays, and other "effectively immutable"
 * classes with special handling (e.g. java.util.Date).
 * <li>Properties automatically have private, final backing fields with getters.
 * Attempts to update the property will result in a {@code ReadOnlyPropertyException}.
 * <li>A map-based constructor is provided which allows you to set properties by name.
 * <li>A tuple-style constructor is provided which allows you to set properties in the same order as they are defined.
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
 * <li>{@code Date}s, {@code Cloneable}s and arrays are defensively copied on the way in (constructor) and out (getters).
 * Arrays and {@code Cloneable} objects use the {@code clone} method. For your own classes,
 * it is up to you to define this method and use deep cloning if appropriate.
 * <li>{@code Collection}s and {@code Map}s are wrapped by immutable wrapper classes (but not deeply cloned!).
 * Attempts to update them will result in an {@code UnsupportedOperationException}.
 * <li>Fields that are enums or other {@code @Immutable} classes are allowed but for an
 * otherwise possible mutable property type, an error is thrown.
 * <li>You don't have to follow Groovy's normal property conventions, e.g. you can create an explicit private field and
 * then you can write explicit get and set methods. Such an approach, isn't currently prohibited (to give you some
 * wiggle room to get around these conventions) but any fields created in this way are deemed not to be part of the
 * significant state of the object and aren't factored into the {@code equals} or {@code hashCode} methods.
 * Similarly, you may use static properties (though usually this is discouraged) and these too will be ignored
 * as far as significant state is concerned. If you do break standard conventions, you do so at your own risk and
 * your objects may no longer be immutable. It is up to you to ensure that your objects remain immutable at least
 * to the extent expected in other parts of your program!
 * </ul>
 * Immutable classes are particularly useful for functional and concurrent styles of programming
 * and for use as key values within maps. If you want similar functionality to what this annotation
 * provides but don't need immutability then consider using {@code @Canonical}.
 * <p>
 * Customising behaviour:
 * <p>
 * You can customise the toString() method provided for you by {@code @Immutable}
 * by also adding the {@code @ToString} annotation to your class definition.
 * <p>
 * Limitations:
 * <ul>
 * <li>
 * As outlined above, Arrays and {@code Cloneable} objects use the {@code clone} method. For your own classes,
 * it is up to you to define this method and use deep cloning if appropriate.
 * </li>
 * <li>
 * As outlined above, {@code Collection}s and {@code Map}s are wrapped by immutable wrapper classes (but not deeply cloned!).
 * </li>
 * <li>
 * Currently {@code BigInteger} and {@code BigDecimal} are deemed immutable but see:
 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6348370
 * </li>
 * <li>
 * {@code java.awt.Color} is treated as "effectively immutable" but is not final so while not normally used with child
 * classes, it isn't strictly immutable. Use at your own risk.
 * </li>
 * <li>
 * {@code java.util.Date} is treated as "effectively immutable" but is not final so it isn't strictly immutable.
 * Use at your own risk.
 * </li>
 * <li>
 * Groovy's normal map-style naming conventions will not be available if the first property
 * has type {@code LinkedHashMap} or if there is a single Map, AbstractMap or HashMap property.
 * </li>
 * </ul>
 *
 * @author Paul King
 * @author Andre Steingress
 * @see groovy.transform.ToString
 * @see groovy.transform.Canonical
 * @since 1.7
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ImmutableASTTransformation")
public @interface Immutable {
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
}
