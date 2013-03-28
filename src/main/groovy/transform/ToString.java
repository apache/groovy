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
 * <pre>
 * import groovy.transform.ToString
 * {@code @ToString} class NamedThing {
 *     String name
 * }
 * {@code @ToString}(includeNames=true,includeSuper=true)
 * class AgedThing extends NamedThing {
 *     int age
 * }
 * println new AgedThing(name:'Lassie', age:5)
 * </pre>
 * Then the output will be:
 * <pre>
 * AgedThing(age:5, super:NamedThing(Lassie))
 * </pre>
 * {@code @ToString} can also be used in conjunction with {@code @Canonical} and {@code @Immutable}.
 * <p>
 * If you want to omit fields or properties referring to <tt>null</tt>, you can use the <tt>ignoreNulls</tt> flag:
 * <pre>
 * import groovy.transform.ToString
 * {@code @ToString(ignoreNulls = true)} class NamedThing {
 *     String name
 * }
 * println new NamedThing(name: null)
 * </pre>
 * Which results in:
 * <pre>
 * NamedThing()
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
 *
 * @author Paul King
 * @author Andre Steingress
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
     * List of field and/or property names to include within the generated toString.
     * Must not be used if 'excludes' is used. For convenience, a String with comma separated names
     * can be used in addition to an array (using Groovy's literal list notation) of String values.
     */
    String[] includes() default {};

    /**
     * Whether to include super in generated toString.
     */
    boolean includeSuper() default false;

    /**
     * Whether to include names of properties/fields in generated toString.
     */
    boolean includeNames() default false;

    /**
     * Include fields as well as properties in generated toString.
     */
    boolean includeFields() default false;

    /**
     * Don't display any fields or properties with value <tt>null</tt>.
     */
    boolean ignoreNulls() default false;

    /**
     * Whether to include the fully-qualified class name (i.e. including
     * the package) or just the simple class name in the generated toString.
     * @since 2.0.6
     */
    boolean includePackage() default true;

    /**
     * Whether to cache toString() calculations. You should only set this to true if
     * you know the object is immutable (or technically mutable but never changed).
     * @since 2.1.0
     */
    boolean cache() default false;

}
