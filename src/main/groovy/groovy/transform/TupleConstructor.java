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
 * Class annotation used to assist in the creation of tuple constructors in classes.
 * If the class is also annotated with {@code @ImmutableBase}, then the generated
 * constructor will contain additional code needed for immutable classes.
 *
 * Should be used with care with other annotations which create constructors - see "Known
 * Limitations" for more details.
 * <p>
 * It allows you to write classes in this shortened form:
 * <pre class="groovyTestCase">
 * {@code @groovy.transform.TupleConstructor} class Customer {
 *     String first, last
 *     int age
 *     Date since
 *     Collection favItems
 * }
 * def c1 = new Customer(first:'Tom', last:'Jones', age:21, since:new Date(), favItems:['Books', 'Games'])
 * def c2 = new Customer('Tom', 'Jones', 21, new Date(), ['Books', 'Games'])
 * def c3 = new Customer('Tom', 'Jones')
 * </pre>
 * The {@code @TupleConstructor} annotation instructs the compiler to execute an
 * AST transformation which adds the necessary constructor method to your class.
 * <p>
 * A tuple constructor is created with a parameter for each property (and optionally field and
 * super properties). The default order is properties, pseudo/JavaBean properties and then fields
 * for parent classes first (if includeSuperXxx annotation attributes are used).
 *
 * A default value is provided (using Java's default values) for all parameters in the constructor.
 * Groovy's normal conventions then allows any number of parameters to be left off the end of the parameter list
 * including all of the parameters - giving a no-arg constructor which can be used with the map-style naming conventions.
 * <p>
 * The order of parameters is given by the properties of any super classes (if {@code includeSuperProperties} is set)
 * with the most super first followed by the properties of the class followed
 * by the fields of the class (if {@code includeFields} is set). Within each grouping the order
 * is as attributes appear within the respective class.
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * import groovy.transform.TupleConstructor
 *
 * &#64;TupleConstructor()
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 * }
 *
 * def person = new Person('mrhaki', ['Groovy', 'Java'])
 *
 * assert person.name == 'mrhaki'
 * assert person.likes == ['Groovy', 'Java']
 *
 * person = new Person('mrhaki')
 *
 * assert person.name == 'mrhaki'
 * assert !person.likes
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // includeFields in the constructor creation.
 * import groovy.transform.TupleConstructor
 *
 * &#64;TupleConstructor(includeFields=true)
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 *
 *     boolean isActivated() { active }
 * }
 *
 * def person = new Person('mrhaki', ['Groovy', 'Java'], true)
 *
 * assert person.name == 'mrhaki'
 * assert person.likes == ['Groovy', 'Java']
 * assert person.activated
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // use force attribute to force creation of constructor
 * // even if we define our own constructors.
 * import groovy.transform.TupleConstructor
 *
 * &#64;TupleConstructor(force=true)
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 *
 *     Person(boolean active) {
 *         this.active = active
 *     }
 *
 *     boolean isActivated() { active }
 * }
 *
 * def person = new Person('mrhaki', ['Groovy', 'Java'])
 *
 * assert person.name == 'mrhaki'
 * assert person.likes == ['Groovy', 'Java']
 * assert !person.activated
 *
 * person = new Person(true)
 *
 * assert person.activated
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // include properties and fields from super class.
 * import groovy.transform.TupleConstructor
 *
 * &#64;TupleConstructor(includeFields=true)
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 *
 *     boolean isActivated() { active }
 * }
 *
 * &#64;TupleConstructor(callSuper=true, includeSuperProperties=true, includeSuperFields=true)
 * class Student extends Person {
 *     List courses
 * }
 *
 * def student = new Student('mrhaki', ['Groovy', 'Java'], true, ['IT'])
 *
 * assert student.name == 'mrhaki'
 * assert student.likes == ['Groovy', 'Java']
 * assert student.activated
 * assert student.courses == ['IT']
 * </pre>
 * <p>
 * Custom visibility:
 * <ul>
 * <li>The {@code @TupleConstructor} annotation generates a public constructor unless an applicable
 * {@link VisibilityOptions} annotation is also present. It can be useful to change the visibility
 * if you want to also create a builder or provide your own static factory method for object creation.
 * You can make the constructor private and access it from the builder or your factory method. (Note:
 * you'll probably want to use {@code @CompileStatic} in conjunction with such an approach since
 * dynamic Groovy currently gives the ability to access even private constructors.)</li>
 * <li>An optional {@code visibilityId} attribute can be specified. If present, it must match the optional
 * {@code id} attribute of an applicable {@code VisibilityOptions} annotation. This can be useful
 * if multiple {@code VisibilityOptions} annotations are needed.</li>
 * </ul>
 * <p>
 * Custom property handling:
 * <ul>
 * <li>The {@code @TupleConstructor} annotation supports customization using {@code @PropertyOptions}
 * which allows a custom property handler to be defined. This is most typically used behind the scenes
 * by the {@code @Immutable} meta-annotation but you can also define your own handler. If a custom
 * handler is present, it will determine the code generated when initializing any property (or field).</li>
 * </ul>
 * <p>
 * Named-argument support:
 * <ul>
 * <li>Groovy supports named-arguments for classes with a no-arg constructor or a constructor
 * with a Map as the first argument. This is compatible with the default kind of constructor(s)
 * that {@code @TupleConstructor} produces.</li>
 * <li>If the {@code defaults} annotation attribute is set to {@code false},
 * and no other map-based constructor are added then named-argument processing will not be available.</li>
 * <li>If there is more than one included property (and/or field) and the first property (or field) has type
 * Object, AbstractMap, Map or HashMap, then a special {@code LinkedHashMap} constructor will be created
 * in addition to the tuple constructor to support named parameters in the normal way. This won't be created
 * if the class is already annotated with {@code @MapConstructor} or if the {@code defaults}
 * annotation attribute is set to {@code false}.</li>
 * <li>If the first property (or field) has type {@code LinkedHashMap} or if there is
 * a single Object, AbstractMap, Map or HashMap property (or field), then no additional constructor
 * will be added and Groovy's normal map-style naming conventions will not be available.</li>
 * </ul>
 * <p>
 * Known limitations/special cases:
 * <ul>
 * <li>This AST transform might become a no-op if you are defining your own constructors or
 * combining with other AST transforms which create constructors (e.g. {@code @InheritConstructors});
 * the order in which the particular transforms are processed becomes important in that case.
 * See the {@code force} attribute for further details about customizing this behavior.</li>
 * <li>This AST transform normally uses default parameter values which creates multiple constructors under
 * the covers. You should use with care if you are defining your own constructors or
 * combining with other AST transforms which create constructors (e.g. {@code @InheritConstructors});
 * the order in which the particular transforms are processed becomes important in that case.
 * See the {@code defaults} attribute for further details about customizing this behavior.</li>
 * </ul>
 *
 * @see PropertyOptions
 * @see VisibilityOptions
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.TupleConstructorASTTransformation")
public @interface TupleConstructor {
    /**
     * List of field and/or property names to exclude from the constructor.
     * Must not be used if 'includes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] excludes() default {};

    /**
     * List of field and/or property names to include within the constructor. The order of inclusion
     * is determined by the order in which the names are specified.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     * The default value is a special marker value indicating that no includes are defined;
     * all fields are included if includes remains undefined and excludes is explicitly or implicitly
     * an empty list.
     */
    String[] includes() default {Undefined.STRING};

