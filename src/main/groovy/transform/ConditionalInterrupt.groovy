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


import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target
import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * Allows "interrupt-safe" executions of scripts by adding a custom check for interruption
 * on loops (for, while, do), the first statement of closures, and the first statement of methods.
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
 * Extensive usage examples can be found in the unit test for this class. A smaller example is presented here.
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
 * Which results in the following code being generated. Notice the checks and exceptions:
 * <pre>
 * public class script1291741477073 extends groovy.lang.Script {
 *
 *     Object counter = 0
 *
 *     public java.lang.Object run() {
 *         counter = 0
 *     }
 *
 *     public java.lang.Object scriptMethod() {
 *         if (this.conditionalTransform$condition()) {
 *             throw new java.lang.InterruptedException('Execution interrupted. The following condition failed: { counter++> 10}')
 *         }
 *         4.times({
 *             if (this.conditionalTransform$condition()) {
 *                 throw new java.lang.InterruptedException('Execution interrupted. The following condition failed: { counter++> 10}')
 *             }
 *             this.println('executing script method...')
 *         })
 *     }
 *
 *     private java.lang.Object conditionalTransform$condition() {
 *         counter++ > 10
 *     }
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
 *     def myMethod() {
 *         4.times {
 *             println 'executing script method...'
 *         }
 *     }
 * }
 *
 * new MyClass().myMethod()
 * </pre>
 *
 * @see groovy.transform.TimedInterrupt
 * @see groovy.transform.ThreadInterrupt
 * @author Cedric Champeau
 * @author Hamlet D'Arcy
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ ElementType.METHOD, ElementType.TYPE])
@GroovyASTTransformationClass(["org.codehaus.groovy.transform.ConditionalInterruptibleASTTransformation"])
public @interface ConditionalInterrupt {
    /**
     * By default, annotating anything in a source file ('Compilation Unit') will trigger this transformation
     * for all classes and scripts in that file. If you add the Annotation to an import statement, then all
     * scripts and Classes will be enhanced. If you want to change this behavior then set applyToAllClasses
     * to false. If you annotate a type then only that type will be augmented, not other types or the surrounding
     * script. If you annotate a script, then any enclosed types will not be augmented.
     *
     * @return
     */
    boolean applyToAllClasses() default true;
    /**
     * By default an isInterrupted check is added to the start of all user-defined methods. To turn this off simply
     * set this parameter to false.
     *
     * @return
     */
    boolean checkOnMethodStart() default true;

    /**
     * Sets the type of exception which is thrown.
     *
     * @return
     */
    Class thrown() default InterruptedException;

    /**
     * Condition should be set as a closure expression. 
     * @return
     */
    Class value();

}

