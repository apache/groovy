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
 *
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
     * Include fields in the constructor.
     */
    boolean includeFields() default false;

    /**
     * Include properties in the constructor.
     */
    boolean includeProperties() default true;

    /**
     * Include properties from super classes in the constructor.
     */
    boolean includeSuperProperties() default false;

    /**
     * By default, properties are set directly using their respective field.
     * By setting {@code useSetters=true} then a writable property will be set using its setter.
     * If turning on this flag we recommend that setters that might be called are
     * made null-safe wrt the parameter.
     */
    boolean useSetters() default false;

    /**
     * A Closure containing statements which will be prepended to the generated constructor. The first statement within the Closure may be "super(someArgs)" in which case the no-arg super constructor won't be called.
     */
    Class pre() default Undefined.CLASS.class;

    /**
     * A Closure containing statements which will be appended to the end of the generated constructor. Useful for validation steps or tweaking the populated fields/properties.
     */
    Class post() default Undefined.CLASS.class;

    /**
     * Whether to include all fields and/or properties within the constructor, including those with names that are considered internal.
     */
    boolean allNames() default false;
}
