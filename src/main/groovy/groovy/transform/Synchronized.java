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
package groovy.transform;

import org.codehaus.groovy.transform.GroovyASTTransformationClass;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to make a method call synchronized for concurrency handling
 * with some useful baked-in conventions.
 * <p>
 * {@code @Synchronized} is a safer variant of the <code>synchronized</code> method modifier.
 * The annotation can only be used on static and instance methods. It operates similarly to
 * the <code>synchronized</code> keyword, but it locks on different objects. When used with
 * an instance method, the <code>synchronized</code> keyword locks on <code>this</code>, but the annotation
 * locks on a (by default automatically generated) field named <code>$lock</code>.
 * If the field does not exist, it is created for you. If you annotate a static method,
 * the annotation locks on a static field named <code>$LOCK</code> instead.
 * <p>
 * If you want, you can create these locks yourself.
 * The <code>$lock</code> and <code>$LOCK</code> fields will not be generated if you create
 * them yourself. You can also choose to lock on another field, by specifying its name as
 * parameter to the {@code @Synchronized} annotation. In this usage variant, the lock field
 * will not be created automatically, and you must explicitly create it yourself.
 * <p>
 * <em>Rationale:</em> Locking on <code>this</code> or your own class object can have unfortunate side-effects,
 * as other code not under your control can lock on these objects as well, which can
 * cause race conditions and other nasty threading-related bugs.
 * <p>
 * <em>Example usage:</em>
 * <pre>
 * class SynchronizedExample {
 *   private final myLock = new Object()
 *
 *   {@code @}Synchronized
 *   static void greet() {
 *     println "world"
 *   }
 *
 *   {@code @}Synchronized
 *   int answerToEverything() {
 *     return 42
 *   }
 *
 *   {@code @}Synchronized("myLock")
 *   void foo() {
 *     println "bar"
 *   }
 * }
 * </pre>
 * which becomes:
 * <pre>
 * class SynchronizedExample {
 *   private static final $LOCK = new Object[0]
 *   private final $lock = new Object[0]
 *   private final myLock = new Object()
 *
 *   static void greet() {
 *     synchronized($LOCK) {
 *       println "world"
 *     }
 *   }
 *
 *   int answerToEverything() {
 *     synchronized($lock) {
 *       return 42
 *     }
 *   }
 *
 *   void foo() {
 *     synchronized(myLock) {
 *       println "bar"
 *     }
 *   }
 * }
 * </pre>
 *
 * <em>Credits:</em> this annotation is inspired by the Project Lombok annotation of the
 * same name. The functionality has been kept similar to ease the learning
 * curve when swapping between these two tools.
 * <p>
 * <em>Details:</em> If <code>$lock</code> and/or <code>$LOCK</code> are auto-generated, the fields are initialized
 * with an empty <code>Object[]</code> array, and not just a new <code>Object()</code> as many snippets using
 * this pattern tend to use. This is because a new <code>Object</code> is NOT serializable, but
 * a 0-size array is. Therefore, using {@code @Synchronized} will not prevent your
 * object from being serialized.
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * import groovy.transform.Synchronized
 *
 * class Util {
 *     private counter = 0
 *
 *     private def list = ['Groovy']
 *
 *     private Object listLock = new Object[0]
 *
 *     &#64;Synchronized
 *     void workOnCounter() {
 *         assert 0 == counter
 *         counter++
 *         assert 1 == counter
 *         counter --
 *         assert 0 == counter
 *     }
 *
 *     &#64;Synchronized('listLock')
 *     void workOnList() {
 *         assert 'Groovy' == list[0]
 *         list &lt;&lt; 'Grails'
 *         assert 2 == list.size()
 *         list = list - 'Grails'
 *         assert 'Groovy' == list[0]
 *     }
 * }
 *
 * def util = new Util()
 * def tc1 = Thread.start {
 *     100.times {
 *         util.workOnCounter()
 *         sleep 20 
 *         util.workOnList()
 *         sleep 10
 *     }
 * }
 * def tc2 = Thread.start {
 *     100.times {
 *         util.workOnCounter()
 *         sleep 10 
 *         util.workOnList()
 *         sleep 15
 *     }
 * }
 * tc1.join()
 * tc2.join()
 * </pre>
 *
 * @since 1.7.3
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.SynchronizedASTTransformation")
public @interface Synchronized {
    /**
     * @return if a user specified lock object with the given name should be used
     */
    String value () default "";
}
