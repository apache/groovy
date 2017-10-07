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

import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.customizers.builder.CompilerCustomizationBuilder;
import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically add the final qualifier to method, constructor,
 * and closure parameters.
 * <p>The annotation can be placed at the class level in which case it applies to
 * all methods, constructors, and closures within the class, or on individual
 * methods or constructors.
 * <p>In general it will make the most sense to automatically apply the
 * annotation to all classes of a project
 * (groovyc --configscript; google "Customising The Groovy Compiler", or see {@link CompilerConfiguration} and
 * {@link CompilerCustomizationBuilder}),
 * so that one can be sure that all arguments will automatically be final,
 * completely eliminating the need to clutter the code with final keywords
 * in any paramete list.
 * <p>
 * <em>Example usage:</em>
 * <pre class="groovyTestCase">
 * {@code @groovy.transform.AutoFinal}
 * class Person {
 *     final String first, last
 *     Person(String first, String last) {
 *         this.first = first
 *         this.last = last
 *     }
 *     String fullName(boolean reversed = false, String separator = ' ') {
 *         final concatCls = { String n0, String n1 -> "$n0$separator$n1" }
 *         concatCls(reversed ? last : first, reversed ? first : last)
 *     }
 * }
 *
 * final js = new Person('John', 'Smith')
 * assert js.fullName() == 'John Smith'
 * assert js.fullName(true, ', ') == 'Smith, John'
 * </pre>
 * for this case, the constructor for the <code>Person</code> class will be
 * equivalent to the following code:
 * <pre>
 * Person(final String first, final String last) {
 *   ...
 * }
 * </pre>
 * And after normal default parameter processing takes place, the following overloaded methods will exist:
 * <pre>
 * String fullName(final boolean reversed, final String separator) { ... }
 * String fullName(final boolean reversed) { fullName(reversed, ' ') }
 * String fullName() { fullName(false) }
 * </pre>
 * and the closure will have become:
 * <pre>
 * { final String n0, final String n1 -> "$n0$separator$n1" }
 * </pre>
 *
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.AutoFinalASTTransformation")
public @interface AutoFinal {
}
