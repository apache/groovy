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
package groovy.lang;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that supports writing constructor call expressions without the 'new'
 * keyword. Instead they can be written "Ruby-style" as a method call to a 'new'
 * method or "Python-style" by just omitting the 'new' keyword.
 * <p>
 * WARNING: For the Python style with class-name-matching pattern, the pattern should be chosen as to avoid matching
 * method names if possible. If following Java/Groovy naming conventions, class names (contrary to method names) start
 * with an uppercase letter. In this case {@code pattern="[A-Z].*"} (see {@link java.util.regex.Pattern} for supported
 * Java pattern syntax) is the recommended pattern to allow all classes to be created without requiring a new keyword.
 * Using a pattern that also matches method names (e.g. ".+", ".*" or "[a-zA-Z].*") might negatively impact build
 * performance, since the Groovy compiler will have to match every class in context against any potential constructor
 * call.
 * <p>
 * {@literal @Newify} allows you to write code snippets like this ("Python-style"):
 * <pre>
 * {@code @Newify([Tree,Leaf])} class MyTreeProcessor {
 *     def myTree = Tree(Tree(Leaf("A"), Leaf("B")), Leaf("C"))
 *     def process() { ... }
 * }
 * </pre>
 * <pre>
 * {@code // Any class whose name matches pattern can be created without new}
 * {@code @Newify(pattern="[A-Z].*")} class MyTreeProcessor {
 *     final myTree = Tree(Tree(Leaf("A"), Leaf("B")), Leaf("C"))
 *     final sb = StringBuilder("...")
 *     def dir = File('.')
 *     def root = XmlSlurper().parseText(File(dir, sb.toString()).text)
 * }
 * </pre>
 * or this ("Ruby-style"):
 * <pre>
 * {@code @Newify} class MyTreeProcessor {
 *     def myTree = Tree.new(Tree.new(Leaf.new("A"), Leaf.new("B")), Leaf.new("C"))
 *     def process() { ... }
 * }
 * </pre>
 *
 * After the AST transformation, the following code is passed on for further compilation:
 * <pre>
 * class MyTreeProcessor {
 *     def myTree = new Tree(new Tree(new Leaf("A"), new Leaf("B")), new Leaf("C"))
 *     def process() { ... }
 * }
 * </pre>
 * The annotation can be used on a whole class as shown above or selectively on a particular
 * method, constructor or field.
 * <p>
 * The "Ruby-style" new conversions occur automatically unless the 'auto=false'
 * flag is given when using the annotation. You might do this if you create a new method
 * using meta programming.
 * <p>
 * For the "Python-style" conversions you can either specify each class name on which you want them
 * to apply, or supply a pattern to match class names against. The transformation then works by
 * matching the basename of the provided classes to any
 * similarly named instance method calls not specifically bound to an object, i.e. associated
 * with the 'this' object. In other words <code>Leaf("A")</code> would be transformed to
 * <code>new Leaf("A")</code> but <code>x.Leaf("A")</code> would not be touched.
 * <p>
 * An example showing how to use the annotation at different levels:
 * <pre>
 * {@code @Newify(auto=false, value=Foo)}
 * class Main {
 *     {@code @Newify} // turn auto on for field
 *     def field1 = java.math.BigInteger.new(42)
 *     def field2, field3, field4
 *
 *     {@code @Newify(pattern="[A-z][A-Za-z0-9_]*")} // Any class name that starts with an uppercase letter
 *     def process() {
 *         field2 = A(Bb(Ccc("my bar")))
 *     }
 *
 *     {@code @Newify(Baz)}
 *     Main() {
 *         field3 = Foo("my foo")
 *         field4 = Baz("my baz")
 *     }
 * }
 * </pre>
 *
 * The annotation is intended to be used sparingly; perhaps in DSL scenarios or when
 * using deeply nested structural types. In particular, there is no support for using
 * the facility with two similarly named classes from different packages at the same time.
 * Though it is OK to have different packages in different contexts. Also, there is
 * no support for turning "Ruby-style" conversions off at the method, constructor or
 * field level if already turned on at the class level.
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.CONSTRUCTOR, ElementType.METHOD, ElementType.TYPE, ElementType.FIELD, ElementType.LOCAL_VARIABLE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.NewifyASTTransformation")
public @interface Newify {
    /**
     * @return one or more classes where "Python-style" constructor notation will be supported
     */
    Class<?>[] value() default {};

    /**
     * @return if automatic conversion of "Ruby-style" new method calls should occur
     */
    boolean auto() default true;

    /**
     * @since 2.5.0
     * @return a regex pattern for class names where "Python-style" constructor notation will be supported
     */
    String pattern() default "";
}
