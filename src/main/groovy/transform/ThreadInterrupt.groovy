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
package groovy.transform

import java.lang.annotation.Documented
import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass


/**
 * Allows "interrupt-safe" executions of scripts by adding Thread.currentThread().isInterrupted()
 * checks on loops (for, while, do), the first statement of closures, and the first statement of methods.
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
 * Extensive usage examples can be found in the unit test for this class. A smaller example is presented here.
 * The following is sample usage of the annotation:
 *
 * <pre>
 * <code>@groovy.transform.ThreadInterrupt</code>
 * def scriptMethod() {
 *     4.times {
 *         println 'executing script method...'
 *     }
 * }
 *
 * class MyClass {
 *
 *   def myMethod() {
 *       for (i in (1..10)) {
 *           println 'executing method...'
 *       }
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
 *     public java.lang.Object myMethod() {
 *         if (java.lang.Thread.currentThread().isInterrupted()) {
 *             throw new java.lang.InterruptedException('Execution Interrupted')
 *         }
 *         for (java.lang.Object i : (1..10)) {
 *             if (java.lang.Thread.currentThread().isInterrupted()) {
 *                 throw new java.lang.InterruptedException('Execution Interrupted')
 *             }
 *             this.println('executing method...')
 *         }
 *     }
 * }
 *
 * this.scriptMethod()
 * new MyClass().myMethod()
 * </pre>
 *
 * @see groovy.transform.TimedInterrupt
 * @see groovy.transform.ConditionalInterrupt
 * @author Cedric Champeau
 * @author Hamlet D'Arcy
 * @since 1.8.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ ElementType.METHOD, ElementType.TYPE])
@GroovyASTTransformationClass(["org.codehaus.groovy.transform.ThreadInterruptibleASTTransformation"])
public @interface ThreadInterrupt {
    /**
     * By default, annotating anything in a source file ('Compilation Unit') will trigger this transformation
     * for all classes and scripts in that file. If you add the Annotation to an import statement, then all
     * scripts and Classes will be enhanced. If you want to change this behavior then set applyToAllClasses
     * to false. If you annotate a type then only that type will be augmented, not other types or the surrounding
     * script. If you annotate a script, then any enclosed types will not be augmented.
     * @return
     */
    boolean applyToAllClasses() default true

    /**
     * By default an isInterrupted check is added to the start of all user-defined methods. To turn this off simply
     * set this parameter to false.
     * @return
     */
    boolean checkOnMethodStart() default true

    /**
     * Sets the type of exception which is thrown.
     *
     * @return
     */
    Class thrown() default InterruptedException
}
