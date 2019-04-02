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
 * Allows "interrupt-safe" executions of scripts by adding a custom check for interruption
 * into loops (for, while, ...) and at the start of closures and methods.
 * <p>
 * This is especially useful when executing foreign scripts that you do not have control over. Inject this
 * transformation into a script that you need to interrupt based on some custom criteria.
 * <p>
 * Annotating anything in a script will cause for loops, while loops, methods, and closures to make a
 * check against the specified closure. If the closure yields true (according to GroovyTruth), then the script
 * will throw an InterruptedException. The annotation by default applies to any classes defined in the script
 * as well. Annotated a class will cause (by default) all classes in the entire file ('Compilation Unit') to be
 * enhanced. You can fine tune what is enhanced using the annotation parameters.
 * <p>
 * The following is sample usage of the annotation:
 * <pre>
 * <code>@ConditionalInterrupt({ counter++> 10})</code>
 * import groovy.transform.ConditionalInterrupt
 *
 * counter = 0
 * def scriptMethod() {
 *      4.times {
 *          println 'executing script method...'
 *      }
 * }
 *
 * scriptMethod()
 * </pre>
 * Which results in the following code being generated (XXXXXX will be replaced with some runtime generated hashCode). Notice the checks and exceptions:
 * <pre>
 * public class script1291741477073 extends groovy.lang.Script {
 *   Object counter = 0
 *
 *   public java.lang.Object run() {
 *     counter = 0
 *   }
 *
 *   public java.lang.Object scriptMethod() {
 *     if (this.conditionalTransformXXXXXX$condition()) {
 *       throw new java.lang.InterruptedException('Execution interrupted. The following condition failed: { counter++> 10}')
 *     }
 *     4.times({
 *       if (this.conditionalTransformXXXXXX$condition()) {
 *         throw new java.lang.InterruptedException('Execution interrupted. The following condition failed: { counter++> 10}')
 *       }
 *       this.println('executing script method...')
 *     })
 *   }
 *
 *   private java.lang.Object conditionalTransformXXXXXX$condition() {
 *     counter++ > 10
 *   }
 * }
 * </pre>
 *
 * Note that when you're annotating scripts, the variable scoping semantics are unchanged. Therefore, you must be
 * careful about the variable scope you're using. Make sure that variables you reference in the closure parameter
 * are in scope during script execution. The following example will throw a MissingPropertyException because
 * counter is not in scope for a class:
 * <pre>
 * import groovy.transform.ConditionalInterrupt
 *
 * def counter = 0
 * <code>@ConditionalInterrupt({ counter++> 10})</code>
 * class MyClass {
 *   def myMethod() {
 *     4.times {
 *       println 'executing script method...'
 *     }
 *   }
 * }
 *
 * new MyClass().myMethod()
 * </pre>
 * Additional usage examples can be found in the unit test for this class.
 *
 * @see TimedInterrupt
 * @see ThreadInterrupt
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.LOCAL_VARIABLE])
@GroovyASTTransformationClass(['org.codehaus.groovy.transform.ConditionalInterruptibleASTTransformation'])
@interface ConditionalInterrupt {
    /**
     * Set this to false if you have multiple classes within one source file and only
     * want a conditional check on some of the classes. Place annotations on the classes
     * you want enhanced. Set to true (the default) for blanket coverage of conditional
     * checks on all methods, loops and closures within all classes/script code.
     *
     * For even finer-grained control see {@code applyToAllMembers}.
     *
     * @see #applyToAllMembers()
     */
    boolean applyToAllClasses() default true

    /**
     * Set this to false if you have multiple methods/closures within a class or script and only
     * want conditional checks on some of them. Place annotations on the methods/closures that
     * you want enhanced. When false, {@code applyToAllClasses} is automatically set to false.
     *
     * Set to true (the default) for blanket coverage of conditional checks on all methods, loops
     * and closures within the class/script.
     *
     * @since 2.2.0* @see #applyToAllClasses()
     */
    boolean applyToAllMembers() default true

    /**
     * By default a conditional check is added to the start of all user-defined methods. To turn this off simply
     * set this parameter to false.
     */
    boolean checkOnMethodStart() default true

    /**
     * Sets the type of exception which is thrown.
     */
    Class thrown() default InterruptedException

    /**
     * Conditional check - set as a closure expression.
     */
    Class value()
}
