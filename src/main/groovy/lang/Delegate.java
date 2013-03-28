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
package groovy.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Field annotation to automatically delegate part of the functionality of an owner class to the annotated field.
 * <p>
 * All public instance methods present in the type of the annotated field and not present in the owner class
 * will be added to owner class at compile time. The implementation of such automatically added
 * methods is code which calls through to the delegate as per the normal delegate pattern.
 * <p>
 * As an example, consider this code:
 * <pre>
 * class Event {
 *     {@code @Delegate} Date when
 *     String title, url
 * }
 *
 * def gr8conf = new Event(title: "GR8 Conference",
 *                           url: "http://www.gr8conf.org",
 *                          when: Date.parse("yyyy/MM/dd", "2009/05/18"))
 *
 * def javaOne = new Event(title: "JavaOne",
 *                           url: "http://java.sun.com/javaone/",
 *                          when: Date.parse("yyyy/MM/dd", "2009/06/02"))
 *
 * assert gr8conf.before(javaOne.when)
 * </pre>
 *
 * In this example, the {@code Event} class will have a method called
 * {@code before(Date otherDate)} as well as other public methods of the
 * {@code Date} class.
 * The implementation of the {@code before()} method will look like this:
 * <pre>
 *     public boolean before(Date otherDate) {
 *         return when.before(otherDate);
 *     }
 * </pre>
 *
 * By default, the owner class will also be modified to implement any interfaces
 * implemented by the field. So, in the example above, because {@code Date}
 * implements {@code Cloneable} the following will be true:
 *
 * <pre>
 * assert gr8conf instanceof Cloneable
 * </pre>
 *
 * This behavior can be disabled by setting the
 * annotation's {@code interfaces} element to false,
 * i.e. {@code @Delegate(interfaces = false)}, e.g. in the above
 * example, the delegate definition would become:
 * <pre>
 *     {@code @Delegate}(interfaces = false) Date when
 * </pre>
 * and the following would be true:
 * <pre>
 * assert !(gr8conf instanceof Cloneable)
 * </pre>
 *
 * If multiple delegate fields are used and the same method signature occurs
 * in more than one of the respective field types, then the delegate will be
 * made to the first defined field having that signature. If this does occur,
 * it might be regarded as a smell (or at least poor style) and it might be
 * clearer to do the delegation by long hand.
 * <p>
 * By default, methods of the delegate type marked as {@code @Deprecated} are
 * not automatically added to the owner class (but see the technical note
 * about interfaces below). You can force these methods to
 * be added by setting the annotation's {@code deprecated} element to true,
 * i.e. {@code @Delegate(deprecated = true)}.
 * <p>
 * For example, in the example above if we change the delegate definition to:
 * <pre>
 *     {@code @Delegate}(deprecated = true) Date when
 * </pre>
 * then the following additional lines will execute successfully (during 2009):
 * <pre>
 * assert gr8conf.year + 1900 == 2009
 * assert gr8conf.toGMTString().contains(" 2009 ")
 * </pre>
 * Otherwise these lines produce a groovy.lang.MissingPropertyException
 * or groovy.lang.MissingMethodException respectively as those two methods are
 * {@code @Deprecated} in {@code Date}.
 * <p>
 * <b>Technical notes</b>:
 * <ul>
 * <li>Static methods, synthetic methods or methods from the <code>GroovyObject</code> interface
 * are not candidates for delegation</li>
 * <li>Non-abstract non-static methods defined in the owner class or its superclasses take
 * precedence over methods with identical signatures from a {@code @Delegate} field</li>
 * <li>All methods defined in the owner class (including static, abstract or private etc.)
 * take precedence over methods with identical signatures from a {@code @Delegate} field</li>
 * <li>Recursive delegation to your own class is not allowed</li>
 * <li>Mixing of {@code @Delegate} with default method arguments is known not to work in some cases.
 * We recommend not using these features together.</li>
 * <li>When the type of the delegate field is an interface, the {@code deprecated} attribute will be
 * ignored if the owner class implements that interface (i.e. you must set {@code interfaces=false}
 * if you want the {@code deprecated} attribute to be used). Otherwise, the resulting class would
 * not compile anyway without manually adding in any deprecated methods in the interface.</li>
 * </ul>
 *
 * @author Alex Tkachman
 * @author Paul King
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.DelegateASTTransformation")
public @interface Delegate {
    /**
     * @return true if owner class should implement interfaces implemented by field
     */
    boolean interfaces() default true;

    /**
     * Whether to apply the delegate pattern to deprecated methods; to avoid compilation
     * errors, this is ignored if the type of the delegate field is an interface and
     * {@code interfaces=true}.
     *
     * @return true if owner class should delegate to methods annotated with @Deprecated
     */
    boolean deprecated() default false;

    /**
     * Whether to carry over annotations from the methods of the delegate
     * to your delegating method. Currently Closure annotation members are
     * not supported.
     *
     * @return true if generated delegate methods should keep method annotations
     */
    boolean methodAnnotations() default false;

    /**
     * Whether to carry over annotations from the parameters of delegate
     * methods to your delegating method. Currently Closure annotation members are
     * not supported.
     *
     * @return true if generated delegate methods should keep parameter annotations
     */
    boolean parameterAnnotations() default false;
}
