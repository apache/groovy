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
 * Class annotation to make constructors from a super class available in a sub class.
 * Should be used with care with other annotations which create constructors - see "Known
 * Limitations" for more details.
 * <p>
 * {@code @InheritConstructors} saves you typing some boilerplate code.
 * <p>
 * <em>Example usage:</em>
 * <pre class="groovyTestCase">
 * class Person {
 *     String first, last
 *     Person(String first, String last) {
 *         this.first = first
 *         this.last = last.toUpperCase()
 *     }
 * }
 *
 * {@code @groovy.transform.InheritConstructors}
 * class PersonAge extends Person {
 *     int age
 * }
 *
 * def js = new PersonAge('John', 'Smith')
 * js.age = 25
 * 
 * assert "$js.last, $js.first is $js.age years old" == 'SMITH, John is 25 years old'
 * </pre>
 * for this case, the <code>PersonAge</code> class will be
 * equivalent to the following code:
 * <pre>
 * class PersonAge extends Person {
 *     PersonAge(String first, String last) {
 *         super(first, last)
 *     }
 *     int age
 * }
 * </pre>
 * You may add additional constructors in addition to inherited ones.
 * If the argument types of a supplied constructor exactly match those
 * of a parent constructor, then that constructor won't be inherited.
 * <p>
 * <em>Style note:</em> Don't go overboard using this annotation.
 * Typical Groovy style is to use named-arg constructors when possible.
 * This is easy to do for Groovy objects or any objects following JavaBean
 * conventions. In other cases, inheriting the constructors may be useful.
 * However, sub-classes often introduce new properties and these are often best
 * set in a constructor; especially if that matches the style adopted
 * in parent classes. So, even for the example above, it may have been
 * better style to define an explicit constructor for <code>PersonAge</code>
 * that also set the <code>age</code> property. Sometimes, consistent
 * style is much more important than saving a few keystrokes.
 * <p>
 * As another example, this:
 * <pre>
 * {@code @InheritConstructors} class CustomException extends RuntimeException { }
 * </pre>
 * is equivalent to this:
 * <pre>
 * class CustomException extends RuntimeException {
 *     CustomException() {
 *         super()
 *     }
 *     CustomException(String message) {
 *         super(message)
 *     }
 *     CustomException(String message, Throwable cause) {
 *         super(message, cause)
 *     }
 *     CustomException(Throwable cause) {
 *         super(cause)
 *     }
 * }
 * </pre>
 * Known Limitations:
 * <ul>
 * <li>This AST transform creates (potentially) numerous constructors.
 * You should take care to avoid constructors with duplicate signatures if you are defining your own constructors or
 * combining with other AST transforms which create constructors (e.g. {@code @TupleConstructor});
 * the order in which the particular transforms are processed becomes important in that case.</li>
 * <li>If you create Groovy constructors with optional
 * arguments this leads to multiple constructors created in the byte code.
 * The expansion to multiple constructors occurs in a later phase to
 * this AST transformation. This means that you can't override (i.e. not
 * inherit) the constructors with signatures that Groovy adds later.
 * If you get it wrong you will get a compile-time error about the duplication.</li>
 * </ul>
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * import groovy.transform.InheritConstructors
 *
 * &#64;InheritConstructors
 * class MyException extends Exception {
 * }
 *
 * def e = new MyException()
 * def e1 = new MyException('message')   // Other constructors are available.
 * assert 'message' == e1.message
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * import groovy.transform.InheritConstructors

 * class Person {
 *     String name
 *
 *     Person(String name) {
 *         this.name = name
 *     }
 * }
 *
 * &#64;InheritConstructors
 * class Child extends Person {}
 *
 * def child = new Child('Liam')
 * assert 'Liam' == child.name
 * </pre>
 *
 * @since 1.7.3
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.InheritConstructorsASTTransformation")
public @interface InheritConstructors {
    /**
     * Whether to carry over annotations on the copied constructors.
     * Currently Closure annotation members are not supported.
     *
     * @return true if copied constructor should keep constructor annotations
     */
    boolean constructorAnnotations() default false;

    /**
     * Whether to carry over parameter annotations on the copied constructors.
     * Currently Closure annotation members are not supported.
     *
     * @return true if copied constructor should keep parameter annotations
     */
    boolean parameterAnnotations() default false;
}
