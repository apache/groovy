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

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Allows safe timed executions of scripts by adding elapsed time checks into loops (for, while)
 * and at the start of closures and methods and throwing an exception if a timeout occurs.
 * <p>
 * This is especially useful when executing foreign scripts that you do not have control over.
 * Inject this transformation into a script that you want to timeout after a specified amount of time.
 * <p>
 * Annotating anything in a script will cause for loops, while loops, methods, and closures to make an
 * elapsed time check and throw a TimeoutException if the check yields true. The annotation by default
 * will apply to any classes defined in the script as well. Annotating a class will cause (by default)
 * all classes in the entire file ('Compilation Unit') to be enhanced. You can fine tune what is
 * enhanced using the annotation parameters. Static methods and static fields are ignored.
 * <p>
 * The following is sample usage of the annotation forcing the script to timeout after 5 minutes (300 seconds):
 *
 * <pre>
 * import groovy.transform.TimedInterrupt
 * import java.util.concurrent.TimeUnit
 *
 * {@code @TimedInterrupt}(value = 300L, unit = TimeUnit.SECONDS)
 * class MyClass {
 *      def method() {
 *          println '...'
 *      }
 * }
 * </pre>
 * This sample script will be transformed at compile time to something that resembles this:
 * <pre>
 * import java.util.concurrent.TimeUnit
 * import java.util.concurrent.TimeoutException
 *
 * public class MyClass {
 *     // XXXXXX below is a placeholder for a hashCode value at runtime
 *     final private long timedInterruptXXXXXX$expireTime
 *     final private java.util.Date timedInterruptXXXXXX$startTime
 *
 *     public MyClass() {
 *         timedInterruptXXXXXX$expireTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(300, TimeUnit.SECONDS)
 *         timedInterruptXXXXXX$startTime = new java.util.Date()
 *     }
 *
 *     public java.lang.Object method() {
 *         if (timedInterruptXXXXXX$expireTime < System.nanoTime()) {
 *             throw new TimeoutException('Execution timed out after 300 units. Start time: ' + timedInterruptXXXXXX$startTime)
 *         }
 *         return this.println('...')
 *     }
 * }
 * </pre>
 * See the unit test for this class for additional examples.
 *
 * @see ThreadInterrupt
 * @see ConditionalInterrupt
 * @since 1.8.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.LOCAL_VARIABLE])
@GroovyASTTransformationClass(['org.codehaus.groovy.transform.TimedInterruptibleASTTransformation'])
@interface TimedInterrupt {
    /**
     * Set this to false if you have multiple classes within one source file and only want
     * timeout checks on some of the classes (or you want different time constraints on different classes).
     * Place an annotation with appropriate parameters on each class you want enhanced.
     * Set to true (the default) for blanket coverage of timeout checks on all methods, loops
     * and closures within all classes/script code.
     *
     * For even finer-grained control see {@code applyToAllMembers}.
     *
     * @see #applyToAllMembers()
     */
    boolean applyToAllClasses() default true

    /**
     * Set this to false if you have multiple methods/closures within a class or script and only
     * want timeout checks on some of them (or you want different time constraints on different methods/closures).
     * Place annotations with appropriate parameters on the methods/closures that you want enhanced.
     * When false, {@code applyToAllClasses} is automatically set to false.
     *
     * Set to true (the default) for blanket coverage of timeout checks on all methods, loops
     * and closures within the class/script.
     *
     * @since 2.2.0* @see #applyToAllClasses()
     */
    boolean applyToAllMembers() default true

    /**
     * By default a time check is added to the start of all user-defined methods. To turn this off
     * simply set this parameter to false.
     */
    boolean checkOnMethodStart() default true

    /**
     * The maximum elapsed time the script will be allowed to run for. By default it is measure in seconds
     */
    long value()

    /**
     * The TimeUnit of the value parameter. By default it is TimeUnit.SECONDS.
     */
    TimeUnit unit() default TimeUnit.SECONDS

    /**
     * The type of exception thrown when timeout is reached.
     */
    Class thrown() default TimeoutException
}

