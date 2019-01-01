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
 * Method annotation that creates a cache for the results of the execution of the annotated method. Whenever the method
 * is called, the mapping between the parameters and the return value is preserved in a cache making subsequent calls with
 * the same arguments fast.
 * 
 * <p>
 * Example usage:
 * 
 * <pre>
 * class MemoizedExample {
 * 
 *     {@code @Memoized}
 *     int sum(int n1, int n2) {
 *         println "$n1 + $n2 = ${n1 + n2}" 
 *         n1 + n2
 *     }
 * }
 * </pre>
 * 
 * which becomes (approximately):
 * 
 * <pre>
 * class MemoizedExample {
 * 
 *     private final Closure memoizedSum = { int n1, int n2 ->
 *         private$method$memoizedSum(n1,n2)
 *     }.memoize()
 * 
 *     int sum(int n1, int n2) {
 *         memoizedSum(n1, n2)
 *     }
 *
 *     private private$method$memoizedSum(int n1, int n2) {
 *         println "$n1 + $n2 = ${n1 + n2}"
 *         n1 + n2
 *     }
 * }
 * </pre>
 * 
 * <p>
 * Upon execution of this code:
 * 
 * <pre>
 * def instance = new MemoizedExample()
 * println instance.sum(1, 2)
 * println instance.sum(1, 2)
 * println instance.sum(2, 3)
 * println instance.sum(2, 3)
 * </pre>
 * 
 * The following will be output:
 * 
 * <pre>
 * 1 + 2 = 3
 * 3
 * 3
 * 2 + 3 = 5
 * 5
 * 5
 * </pre>
 * 
 * <p>More examples:</p>
 * <pre class="groovyTestCase">
 * import groovy.transform.*
 *
 * // Script variable which is changed when increment()
 * // method is invoked. 
 * // If cached method call is invoked then the value
 * // of this variable will not change.
 * &#64;Field boolean incrementChange = false
 *
 * &#64;Memoized 
 * int increment(int value) {
 *     incrementChange = true
 *     value + 1 
 * }
 *
 * // Invoke increment with argument 10.
 * increment(10)
 *
 * // increment is invoked so incrementChange is true.
 * assert incrementChange
 *
 * // Set incrementChange back to false.
 * incrementChange = false
 *
 * // Invoke increment with argument 10.
 * increment(10)
 *
 * // Now the cached method is used and
 * // incrementChange is not changed.
 * assert !incrementChange
 *
 * // Invoke increment with other argument value.
 * increment(11)
 *
 * // increment is invoked so incrementChange is true.
 * assert incrementChange
 * </pre>
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.METHOD })
@GroovyASTTransformationClass("org.codehaus.groovy.transform.MemoizedASTTransformation")
public @interface Memoized {
    
    /**
     * Number of cached return values to protect from garbage collection.
     */
    int protectedCacheSize() default 0;
    
    /**
     * The maximum size the cache can grow to.
     */
    int maxCacheSize() default 0;
}
