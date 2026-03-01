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

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Method annotation to make a method execute asynchronously and return an
 * {@link groovy.concurrent.Awaitable Awaitable}.
 * <p>
 * When applied to a method, the {@code @Async} transformation will:
 * <ul>
 *   <li>Change the method's return type to {@code Awaitable<T>} (where {@code T} is the original return type)</li>
 *   <li>Execute the method body asynchronously via {@link org.apache.groovy.runtime.async.AsyncSupport#executeAsync AsyncSupport.executeAsync}</li>
 *   <li>Transform any {@code await(future)} calls within the method to use {@link groovy.concurrent.AsyncUtils#await AsyncUtils.await}</li>
 *   <li>For generator methods containing {@code yield return}, return
 *       {@link groovy.concurrent.AsyncStream AsyncStream<T>} instead</li>
 * </ul>
 * <p>
 * The {@code await(future)} pattern inside an {@code @Async} method blocks the async thread until the
 * given future completes and returns the unwrapped result. This provides a sequential programming model
 * over asynchronous operations, similar to JavaScript's {@code async/await} or C#'s {@code async/await}.
 * <p>
 * <em>Example usage:</em>
 * <pre>
 * import groovy.transform.Async
 * import groovy.concurrent.Awaitable
 *
 * class DataService {
 *
 *     {@code @}Async
 *     def fetchUser(long id) {
 *         def profile = await(fetchProfile(id))
 *         def orders = await(fetchOrders(id))
 *         return [profile: profile, orders: orders]
 *     }
 *
 *     {@code @}Async
 *     Awaitable&lt;Map&gt; fetchProfile(long id) {
 *         return [name: "User$id"]
 *     }
 *
 *     {@code @}Async
 *     Awaitable&lt;List&gt; fetchOrders(long id) {
 *         return ["order1", "order2"]
 *     }
 * }
 *
 * def service = new DataService()
 * def awaitable = service.fetchUser(1)    // returns Awaitable
 * def result = awaitable.get()            // blocks until complete
 * assert result.profile.name == 'User1'
 * assert result.orders.size() == 2
 * </pre>
 * <p>
 * For ad-hoc async execution without annotating a method, use the {@code async} keyword:
 * <pre>
 * def awaitable = async {
 *     def data = await fetchFromRemote()
 *     return process(data)
 * }
 * def result = awaitable.get()
 * </pre>
 * <p>
 * <b>Custom Executor:</b> By default, tasks run on virtual threads (JDK 21+) or
 * a dedicated daemon thread pool whose size is controlled by the system property
 * {@code groovy.async.parallelism} (JDK &lt; 21). You can specify a custom
 * executor by providing the name of a field in the class:
 * <pre>
 * class MyService {
 *     private final Executor myExecutor = Executors.newFixedThreadPool(10)
 *
 *     {@code @}Async(executor = "myExecutor")
 *     def fetchData() {
 *         return await(remoteCall())
 *     }
 * }
 * </pre>
 * <p>
 * <b>Restrictions:</b>
 * <ul>
 *   <li>Cannot be applied to abstract methods</li>
 *   <li>Cannot be applied to constructors</li>
 *   <li>Cannot be applied to methods that already return {@code Awaitable}</li>
 * </ul>
 *
 * @see groovy.concurrent.AsyncUtils
 * @see groovy.concurrent.Awaitable
 * @see groovy.concurrent.AsyncStream
 * @since 6.0.0
 */
@Documented
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
@GroovyASTTransformationClass("org.codehaus.groovy.transform.AsyncASTTransformation")
public @interface Async {
    /**
     * Optional name of a field in the declaring class that holds an
     * {@link java.util.concurrent.Executor Executor} to use for async execution.
     * If empty (the default), uses the default executor (virtual threads on
     * JDK 21+, or a dedicated daemon thread pool controlled by the system
     * property {@code groovy.async.parallelism}).
     *
     * @return the executor field name, or empty string for the default executor
     */
    String executor() default "";
}
