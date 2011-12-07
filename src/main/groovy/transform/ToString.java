/*
 * Copyright 2008-2011 the original author or authors.
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
 * <p/>
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
 *
 * @author Paul King
 * @see Immutable
 * @see Canonical
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ToStringASTTransformation")
public @interface ToString {
    /**
     * Comma separated list of field and/or property names to exclude from generated toString.
     * Must not be used if 'includes' is used.
     */
    String excludes() default "";

    /**
     * Comma separated list of field and/or property names to include within the generated toString.
     * Must not be used if 'excludes' is used.
     */
    String includes() default "";

    /**
     * Whether to include super in generated toString
     */
    boolean includeSuper() default false;

    /**
     * Whether to include names of properties/fields in generated toString
     */
    boolean includeNames() default false;

    /**
     * Include fields as well as properties in generated toString
     */
    boolean includeFields() default false;
}
