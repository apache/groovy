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
 * Field annotation to automatically delegate part of the functionality of a class to the annotated field.
 *
 * All non-private methods present in the type of annotated field and not present in the owner class
 * will be added to owner class at compile time. The implementation of such automatically added
 * methods is code which calls through to the delegate as per the normal delegate pattern.
 *
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
 * i.e. {@code @Delegate(interfaces = false)}.
 *
 * @author Alex Tkachman
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.FIELD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.DelegateASTTransformation")
public @interface Delegate {
    /**
     * @return true if owner class should implement interfaces implemented by field
     */
    boolean interfaces() default true;
}