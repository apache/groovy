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
 * Class annotation used to assist in the creation of map constructors in classes.
 * If the class is also annotated with {@code @ImmutableBase}, then the generated
 * constructor will contain additional code needed for immutable classes.
 * <p>
 * It allows you to write classes in this shortened form:
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 *
 * {@code @TupleConstructor}
 * class Person {
 *     String first, last
 * }
 *
 * {@code @CompileStatic} // optional
 * {@code @ToString(includeSuperProperties=true)}
 * {@code @MapConstructor}(pre={ super(args?.first, args?.last); args = args ?: [:] }, post = { first = first?.toUpperCase() })
 * class Author extends Person {
 *     String bookName
 * }
 *
 * assert new Author(first: 'Dierk', last: 'Koenig', bookName: 'ReGinA').toString() == 'Author(ReGinA, DIERK, Koenig)'
 * assert new Author().toString() == 'Author(null, null, null)'
 * </pre>
 * The {@code @MapConstructor} annotation instructs the compiler to execute an
 * AST transformation which adds the necessary constructor method to your class.
 * <p>
 * A map constructor is created which sets properties, and optionally fields and
 * super properties if the property/field name is a key within the map.
 * <p>
 * For the above example, the generated constructor will be something like:
 * <pre>
 * public Author(java.util.Map args) {
 *     super(args?.first, args?.last)
 *     args = args ? args : [:]
 *     if (args.containsKey('bookName')) {
 *         this.bookName = args['bookName']
 *     }
 *     first = first?.toUpperCase()
 * }
 * </pre>
 * <p>
 * Custom visibility:
 * <ul>
 * <li>The {@code @MapConstructor} annotation generates a public constructor unless an applicable
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
 * <li>The {@code @MapConstructor} annotation supports customization using {@code @PropertyOptions}
 * which allows a custom property handler to be defined. This is most typically used behind the scenes
 * by the {@code @Immutable} meta-annotation but you can also define your own handler. If a custom
 * handler is present, it will determine the code generated when initializing any property (or field).</li>
 * </ul>
 * <p>
 * Known limitations/special cases:
 * <ul>
 * <li>
 * The generated map constructor will have an argument of type {@code Map} unless a single property (or field)
 * is included and the type of that property (or field) is Object, AbstractMap, Map or HashMap.
 * In this case, the generated constructor will be of type {@code LinkedHashMap}.
 * This allows the possibility of also adding a tuple constructor without conflict, although
 * no such constructor is added automatically. You can disable this behavior by setting
 * the specialNamedArgHandling annotation attribute to false. This means that for the special
 * case mentioned above, you will not be able to also add a tuple constructor with a single Map
 * argument but you can supply any kind of map as your argument. We'd also recommend not
 * having both a map constructor and a tuple constructor with a single Object, AbstractMap or
 * HashMap since it can cause confusion as to which will be called.
 * </li>
 * </ul>
 *
 * @see PropertyOptions
 * @see VisibilityOptions
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.MapConstructorASTTransformation")
public @interface MapConstructor {
    /**
     * List of field and/or property names to exclude from the constructor.
     * Must not be used if 'includes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] excludes() default {};

    /**
     * List of field and/or property names to include within the constructor.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     * The default value is a special marker value indicating that no includes are defined; all fields and/or properties
     * are included if 'includes' remains undefined and 'excludes' is explicitly or implicitly an empty list.
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
     * Include fields from super classes in the constructor.
     * Groovy properties, JavaBean properties and fields (in that order) from superclasses come before
     * the members from a subclass (unless 'includes' is used to determine the order).
     */
    boolean includeSuperFields() default false;

    /**
     * Whether to include all properties (as per the JavaBean spec) in the generated constructor.
     * When true, Groovy treats any explicitly created setXxx() methods as property setters as per the JavaBean
     * specification.
     * JavaBean properties come after any Groovy properties but before any fields for a given class
     * (unless 'includes' is used to determine the order).
     */
    boolean allProperties() default false;

    /**
     * By default, Groovy properties are set directly using their respective field.
     * By setting {@code useSetters=true} then a writable property will be set using its setter.
     * If turning on this flag we recommend that setters that might be called are
     * made null-safe wrt the parameter.
     */
    boolean useSetters() default false;

    /**
     * Whether to include static properties in the constructor.
     */
    boolean includeStatic() default false;

    /**
     * Whether to include all fields and/or properties within the constructor, including those with names that are considered internal.
     */
    boolean allNames() default false;

    /**
     * In addition to the map constructor, provide a no-arg constructor which calls the map constructor with an empty map.
     */
    boolean noArg() default false;

    /**
     * If true, change the type of the map constructor argument from Map to LinkedHashMap only for the case where
     * the class has a single property (or field) with a Map-like type. This allows both a map and a tuple constructor
     * to be used side-by-side so long as care is taken about the types used when calling.
     */
    boolean specialNamedArgHandling() default true;

    /**
     * If specified, must match the "id" attribute in a VisibilityOptions annotation to enable a custom visibility.
     */
    String visibilityId() default Undefined.STRING;

    /**
     * A Closure containing statements which will be prepended to the generated constructor. The first statement within the Closure may be "super(someArgs)" in which case the no-arg super constructor won't be called.
     */
    Class pre() default Undefined.CLASS.class;

    /**
     * A Closure containing statements which will be appended to the end of the generated constructor. Useful for validation steps or tweaking the populated fields/properties.
     */
    Class post() default Undefined.CLASS.class;
}
