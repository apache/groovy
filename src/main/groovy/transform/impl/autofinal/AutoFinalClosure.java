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
package groovy.transform.impl.autofinal;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation to automatically add final to various syntactic structures,
 * saving you typing of some boilerplate code.
 * Initially, only method and constructor parameters are supported.
 * The annotation may be placed on any method or constructor.
 * It can also be placed at the class level in which case it applies to
 * all methods and constructors within the class.
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
 *         "${reversed ? last : first}$separator${reversed ? first : last}"
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
 *     //...
 * }
 * </pre>
 * And after normal default parameter processing takes place, the following overloaded methods will exist:
 * <pre>
 * String fullName(final boolean reversed, final String separator) { ... }
 * String fullName(final boolean reversed) { fullName(reversed, ' ') }
 * String fullName() { fullName(false) }
 * </pre>
 *
 * @since 2.5.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.TYPE, ElementType.METHOD, ElementType.CONSTRUCTOR})
//@Target({ElementType.TYPE})
//@GroovyASTTransformationClass("org.codehaus.groovy.transform.impl.autofinal.AutoFinalClosureASTTransformation")
@GroovyASTTransformationClass("org.codehaus.groovy.transform.AutoFinalClosureASTTransformation")
public @interface AutoFinalClosure {
}
