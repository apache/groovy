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
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import org.codehaus.groovy.transform.GroovyASTTransformationClass

/**
 * Allows safe timed executions of scripts by adding elapsed time checks on loops (for, while, do)
 * to the first statement of closures and methods.
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
 * Extensive usage examples can be found in the unit test for this class. A smaller example is presented
 * here. The following is sample usage of the annotation forcing the script to timeout after 1000 seconds:
 *
 * <pre>
 * import groovy.transform.TimedInterrupt
 * import java.util.concurrent.TimeUnit
 *
 * {@code @TimedInterrupt}(value = 1000L, unit = TimeUnit.SECONDS)
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
 *         timedInterruptXXXXXX$expireTime = System.nanoTime() + TimeUnit.NANOSECONDS.convert(1000, TimeUnit.SECONDS)
 *         timedInterruptXXXXXX$startTime = new java.util.Date()
 *     }
 *
 *     public java.lang.Object method() {
 *         if (timedInterruptXXXXXX$expireTime < System.nanoTime()) {
 *             throw new TimeoutException('Execution timed out after 1000 units. Start time: ' + timedInterruptXXXXXX$startTime)
 *         }
 *         return this.println('...')
 *     }
 * }
 * </pre>
 *
 * @author Hamlet D'Arcy
 * @author Cedric Champeau
 * @author Paul King
 * @see ThreadInterrupt
 * @see ConditionalInterrupt
 * @since 1.8.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target([ ElementType.PACKAGE, ElementType.METHOD, ElementType.FIELD, ElementType.TYPE, ElementType.LOCAL_VARIABLE])
@GroovyASTTransformationClass(["org.codehaus.groovy.transform.TimedInterruptibleASTTransformation"])
public @interface TimedInterrupt {
    /**
     * In many scenarios, the use of this annotation is to guard against gross time
     * delays when executing scripts. In such cases you can inject the annotation or
     * use a single annotation on your class or on any eligible element in a script.
     * This will trigger this transformation for all classes (including any script class)
     * in that source file. If you want to change this behavior then set {@code applyToAllClasses}
     * to false. This gives you more fine-grained control over what parts are enhanced and
     * allows you to specify different timing constraints on different classes if needed.
     * When set to false, if you annotate a type then only that type will be augmented, not
     * other types or the surrounding script. If you annotate a script, then any enclosed
     * types will not be augmented. For even finer-grained control see {@code applyToAllMembers}.
     *
     * @see #applyToAllMembers()
     */
    boolean applyToAllClasses() default true

    /**
     * If set to false, it automatically sets {@code applyToAllClasses} to false. In addition,
     * if you annotate a method (or Closure field), only that method (or Closure) will be enhanced.
     * This is useful if you want to have different timing constraints on different methods. The implication
     * is that if you don't set this to false, there is little value in having more than one Annotation
     * in any one source file or within any one class.
     *
     * @since 2.2.0
     * @see #applyToAllClasses()
     */
    boolean applyToAllMembers() default true

    /**
     * By default an isInterrupted check is added to the start of all user-defined methods. To turn this off
     * simply set this parameter to false.
     */
    boolean checkOnMethodStart() default true

    /**
     * The maximum elapsed time the script will be allowed to run for. By default it is measure in seconds
     */
    long value();

    /**
     * The TimeUnit of the value parameter. By default it is TimeUnit.SECONDS.
     */
    TimeUnit unit() default TimeUnit.SECONDS

    /**
     * The type of exception thrown when timeout is reached.
     */
    Class thrown() default TimeoutException
}

