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
 * Class annotation used to assist in the creation of {@code toString()} methods in classes.
 * The {@code @ToString} annotation instructs the compiler to execute an
 * AST transformation which adds the necessary toString() method.
 * <p>
 * It allows you to write classes in this shortened form:
 * <pre>
 * {@code @ToString}
 * class Customer {
 *     String first, last
 *     int age
 *     Date since = new Date()
 *     Collection favItems
 *     private answer = 42
 * }
 * println new Customer(first:'Tom', last:'Jones', age:21, favItems:['Books', 'Games'])
 * </pre>
 * Which will have this output:
 * <pre>
 * Customer(Tom, Jones, 21, Wed Jul 14 23:57:14 EST 2010, [Books, Games])
 * </pre>
 * There are numerous options to customize the format of the generated output.
 * E.g. if you change the first annotation to:
 * <pre>
 * {@code @ToString(includeNames=true)}
 * </pre>
 * Then the output will be:
 * <pre>
 * Customer(first:Tom, last:Jones, age:21, since:Wed Jul 14 23:57:50 EST 2010, favItems:[Books, Games])
 * </pre>
 * Or if you change the first annotation to:
 * <pre>
 * {@code @ToString(includeNames=true,includeFields=true,excludes="since,favItems")}
 * </pre>
 * Then the output will be:
 * <pre>
 * Customer(first:Tom, last:Jones, age:21, answer:42)
 * </pre>
 * If you have this example:
 * <pre class="groovyTestCase">
 * import groovy.transform.ToString
 * {@code @ToString} class NamedThing {
 *     String name
 * }
 * {@code @ToString}(includeNames=true,includeSuper=true)
 * class AgedThing extends NamedThing {
 *     int age
 * }
 * String agedThingAsString = new AgedThing(name:'Lassie', age:5).toString()
 * assert agedThingAsString == 'AgedThing(age:5, super:NamedThing(Lassie))'
 * </pre>
 * {@code @ToString} can also be used in conjunction with {@code @Canonical} and {@code @Immutable}.
 * <p>
 * If you want to omit fields or properties referring to <tt>null</tt>, you can use the <tt>ignoreNulls</tt> flag:
 * <pre class="groovyTestCase">
 * import groovy.transform.ToString
 * {@code @ToString(ignoreNulls = true)} class NamedThing {
 *     String name
 * }
 * assert new NamedThing(name: null).toString() == 'NamedThing()'
 * </pre>
 * <p>
 * By default the fully-qualified class name is used as part of the generated toString.
 * If you want to exclude the package, you can set the includePackage flag to false, e.g.:
 * <pre>
 * package my.company
 * import groovy.transform.ToString
 * {@code @ToString(includePackage = false)} class NamedThing {
 *     String name
 * }
 * println new NamedThing(name: "Lassie")
 * </pre>
 * Which results in:
 * <pre>
 * NamedThing(name: Lassie)
 * </pre>
 * If the includePackage flag is {@code true} (the default), then the output will be:
 * <pre>
 * my.company.NamedThing(name: Lassie)
 * </pre>
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------    
 * // Most simple implementation of toString.
 * import groovy.transform.ToString
 *
 * {@code @ToString}
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 * }
 *
 * def person = new Person(name: 'mrhaki', likes: ['Groovy', 'Java'])
 *
 * assert person.toString() == 'Person(mrhaki, [Groovy, Java])'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------    
 * // includeNames to output the names of the properties.
 * import groovy.transform.ToString
 *
 * &#64;ToString(includeNames=true)
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 * }
 *
 * def person = new Person(name: 'mrhaki', likes: ['Groovy', 'Java'])
 *
 * assert person.toString() == 'Person(name:mrhaki, likes:[Groovy, Java])'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // includeFields to not only output properties, but also field values.
 * import groovy.transform.ToString
 *
 * &#64;ToString(includeNames=true, includeFields=true)
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 * }
 *
 * def person = new Person(name: 'mrhaki', likes: ['Groovy', 'Java'])
 *
 * assert person.toString() == 'Person(name:mrhaki, likes:[Groovy, Java], active:false)'
 * </pre>
 * <pre>
 * //--------------------------------------------------------------------------    
 * // Use includeSuper to include properties from super class in output.
 * import groovy.transform.ToString
 *
 * &#64;ToString(includeNames=true)
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 * }
 *
 * &#64;ToString(includeSuper=true, includeNames=true)
 * class Student extends Person {
 *     List courses
 * }
 *
 * def student = new Student(name: 'mrhaki', likes: ['Groovy', 'Java'], courses: ['IT', 'Business'])
 *
 * assert student.toString() == 'Student(courses:[IT, Business], super:Person(name:mrhaki, likes:[Groovy, Java]))'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------    
 * // excludes active field and likes property from output
 * import groovy.transform.ToString
 *
 * &#64;ToString(includeNames=true, includeFields=true, excludes='active,likes')
 * class Person {
 *     String name
 *     List likes
 *     private boolean active = false
 * }
 *
 * def person = new Person(name: 'mrhaki', likes: ['Groovy', 'Java'])
 *
 * assert person.toString() == 'Person(name:mrhaki)'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // Don't include the package name in the output
 * package com.mrhaki.blog.groovy
 *
 * import groovy.transform.*
 *
 * &#64;ToString(includePackage=false)
 * class Course {
 *     String title
 *     Integer maxAttendees
 * }
 *
 * final Course course = new Course(title: 'Groovy 101', maxAttendees: 200)
 *
 * assert course.toString() == 'Course(Groovy 101, 200)'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // Don't use properties with null value.
 * package com.mrhaki.blog.groovy
 *
 * import groovy.transform.*
 *
 * &#64;ToString(ignoreNulls=true)
 * class Course {
 *     String title
 *     Integer maxAttendees
 * }
 *
 * final Course course = new Course(title: 'Groovy 101')
 *
 * assert course.toString() == 'com.mrhaki.blog.groovy.Course(Groovy 101)'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // Cache toString() result.
 * package com.mrhaki.blog.groovy
 *
 * import groovy.transform.*
 *
 * &#64;ToString(cache=true)
 * class Course {
 *     String title
 *     Integer maxAttendees
 * }
 *
 * Course course = new Course(title: 'Groovy 101', maxAttendees: 200)
 *
 * assert course.toString() == 'com.mrhaki.blog.groovy.Course(Groovy 101, 200)'
 *
 * // Value change will not be reflected in toString().
 * course.title = 'Grails with REST'
 *
 * assert course.toString() == 'com.mrhaki.blog.groovy.Course(Groovy 101, 200)'
 * assert course.title == 'Grails with REST'
 * </pre> 
 *
 * @see groovy.transform.Immutable
 * @see groovy.transform.Canonical
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ToStringASTTransformation")
public @interface ToString {
    /**
     * List of field and/or property names to exclude from generated toString.
     * Must not be used if 'includes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] excludes() default {};

    /**
     * List of field and/or property names to include within the generated toString. The order of inclusion
     * is determined by the order in which the names are specified.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     * The default value is a special marker value indicating that no includes are defined; all fields and/or properties
     * are included if 'includes' remains undefined and 'excludes' is explicitly or implicitly an empty list.
     * The special name 'super' can be used instead of using the 'includeSuper' flag.
     */
    String[] includes() default {Undefined.STRING};

