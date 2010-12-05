/*
 * Copyright 2008-2010 the original author or authors.
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
 * <br/>
 * <br/>
 * This is especially useful when executing foreign scripts that you do not have control over. Inject this
 * transformation into a script that you need to interrupt.
 * <br/>
 * <br/>
 * Annotating anything in a script will cause for loops, while loops, methods, and closures to make an
 * isInterruptedCheck and throw a InterruptedException if the check yields true. The annotation by default
 * will apply to any classes defined in the script as well. Annotated a class will cause (by default) all classes
 * in the entire file ('Compilation Unit') to be enhanced. You can fine tune what is enhanced using the annotation
 * parameters.
 * <br/>
 * <br/>
 * Extensive usage examples can be found in the unit test for this class. A smaller example is presented here.
 * The following is sample usage of the annotation:
 * <br/>
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
 * <br/>
 * Which results in the following code being generated. Notice the checks and exceptions:
 * <br/>
 * <pre>
 * public class script1291120482763 extends groovy.lang.Script {
 *
 *     public java.lang.Object scriptMethod() {
 *         if (this.conditionalTransform$condition()) {
 *             throw new java.lang.InterruptedException('Execution Interrupted')
 *         }
 *         4.times({
 *             if (this.conditionalTransform$condition()) {
 *                 throw new java.lang.InterruptedException('Execution Interrupted')
 *             }
 *             this.println('executing script method...')
 *         })
 *     }
 *
 *     protected java.lang.Boolean conditionalTransform$condition() {
 *         ( counter )++ > 10
 *     }
 *
 * }
 * </pre>
 * <br/>
 *
 * Note that when you're annotating scripts, the variable scoping semantics are unchanged. Therefore, you must be
 * careful about the variable scope you're using. For example will work :
 *
 * <pre><code>@ConditionalInterrupt({counter++>1})</code>
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
 * while the following will throw a 'No such property: counter' error :
 *
 * <pre><code>@ConditionalInterrupt({counter++>1})</code>
 * import groovy.transform.ConditionalInterrupt
 *
 * def counter = 0
 * def scriptMethod() {
 *      4.times {
 *          println 'executing script method...'
 *      }
 * }
 *
 * scriptMethod()
 * </pre>
 *
 * @author Cédric Champeau
 * @author Hamlet D'Arcy
 *
 * @since 1.8.0
 */
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
     * @return
     */
    boolean applyToAllClasses() default true;
    /**
     * By default an isInterrupted check is added to the start of all user-defined methods. To turn this off simply
     * set this parameter to false.
     * @return
     */
    boolean checkOnMethodStart() default true;


    /**
     * Condition should be set as a closure expression. The closure will automatically be converted to a
     * boolean expression statement.
     * @return
     */
    Class value();

}