    /**
     * Include properties in the constructor.
     */
    boolean includeProperties() default true;

    /**
     * Include fields in the constructor. Fields come after any properties.
     */
    boolean includeFields() default false;

    /**
     * Include properties from super classes in the constructor.
     * Groovy properties, JavaBean properties and fields (in that order) from superclasses come before
     * the members from a subclass (unless 'includes' is used to determine the order).
     */
    boolean includeSuperProperties() default false;

    /**
     * Include visible fields from super classes in the constructor.
     * Groovy properties, JavaBean properties and fields (in that order) from superclasses come before
     * the members from a subclass (unless 'includes' is used to determine the order).
     */
    boolean includeSuperFields() default false;

    /**
     * Should super properties be called within a call to the parent constructor
     * rather than set as properties. Typically used in combination with {@code includeSuperProperties}.
     * Can't be true if using {@code pre} with a {@code super} first statement.
     */
    boolean callSuper() default false;

    /**
     * By default, this annotation becomes a no-op if you provide your own constructor.
     * By setting {@code force=true} then the tuple constructor(s) will be added regardless of
     * whether existing constructors exist. It is up to you to avoid creating duplicate constructors.
     */
    boolean force() default false;

    /**
     * Used to set whether default value processing is enabled (the default) or disabled.
     *
     * By default, every constructor parameter is given a default value. This value will
     * be Java's default for primitive types (zero or false) and null for Objects, unless
     * an initial value is given when declaring the property or field. A consequence of
     * this design is that you can leave off parameters from the right if the default
     * value will suffice. As far as Java interoperability is concerned, Groovy will
     * create additional constructors under the covers representing the constructors
     * with parameters left off, all the way from the constructor with all arguments
     * to the no-arg constructor.
     *
     * However, when set to false, default values are not allowed for properties and fields.
     * Only the constructor containing all arguments will be provided.
     * In particular, a no-arg constructor won't be provided and since this is currently
     * used by Groovy when using named-arguments, the named-argument style won't be available.
     *
     * @since 2.5.0
     */
    boolean defaults() default true;

    /**
     * By default, properties are set directly using their respective field.
     * By setting {@code useSetters=true} then a writable property will be set using its setter.
     * If turning on this flag we recommend that setters that might be called are
     * made null-safe wrt the parameter.
     *
     * @since 2.5.0
     */
    boolean useSetters() default false;

    /**
     * Whether to include all fields and/or properties within the constructor, including those with names that are
     * considered internal.
     *
     * @since 2.5.0
     */
    boolean allNames() default false;

    /**
     * Whether to include all properties (as per the JavaBean spec) in the generated constructor.
     * When true, Groovy treats any explicitly created setXxx() methods as property setters as per the JavaBean
     * specification.
     * JavaBean properties come after any Groovy properties but before any fields for a given class
     * (unless 'includes' is used to determine the order).
     *
     * @since 2.5.0
     */
    boolean allProperties() default false;

    /**
     * If specified, must match the "id" attribute in a VisibilityOptions annotation to enable a custom visibility.
     *
     * @since 2.5.0
     */
    String visibilityId() default Undefined.STRING;

    /**
     * A Closure containing statements which will be prepended to the generated constructor. The first statement
     * within the Closure may be {@code super(someArgs)} in which case the no-arg super constructor won't be called.
     *
     * @since 2.5.0
     */
    Class pre() default Undefined.CLASS.class;

    /**
     * A Closure containing statements which will be appended to the end of the generated constructor. Useful for validation steps or tweaking the populated fields/properties.
     *
     * @since 2.5.0
     */
    Class post() default Undefined.CLASS.class;
}
