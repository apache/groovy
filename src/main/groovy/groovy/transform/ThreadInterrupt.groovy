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

/**
 * Allows "interrupt-safe" executions of scripts by adding Thread.currentThread().isInterrupted()
 * checks into loops (for, while) and at the start of closures and methods.
 * <p>
 * This is especially useful when executing foreign scripts that you do not have control over. Inject this
 * transformation into a script that you need to interrupt.
 * <p>
 * Annotating anything in a script will cause for loops, while loops, methods, and closures to make an
 * isInterruptedCheck and throw a InterruptedException if the check yields true. The annotation by default
 * will apply to any classes defined in the script as well. Annotated a class will cause (by default) all classes
 * in the entire file ('Compilation Unit') to be enhanced. You can fine tune what is enhanced using the annotation
 * parameters.
 * <p>
 * The following is sample usage of the annotation:
 *
 * <pre>
 * <code>@groovy.transform.ThreadInterrupt</code>
 * def scriptMethod() {
 *   4.times {
 *     println 'executing script method...'
 *   }
 * }
 *
 * class MyClass {
 *   def myMethod() {
 *     for (i in (1..10)) {
 *       println 'executing method...'
 *     }
 *   }
 * }
 *
 * scriptMethod()
 * new MyClass().myMethod()
 * </pre>
 *
 * Which results in the following code being generated. Notice the checks and exceptions:
 *
 * <pre>
 * public class script1290627909406 extends groovy.lang.Script {
 *
 *     public java.lang.Object scriptMethod() {
 *         if (java.lang.Thread.currentThread().isInterrupted()) {
 *             throw new java.lang.InterruptedException('Execution Interrupted')
 *         }
 *         4.times({
 *             if (java.lang.Thread.currentThread().isInterrupted()) {
 *                 throw new java.lang.InterruptedException('Execution Interrupted')
 *             }
 *             this.println('executing script method...')
 *         })
 *     }
 * }
 * public class MyClass extends java.lang.Object {
 *
 *   public java.lang.Object myMethod() {
 *     if (java.lang.Thread.currentThread().isInterrupted()) {
 *       throw new java.lang.InterruptedException('Execution Interrupted')
 *     }
 *     for (java.lang.Object i : (1..10)) {
 *       if (java.lang.Thread.currentThread().isInterrupted()) {
 *         throw new java.lang.InterruptedException('Execution Interrupted')
 *       }
 *       this.println('executing method...')
 *     }
 *   }
 * }
 *
 * this.scriptMethod()
 * new MyClass().myMethod()
 * </pre>
 * Additional usage examples can be found in the unit test for this class.
 *
 * @see TimedInterrupt
 * @see ConditionalInterrupt
 * @since 1.8.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.LOCAL_VARIABLE])
@GroovyASTTransformationClass(["org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation"])
@interface ThreadInterrupt {
    /**
     * Set this to false if you have multiple classes within one source file and only
     * want isInterrupted checks on some of the classes. Place annotations on the classes
     * you want enhanced. Set to true (the default) for blanket coverage of isInterrupted
     * checks on all methods, loops and closures within all classes/script code.
     *
     * For even finer-grained control see {@code applyToAllMembers}.
     *
     * @see #applyToAllMembers()
     */
    boolean applyToAllClasses() default true

    /**
     * Set this to false if you have multiple methods/closures within a class or script and only
     * want isInterrupted checks on some of them. Place annotations on the methods/closures that
     * you want enhanced. When false, {@code applyToAllClasses} is automatically set to false.
     *
     * Set to true (the default) for blanket coverage of isInterrupted checks on all methods, loops
     * and closures within the class/script.
     *
     * @since 2.2.0
     * @see #applyToAllClasses()
     */
    boolean applyToAllMembers() default true

    /**
     * By default an isInterrupted check is added to the start of all user-defined methods. To turn this off simply
     * set this parameter to false.
     */
    boolean checkOnMethodStart() default true

    /**
     * Sets the type of exception which is thrown.
     */
    Class thrown() default InterruptedException
}
