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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The AnnotationCollector can be used to define aliases for groups of 
 * annotations. The Alias needs to be a class or annotation annotated with 
 * AnnotationCollector, otherwise nothing is required. The alias will be 
 * replaced on the AST level and will never appear in later. Any members of the 
 * class or annotation will be ignored, but could be used by a custom processor.
 * Annotation arguments are mapped to the aliased annotations
 * if existing. Should the default processor not be able to map one of the
 * arguments and error will be given. Is this not wished or if you want a 
 * different mapping a custom processor has to be used. There are two ways of 
 * using the alias. The first way is by providing the annotations as list/array:
 * <pre class="groovyTestCase">
 *          import groovy.transform.*
 *          &#64;AnnotationCollector([ToString, EqualsAndHashCode, Immutable])
 *          &#64;interface Alias {}

 *          &#64;Alias(excludes=["a"])
 *          class Foo {
 *              Integer a, b
 *          }
 *          assert Foo.class.annotations.size() == 3
 *          assert new Foo(a: 1, b: 2).toString() == "Foo(2)"
 * </pre>
 * In the example above we have Alias as the alias annotation and an argument
 * excludes which will be mapped to ToString and EqualsAndHashCode. Immutable 
 * doesn't have excludes, thus nothing will be done there.<br>
 * The other way is to add annotations to the alias:
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 * &#64;ToString(excludes=["a"])
 * &#64;EqualsAndHashCode
 * &#64;Immutable
 * &#64;AnnotationCollector
 * &#64;interface Alias {}
 *
 * &#64;Alias
 * class Foo {
 *     Integer a, b
 * }
 * assert Foo.class.annotations.size() == 3
 * assert new Foo(a: 1, b: 2).toString() == "Foo(2)"
 * </pre>
 * In the example above we have again Alias as the alias annotation, but
 * this time the argument is part of the alias. Instead of mapping excludes to
 * ToString as well as EqualsAndHashCode, only ToString will have the excludes.
 * Again the alias can have an argument excludes, which would overwrite the 
 * excludes given in from the definition and be mapped to ToString as well as
 * EqualsAndHashCode.
 * If both ways are combined, then the list overwrites annotation usage.
 * NOTE: The aliasing does not support aliasing of aliased annotations. 
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * import groovy.transform.*
 * &#64;AnnotationCollector([EqualsAndHashCode, ToString])
 * &#64;interface Simple {}
 *
 *
 * &#64;Simple
 * class User {
 *     String username
 *     int age
 * }
 *
 * def user = new User(username: 'mrhaki', age: 39)
 * assert user.toString() == 'User(mrhaki, 39)'
 *
 * // We still have 2 annotations:
 * assert User.class.annotations.size() == 2
 *
 *
 * // We can use the attributes from the 
 * // grouped annotations.
 * &#64;Simple(excludes = 'street')
 * class Address {
 *     String street, town
 * }
 *
 * def address = new Address(street: 'Evergreen Terrace', town: 'Springfield') 
 * assert address.toString() == 'Address(Springfield)'
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // Use a custom processor to handle attributes.
 * import org.codehaus.groovy.transform.*
 * import org.codehaus.groovy.ast.*
 * import org.codehaus.groovy.control.*
 *
 * class SimpleProcessor extends AnnotationCollectorTransform {
 *
 *     public List&lt;AnnotationNode&gt; visit(AnnotationNode collector, 
 *                                       AnnotationNode aliasAnnotationUsage, 
 *                                       AnnotatedNode aliasAnnotated, 
 *                                       SourceUnit source) {
 *
 *         // Get attributes and attribute value for dontUse.
 *         def attributes = aliasAnnotationUsage.getMembers()
 *         def dontUse = attributes.get('dontUse')
 *         attributes.remove('dontUse')
 *
 *         if (dontUse) {
 *             // Assign value of dontUse to excludes attributes.
 *             aliasAnnotationUsage.addMember("excludes", dontUse)
 *         }
 *
 *         super.visit(collector, aliasAnnotationUsage, aliasAnnotated, source)
 *     }
 *
 * }
 *
 * new GroovyShell(this.class.classLoader).evaluate '''
 * import groovy.transform.*
 *
 * &#64;AnnotationCollector(value = [EqualsAndHashCode, ToString], processor = 'SimpleProcessor')
 * &#64;interface Simple {}
 *
 *
 * &#64;Simple(dontUse = 'age')
 * class User {
 *     String username
 *     int age
 * }
 *
 * def user = new User(username: 'mrhaki', age: 39)
 * assert user.toString() == 'User(mrhaki)'
 * '''
 * </pre>
 * <pre class="groovyTestCase">
 * //--------------------------------------------------------------------------
 * // Use AnnotationCollector as last annotation to group the
 * // previous annotations.
 * import groovy.transform.*
 * &#64;EqualsAndHashCode
 * &#64;ToString
 * &#64;AnnotationCollector
 * &#64;interface Simple {}
 *
 *
 * &#64;Simple
 * class User {
 *     String username
 * }
 *
 * def user = new User(username: 'mrhaki')
 * assert user.toString() == 'User(mrhaki)'
 * </pre>
 * 
 * @see org.codehaus.groovy.transform.AnnotationCollectorTransform
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.ANNOTATION_TYPE, ElementType.TYPE})
public @interface AnnotationCollector {
    /**
     * Processor used for computing custom logic or the list of annotations, or 
     * both. The default is org.codehaus.groovy.transform.AnnotationCollectorTransform.
     * Custom processors need to extend that class. 
     */
    String processor() default "org.codehaus.groovy.transform.AnnotationCollectorTransform";

    /**
     * When the collector annotation is replaced, whether to check for duplicates between
     * the replacement annotations and existing explicit annotations.
     * If you use a custom processor, it is up to that processor whether it honors or ignores
     * this parameter. The default processor honors the parameter.
     */
    AnnotationCollectorMode mode() default AnnotationCollectorMode.DUPLICATE;

    /**
     * List of aliased annotations.
     */
    Class[] value() default {};

    /**
     * Used internally - the default value is a marker value indicating that the attribute hasn't been set.
     * Normally set automatically during annotation processing to an automatically created
     * nested helper class which holds serialization information used in pre-compiled scenarios.
     * If set to the collector annotation, re-purposes the annotation itself to become the helper
     * class (legacy approach used in Groovy 2.5 up to 2.5.2).
     */
    Class serializeClass() default Undefined.CLASS.class;
}
