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
 * This annotation is used in conjunction with {@link WithReadLock} to support read and write synchronization on a method.
 * <p>
 * To use this annotation, declare {@code @WithWriteLock} on your method. The method may be either an instance method or
 * a static method. The resulting method will allow only one thread access to the method at a time, and will wait to access
 * the method until any other read locks have been released.
 * <p>
 * This annotation is a declarative wrapper around the JDK's <code>java.util.concurrent.locks.ReentrantReadWriteLock</code>.
 * Objects containing this annotation will have a ReentrantReadWriteLock field named <code>$reentrantLock</code> added to the class,
 * and method access is protected by the lock. If the method is static then the field is static and named <code>$REENTRANTLOCK</code>.
 * <p>
 * The annotation takes an optional parameter for the name of the field. This field must exist on the class and must be
 * of type ReentrantReadWriteLock.
 * <p>
 * To understand how this annotation works, it is convenient to think in terms of the source code it replaces. The following
 * is a typical usage of this annotation from Groovy:
 * <pre>
 * import groovy.transform.*;
 *
 * public class ResourceProvider {
 *
 *     private final Map&lt;String, String&gt; data = new HashMap&lt;String, String&gt;();
 *
 *    {@code @WithReadLock}
 *     public String getResource(String key) throws Exception {
 *             return data.get(key);
 *     }
 *
 *    {@code @WithWriteLock}
 *     public void refresh() throws Exception {
 *             //reload the resources into memory
 *     }
 * }
 * </pre>
 * As part of the Groovy compiler, code resembling this is produced:
 * <pre>
 * import java.util.concurrent.locks.ReentrantReadWriteLock;
 * import java.util.concurrent.locks.ReadWriteLock;
 *
 * public class ResourceProvider {
 *
 *     private final ReadWriteLock $reentrantlock = new ReentrantReadWriteLock();
 *     private final Map&lt;String, String&gt; data = new HashMap&lt;String, String&gt;();
 *
 *     public String getResource(String key) throws Exception {
 *         $reentrantlock.readLock().lock();
 *         try {
 *             return data.get(key);
 *         } finally {
 *             $reentrantlock.readLock().unlock();
 *         }
 *     }
 *
 *     public void refresh() throws Exception {
 *         $reentrantlock.writeLock().lock();
 *         try {
 *             //reload the resources into memory
 *         } finally {
 *             $reentrantlock.writeLock().unlock();
 *         }
 *     }
 * }
 * </pre>
 *
 * @since 1.8.0
 */
@java.lang.annotation.Documented
@Retention(RetentionPolicy.SOURCE)
@Target({ElementType.METHOD})
@GroovyASTTransformationClass("org.codehaus.groovy.transform.ReadWriteLockASTTransformation")
public @interface WithWriteLock {
    /**
     * @return if a user specified lock object with the given name should be used
     *      the lock object must exist. If the annotated method is static then the 
     *      lock object must be static. If the annotated method is not static then 
     *      the lock object must not be static. 
     */
    String value () default "";
}