    /**
     * Whether to include the toString() of super in the generated toString.
     */
    boolean includeSuper() default false;

    /**
     * Whether to include names of properties/fields in the generated toString.
     */
    boolean includeNames() default false;

    /**
     * Include fields as well as properties in the generated toString. Fields come after any properties.
     */
    boolean includeFields() default false;

    /**
     * Whether to include super properties in the generated toString.
     * Groovy properties, JavaBean properties and fields (in that order) from superclasses come after
     * the members from a subclass (unless 'includes' is used to determine the order).
     *
     * @since 2.4.0
     */
    boolean includeSuperProperties() default false;

    /**
     * Include super fields in the generated toString.
     * Groovy properties, JavaBean properties and fields (in that order) from superclasses come after
     * the members from a subclass (unless 'includes' is used to determine the order).
     *
     * @since 2.5.0
     */
    boolean includeSuperFields() default false;

    /**
     * Don't display any fields or properties with value <tt>null</tt>.
     */
    boolean ignoreNulls() default false;

    /**
     * Whether to include the fully-qualified class name (i.e. including
     * the package) or just the simple class name in the generated toString.
     *
     * @since 2.0.6
     */
    boolean includePackage() default true;

    /**
     * Whether to include all properties (as per the JavaBean spec) in the generated toString.
     * Groovy recognizes any field-like definitions with no explicit visibility as property definitions
     * and always includes them in the {@code @ToString} generated toString (as well as auto-generating the
     * appropriate getters and setters). Groovy also treats any explicitly created getXxx() or isYyy()
     * methods as property getters as per the JavaBean specification. Old versions of Groovy did not.
     * So set this flag to false for the old behavior or if you want to explicitly exclude such properties.
     * JavaBean properties come after any Groovy properties but before any fields for a given class
     * (unless 'includes' is used to determine the order).
     *
     * @since 2.5.0
     */
    boolean allProperties() default true;

    /**
     * Whether to cache toString() calculations. You should only set this to true if
     * you know the object is immutable (or technically mutable but never changed).
     *
     * @since 2.1.0
     */
    boolean cache() default false;

    /**
     * Whether to include all fields and/or properties in the generated toString, including those with names that
     * are considered internal.
     *
     * @since 2.5.0
     */
    boolean allNames() default false;

    /**
     * Whether to avoid using Groovy runtime methods when printing the toString for class members.
     * The generated code is more similar to what is typically used in POJO classes.
     * The presence of the {@code @POJO} annotation on a class is looked for by default but this annotation attribute
     * allows the feature to be explicitly configured if desired.
     *
     * <em>NOTE:</em> this is an incubating feature and may change in future versions.
     *
     * @since 4.0.0
     */
    boolean pojo() default false;
}
