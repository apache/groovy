/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */
package groovy.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Class annotation used to provide default dummy methods for a class extending an abstract super class or
 * implementing one or more interfaces.
 * <p>
 * Example usage:
 * <pre>
 * import groovy.transform.AutoImplement
 *
 * {@code @AutoImplement}
 * class EmptyStringIterator implements Iterator<String> {
 *     boolean hasNext() { false }
 * }
 *
 * assert !new EmptyStringIterator().hasNext()
 * </pre>
 * In the above example, since {@code hasNext} returns false, the {@code next} method
 * should never be called, so any dummy implementation would do for {@code next}.
 * The "empty" implementation provided by default when using {@code @AutoImplement}
 * will suffice - which effectively returns {@code null} in Groovy for non-void,
 * non-primitive methods.
 *
 * As a point of interest, the default implementation for methods returning primitive
 * types is to return the default value (which incidentally never satisfies Groovy truth).
 * For {@code boolean} this means returning {@code false}, so for the above example we
 * could have (albeit perhaps less instructive of our intent) by just using:
 * <pre>
 * {@code @AutoImplement}
 * class EmptyStringIterator implements Iterator<String> { }
 * </pre>
 * If we didn't want to assume that callers of our {@code EmptyStringIterator} correctly followed
 * the {@code Iterator} contract, then we might want to guard against inappropriate calls to {@code next}.
 * Rather than just returning {@code null}, we might want to throw an exception. This is easily done using
 * the {@code exception} annotation attribute as shown below:
 * <pre>
 * import groovy.transform.AutoImplement
 * import static groovy.test.GroovyAssert.shouldFail
 *
 * {@code @AutoImplement}(exception=UnsupportedOperationException)
 * class EmptyStringIterator implements Iterator<String> {
 *     boolean hasNext() { false }
 * }
 *
 * shouldFail(UnsupportedOperationException) {
 *     new EmptyStringIterator().next()
 * }
 * </pre>
 * All implemented methods will throw an instance of this exception constructed using its no-arg constructor.
 *
 * You can also supply a single {@code message} annotation attribute in which case the message will be passed
 * as an argument during exception construction as shown in the following example:
 * <pre>
 * {@code @AutoImplement}(exception=UnsupportedOperationException, message='Not supported for this empty iterator')
 * class EmptyStringIterator implements Iterator<String> {
 *     boolean hasNext() { false }
 * }
 *
 * def ex = shouldFail(UnsupportedOperationException) {
 *     new EmptyStringIterator().next()
 * }
 * assert ex.message == 'Not supported for this empty iterator'
 * </pre>
 * Finally, you can alternatively supply a {@code code} annotation attribute in which case a closure
 * block can be supplied which should contain the code to execute for all implemented methods. This can be
 * seen in the following example:
 * <pre>
 * {@code @AutoImplement}(code = { throw new UnsupportedOperationException("Not supported for ${getClass().simpleName}") })
 * class EmptyStringIterator implements Iterator<String> {
 *     boolean hasNext() { false }
 * }
 *
 * def ex = shouldFail(UnsupportedOperationException) {
 *     new EmptyStringIterator().next()
 * }
 * assert ex.message == 'Not supported for EmptyStringIterator'
 * </pre>
 *
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.AutoImplementASTTransformation")
public @interface AutoImplement {
    /**
     * If defined, all unimplemented methods will throw this exception.
     * Will be ignored if {@code code} is defined.
     */
    Class<? extends RuntimeException> exception() default Undefined.EXCEPTION.class;

    /**
     * If {@code exception} is defined, {@code message} can be used to specify the exception message.
     * Will be ignored if {@code code} is defined or {@code exception} isn't defined.
     */
    String message() default Undefined.STRING;

    /**
     * If defined, all unimplemented methods will execute the code found within the supplied closure.
     */
    Class code() default Undefined.CLASS.class;
}
