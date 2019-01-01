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
package groovy.transform

import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * Method annotation used to transform methods with tail recursive calls into iterative methods automagically
 * since the JVM cannot do this itself. This works for both static and non-static methods.
 * <p/>
 * It allows you to write a method like this:
 * <pre class="groovyTestCase">
 * import groovy.transform.TailRecursive
 * class Target {
 *      {@code @TailRecursive}
 *      long sumUp(long number, long sum = 0) {
 *          if (number == 0)
 *              return sum;
 *          sumUp(number - 1, sum + number)
 *      }
 * }
 * def target = new Target()
 * assert target.sumUp(100) == 5050
 * assert target.sumUp(1000000) == 500000500000 //will blow the stack on most machines when used without {@code @TailRecursive}
 * </pre>
 *
 * {@code @TailRecursive} is supposed to work in combination with {@code @CompileStatic}
 *
 * Known shortcomings:
 * <ul>
 * <li>Only non-void methods are currently being handled. Void methods will fail compilation.
 * <li>Only direct recursion (calling the exact same method again) is supported.
 * <li>Mixing of tail calls and non-tail calls is not possible. The compiler will complain if some recursive calls cannot be handled.
 * <li>Checking if a recursive call is really tail-recursive is not very strict. You might run into cases where non-tail calls will be considered tail calls.
 * <li>In the presence of method overloading and method overriding you might run into situations where a call is considered recursive although it really is not.
 * <li>Catching {@code Throwable} around a recursive might lead to problems
 * <li>Non trivial continuation passing style examples do not work.
 * <li>Probably many unrecognized edge cases.
 * </ul>
 * 
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * import groovy.transform.TailRecursive
 *
 * &#64;TailRecursive
 * long sizeOfList(list, counter = 0) {
 *     if (list.size() == 0) {
 *         counter
 *     } else {
 *        sizeOfList(list.tail(), counter + 1) 
 *     }
 * }
 *
 * // Without &#64;TailRecursive a StackOverFlowError
 * // is thrown.
 * assert sizeOfList(1..10000) == 10000
 * </pre>
 *
 * @since 2.3
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass(["org.codehaus.groovy.transform.tailrec.TailRecursiveASTTransformation"])
@interface TailRecursive {
}
