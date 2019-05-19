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
 * Class annotation used to assist in the creation of {@code Cloneable} classes.
 * The {@code @AutoClone} annotation instructs the compiler to execute an
 * AST transformation which adds a public {@code clone()} method and adds
 * {@code Cloneable} to the list of interfaces which the class implements.
 * <p>
 * Because the JVM doesn't have a one-size fits all cloning strategy, several
 * customizations exist for the cloning implementation. By default, the {@code clone()}
 * method will call {@code super.clone()} before calling {@code clone()} on each
 * {@code Cloneable} property of the class.
 * <p>
 * Example usage:
 * <pre>
 * import groovy.transform.AutoClone
 * {@code @AutoClone}
 * class Person {
 *   String first, last
 *   List favItems
 *   Date since
 * }
 * </pre>
 * Which will create a class equivalent to the following:
 * <pre>
 * class Person implements Cloneable {
 *   ...
 *   public Person clone() throws CloneNotSupportedException {
 *     Person result = (Person) super.clone()
 *     result.favItems = favItems instanceof Cloneable ? (List) favItems.clone() : favItems
 *     result.since = (Date) since.clone()
 *     return result
 *   }
 *   ...
 * }
 * </pre>
 * Which can be used as follows:
 * <pre>
 * def p = new Person(first:'John', last:'Smith', favItems:['ipod', 'shiraz'], since:new Date())
 * def p2 = p.clone()
 *
 * assert p instanceof Cloneable
 * assert p.favItems instanceof Cloneable
 * assert p.since instanceof Cloneable
 * assert !(p.first instanceof Cloneable)
 *
 * assert !p.is(p2)
 * assert !p.favItems.is(p2.favItems)
 * assert !p.since.is(p2.since)
 * assert p.first.is(p2.first)
 * </pre>
 * In the above example, {@code super.clone()} is called which in this case
 * calls {@code clone()} from {@code java.lang.Object}. This does a bit-wise
 * copy of all the properties (references and primitive values). Properties
 * like {@code first} has type {@code String} which is not {@code Cloneable}
 * so it is left as the bit-wise copy. Both {@code Date} and {@code ArrayList}
 * are {@code Cloneable} so the {@code clone()} method on each of those properties
 * will be called. For the list, a shallow copy is made during its {@code clone()} method.
 * <p>
 * If your classes require deep cloning, it is up to you to provide the appropriate
 * deep cloning logic in the respective {@code clone()} method for your class.
 * <p>
 * If one of your properties contains an object that doesn't support cloning
 * or attempts deep copying of a data structure containing an object that
 * doesn't support cloning, then a {@code CloneNotSupportedException} may occur
 * at runtime.
 * <p>
 * Another popular cloning strategy is known as the copy constructor pattern.
 * If any of your fields are {@code final} and {@code Cloneable} you should set
 * {@code style=COPY_CONSTRUCTOR} which will then use the copy constructor pattern.
 * Here is an example making use of the copy constructor pattern:
 * <pre>
 * import groovy.transform.AutoClone
 * import static groovy.transform.AutoCloneStyle.*
 * {@code @AutoClone(style=COPY_CONSTRUCTOR)}
 * class Person {
 *   final String first, last
 *   final Date birthday
 * }
 * {@code @AutoClone(style=COPY_CONSTRUCTOR)}
 * class Customer extends Person {
 *   final int numPurchases
 *   final List favItems
 * }
 * </pre>
 * Which will create classes equivalent to the following:
 * <pre>
 * class Person implements Cloneable {
 *   ...
 *   protected Person(Person other) throws CloneNotSupportedException {
 *     first = other.first
 *     last = other.last
 *     birthday = (Date) other.birthday.clone()
 *   }
 *   public Person clone() throws CloneNotSupportedException {
 *     return new Person(this)
 *   }
 *   ...
 * }
 * class Customer extends Person {
 *   ...
 *   protected Customer(Customer other) throws CloneNotSupportedException {
 *     super(other)
 *     numPurchases = other.numPurchases
 *     favItems = other.favItems instanceof Cloneable ? (List) other.favItems.clone() : other.favItems
 *   }
 *   public Customer clone() throws CloneNotSupportedException {
 *     return new Customer(this)
 *   }
 *   ...
 * }
 * </pre>
 * If you use this style on a child class, the parent class must
 * also have a copy constructor (created using this annotation or by hand).
 * This approach can be slightly slower than the traditional cloning approach
 * but the {@code Cloneable} fields of your class can be final. When using the copy constructor style,
 * you can provide your own custom constructor by hand if you wish. If you do so, it is up to you to
 * correctly copy, clone or deep clone the properties of your class.
 * <p>
 * As a variation of the last two styles, if you set {@code style=SIMPLE}
 * then the no-arg constructor will be called followed by setting the
 * individual properties (and/or fields) calling {@code clone()} if the
 * property/field implements {@code Cloneable}. Here is an example:
 * <pre>
 * import groovy.transform.AutoClone
 * import static groovy.transform.AutoCloneStyle.*
 * {@code @AutoClone(style=SIMPLE)}
 * class Person {
 *   final String first, last
 *   final Date birthday
 * }
 * {@code @AutoClone(style=SIMPLE)}
 * class Customer {
 *   final List favItems
 * }
 * </pre>
 * Which will create classes equivalent to the following:
 * <pre>
 * class Person implements Cloneable {
 *   ...
 *   public Person clone() throws CloneNotSupportedException {
 *     def result = new Person()
 *     copyOrCloneMembers(result)
 *     return result
 *   }
 *   protected void copyOrCloneMembers(Person other) {
 *     other.first = first
 *     other.last = last
 *     other.birthday = (Date) birthday.clone()
 *   }
 *   ...
 * }
 * class Customer extends Person {
 *   ...
 *   public Customer clone() throws CloneNotSupportedException {
 *     def result = new Customer()
 *     copyOrCloneMembers(result)
 *     return result
 *   }
 *   protected void copyOrCloneMembers(Customer other) {
 *     super.copyOrCloneMembers(other)
 *     other.favItems = favItems instanceof Cloneable ? (List) favItems.clone() : favItems
 *   }
 *   ...
 * }
 * </pre>
 * You would typically use this style only for base classes where you didn't
 * want the normal {@code Object} {@code clone()} method to be called and
 * you would typically need to use the {@code SIMPLE} style for any child classes.
 * <p>
 * As a final example, if your class already implements the {@code Serializable}
 * or {@code Externalizable} interface, you can choose the following cloning style:
 * <pre>
 * {@code @AutoClone(style=SERIALIZATION)}
 * class Person implements Serializable {
 *   String first, last
 *   Date birthday
 * }
 * </pre>
 * which outputs a class with the following form:
 * <pre>
 * class Person implements Cloneable, Serializable {
 *   ...
 *   Person clone() throws CloneNotSupportedException {
 *     def baos = new ByteArrayOutputStream()
 *     baos.withObjectOutputStream{ it.writeObject(this) }
 *     def bais = new ByteArrayInputStream(baos.toByteArray())
 *     bais.withObjectInputStream(getClass().classLoader){ (Person) it.readObject() }
 *   }
 *   ...
 * }
 * </pre>
 * This will output an error if your class doesn't implement one of
 * {@code Serializable} or {@code Externalizable}, will typically be
 * significantly slower than the other approaches, also doesn't
 * allow fields to be final, will take up more memory as even immutable classes
 * like String will be cloned but does have the advantage that it performs
 * deep cloning automatically.
 * <p>
 * Further references on cloning:
 * <ul>
 * <li><a href="http://www.codeguru.com/java/tij/tij0128.shtml">http://www.codeguru.com/java/tij/tij0128.shtml</a>
 * <li><a href="http://www.artima.com/objectsandjava/webuscript/ClonCollInner1.html">http://www.artima.com/objectsandjava/webuscript/ClonCollInner1.html</a>
 * <li><a href="http://courses.dce.harvard.edu/~cscie160/JDCTipsCloning">http://courses.dce.harvard.edu/~cscie160/JDCTipsCloning</a>
 * <li><a href="http://www.agiledeveloper.com/articles/cloning072002.htm">http://www.agiledeveloper.com/articles/cloning072002.htm</a>
 * </ul>
 *
 * @see AutoCloneStyle
 * @see ExternalizeMethods
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.AutoCloneASTTransformation")
public @interface AutoClone {
    /**
     * Comma separated list of property (and/or field) names to exclude from cloning.
     * For convenience, a String with comma separated names can be used in addition
     * to an array (using Groovy's literal list notation) of String values.
     * <p>
     * NOTE: When using the {@code CLONE} style, property (and/or field) copying might occur as part of
     * calling {@code super.clone()} which will ignore this list. You can then use this list to
     * streamline the provided {@code clone()} implementation by selecting which Cloneable properties
     * (and/or fields) will have a subsequent call to their {@code clone()} method. If you have
     * immutable properties (and/or fields) this can be useful as the extra {@code clone()} will
     * not be necessary and cloning will be more efficient.
     * <p>
     * NOTE: This doesn't affect property (and/or field) copying that might occur as part
     * of serialization when using the {@code SERIALIZATION} style, i.e. this flag is ignored;
     * instead adjust your serialization code to include or exclude the desired
     * properties (and/or fields) which should carry over during cloning.
     */
    String[] excludes() default {};

    /**
     * Include fields as well as properties when cloning.
     * <p>
     * NOTE: When using the {@code CLONE} style, field copying might occur as part of
     * calling {@code super.clone()} and might be all you require; if you turn on
     * this flag, the provided {@code clone()} implementation will also
     * subsequently call {@code clone()} for each {@code Cloneable} field which can be
     * useful if you have mutable fields.
     * <p>
     * NOTE: This doesn't affect field copying that might occur as part of
     * serialization when using the {@code SERIALIZATION} style, i.e. this flag is ignored;
     * instead adjust your serialization code to include or exclude your fields.
     */
    boolean includeFields() default false;

    /**
     * Style to use when cloning.
     */
    AutoCloneStyle style() default AutoCloneStyle.CLONE;
}
