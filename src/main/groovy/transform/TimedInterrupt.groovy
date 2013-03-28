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
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

/**
 * Allows safe timed executions of scripts by adding elapsed time checks on loops (for, while, do), the first statement
 * of closures, and the first statement of methods.
 * <p>
 * This is especially useful when executing foreign scripts that you do not have control over. Inject this
 * transformation into a script that you want to timeout after a specified amount of timet.
 * <p>
 * Annotating anything in a script will cause for loops, while loops, methods, and closures to make an
 * elapsed time check and throw a TimeoutException if the check yields true. The annotation by default
 * will apply to any classes defined in the script as well. Annotated a class will cause (by default) all classes
 * in the entire file ('Compilation Unit') to be enhanced. You can fine tune what is enhanced using the annotation
 * parameters. Static methods and static fields are ignored.
 * <p>
 * Extensive usage examples can be found in the unit test for this class. A smaller example is presented here.
 * The following is sample usage of the annotation forcing the script to timeout after 1000 seconds:
 *
 * <pre>
 * import groovy.transform.TimedInterrupt
 * import java.util.concurrent.TimeUnit
 *
 * {@code @TimedInterrupt}(value = 1000L, unit = TimeUnit.SECONDS)
 * class MyClass {
 *
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
 *
 *     final private long TimedInterrupt$expireTime
 *     final private java.util.Date TimedInterrupt$startTime
 *
 *     public MyClass() {
 *         TimedInterrupt$expireTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(1000, TimeUnit.SECONDS)
 *         TimedInterrupt$startTime = new java.util.Date()
 *     }
 *
 *     public java.lang.Object method() {
 *         if (TimedInterrupt$expireTime < System.nanoTime()) {
 *             throw new TimeoutException('Execution timed out after 1000 units. Start time: ' + TimedInterrupt$startTime)
 *         }
 *         return this.println('...')
 *     }
 * }
 * </pre>
 *
 * @author Hamlet D'Arcy
 * @see groovy.transform.ThreadInterrupt
 * @see groovy.transform.ConditionalInterrupt
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ ElementType.METHOD, ElementType.TYPE])
@GroovyASTTransformationClass(["org.codehaus.groovy.transform.TimedInterruptibleASTTransformation"])
public @interface TimedInterrupt {
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
     * The maximum elapsed time the script will be allowed to run for. By default it is measure in seconds
     * @return
     */
    long value();

    /**
     * The TimeUnit of the value parameter. By default it is TimeUnit.SECONDS.
     * @return
     */
    TimeUnit unit() default TimeUnit.SECONDS;

    /**
     * The type of exception thrown when timeout is reached.
     * @return
     */
    Class thrown() default TimeoutException;
}

