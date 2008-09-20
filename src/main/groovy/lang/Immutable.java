/*
 * Copyright 2008 the original author or authors.
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

package groovy.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation used for making a class immutable.
 * </p>
 * It allows you to write code snippets like this:
 * <pre>
 * {@code @Immutable} final class Customer {
 *     String first, last
 *     int age
 *     Date since
 *     Collection favItems
 * }
 * def d = new Date()
 * def c1 = new Customer(first:'Tom', last:'Jones', age:21, since:d, favItems:['Books', 'Games'])
 * def c2 = new Customer(first:'Tom', last:'Jones', age:21, since:d, favItems:['Books', 'Games'])
 * assert c1 == c2
 * </pre>
 * A class created in this way has the following characteristics:
 * <ul>
 * <li>Properties automatically have private, final backing fields with getters.
 * Attempts to update the property will result in a {@code ReadOnlyPropertyException}.
 * <li>An {@code equals} method and a {@code hashCode} method are provided based on the property values.
 * <li>{@code Date}s, {@code Cloneable}s and arrays are defensively copied on the way in (constructor) and out (getters).
 * Arrays and cloneable objects use the {@code clone} method. It is up to you to define this method and use deep cloning if appropriate.
 * <li>{@code Collection}s and {@code Map}s are wrapped by immutable wrapper classes (but not deeply cloned!).
 * Attempts to update them will result in an {@code UnsupportedOperationException}.
 * <li>Fields that are enums or other {@code @Immutable} classes are allowed but for an
 * otherwise possible mutable property type, an error is thrown.
 * <li>You don't have to follow Groovy's normal property conventions, e.g. you can create an explicit private field and
 * then you can write explicit get and set methods. Such an approach, isn't currently prohibited (to give you some
 * wiggle room to get around these conventions) but any fields created in this way are deemed not to be part of the
 * significant state of the object and aren't factored into the {@code equals} or {@code hashCode} methods.
 * Use at your own risk!
 * </ul>
 * </p>
 * Such classes are particularly useful for functional and concurrent styles of programming
 * and for use as key values within maps.
 *
 * @author Paul King
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ImmutableASTTransformation")
public @interface Immutable {
}